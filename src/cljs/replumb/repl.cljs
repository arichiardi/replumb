(ns replumb.repl
  (:refer-clojure :exclude [load-file])
  (:require-macros [cljs.env.macros :refer [with-compiler-env]])
  (:require [cljs.js :as cljs]
            [cljs.tagged-literals :as tags]
            [cljs.tools.reader :as r]
            [cljs.analyzer :as ana]
            [cljs.env :as env]
            [cljs.repl :as repl]
            [cljs.pprint :refer [pprint]]
            [replumb.load :as load]
            [replumb.doc-maps :as docs]
            [replumb.common :as common]))

(def ^:dynamic  *custom-eval-fn* "See cljs.js/*eval-fn* in ClojureScript core."
  cljs/js-eval)

(def ^:dynamic *custom-load-fn* "See cljs.js/*load-fn* in ClojureScript core."
  load/js-load)

;;;;;;;;;;;;;
;;; State ;;;
;;;;;;;;;;;;;

;; This is the compiler state atom. Note that cljs/eval wants exactly an atom.
(defonce st (cljs/empty-state))

(defonce app-env (atom {:current-ns 'cljs.user
                        :last-eval-warning nil
                        :initializing? false
                        :needs-init? true}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Util fns - many from mfikes/plank ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ex-info-data "The ex-info data for this file" {:tag ::error})

(defn debug-prn
  [& args]
  (binding [cljs.core/*print-fn* cljs.core/*print-err-fn*]
    (apply println args)))

(defn current-ns
  "Return the current namespace, as a symbol."
  []
  (:current-ns @app-env))

(defn known-namespaces
  []
  (keys (:cljs.analyzer/namespaces @st)))

(defn remove-ns
  "Removes the namespace named by the symbol."
  ([ns]
   (remove-ns env/*compiler* ns))
  ([state ns]
   {:pre [(symbol? ns)]}
   (swap! state update-in [::ana/namespaces] dissoc ns)))

(defn map-keys
  [f m]
  (reduce-kv (fn [r k v] (assoc r (f k) v)) {} m))

(defn repl-read-string
  [line]
  (r/read-string {:read-cond :allow :features #{:cljs}} line))

(defn ns-form?
  [form]
  (and (seq? form) (= 'ns (first form))))

(defn extract-namespace
  [source]
  (let [first-form (repl-read-string source)]
    (when (ns-form? first-form)
      (second first-form))))

(defn resolve
  "From cljs.analizer.api.clj. Given an analysis environment resolve a
  var. Analogous to clojure.core/resolve"
  [env sym]
  {:pre [(map? env) (symbol? sym)]}
  (try
    (ana/resolve-var env sym
      (ana/confirm-var-exists-throw))
    (catch :default _
      (ana/resolve-macro-var env sym))))

(defn get-var
  [env sym]
  (let [var (with-compiler-env st (resolve env sym))
        var (or var
              (if-let [macro-var (with-compiler-env st
                                   (resolve env (symbol "cljs.core$macros" (name sym))))]
                (update (assoc macro-var :ns 'cljs.core)
                  :name #(symbol "cljs.core" (name %)))))]
    (if (= (namespace (:name var)) (str (:ns var)))
      (update var :name #(symbol (name %)))
      var)))

(def valid-opts-set
  "Set of valid option for external input validation:

  * :verbose If true, enables more traces."
  #{:verbose})

(defn valid-opts
  "Extract options according to the valid-opts-set."
  [opts]
  (into {} (filter (comp valid-opts-set first) @app-env)))

(defn env-opts!
  "Reads the map of environment options. Usually these are set when the
  repl is initialized. The function works like merge, the mapping from
  the latter (left-to-right) will be the mapping in the result. Extracts
  the options in the valid-options set."
  [& maps] (apply merge @app-env maps))

(defn make-base-eval-opts!
  "Gets the base set of evaluation options. The variadic arity function
  works like merge, the mapping from the latter (left-to-right) will be
  the mapping in the result. Extracts the options in the valid-options
  set."
  ([]
   (env-opts! {:ns      (:current-ns @app-env)
               :context :expr
               :load    *custom-load-fn*
               :eval    *custom-eval-fn*}))
  ([& maps]
   (apply merge (make-base-eval-opts!) maps)))

(defn self-require?
  [specs]
  (some
    (fn [quoted-spec-or-kw]
      (and (not (keyword? quoted-spec-or-kw))
        (let [spec (second quoted-spec-or-kw)
              ns (if (sequential? spec)
                   (first spec)
                   spec)]
          (= ns @current-ns))))
    specs))

(defn canonicalize-specs
  [specs]
  (letfn [(canonicalize [quoted-spec-or-kw]
            (if (keyword? quoted-spec-or-kw)
              quoted-spec-or-kw
              (as-> (second quoted-spec-or-kw) spec
                (if (vector? spec) spec [spec]))))]
    (map canonicalize specs)))

(defn process-reloads!
  [specs]
  (if-let [k (some #{:reload :reload-all} specs)]
    (let [specs (->> specs (remove #{k}))]
      (if (= k :reload-all)
        (reset! cljs.js/*loaded* #{})
        (apply swap! cljs.js/*loaded* disj (map first specs)))
      specs)
    specs))

(defn make-ns-form
  [kind specs target-ns]
  (if (= kind :import)
    (with-meta `(~'ns ~target-ns
                  (~kind
                    ~@(map (fn [quoted-spec-or-kw]
                             (if (keyword? quoted-spec-or-kw)
                               quoted-spec-or-kw
                               (second quoted-spec-or-kw)))
                        specs)))
      {:merge true :line 1 :column 1})
    (with-meta `(~'ns ~target-ns
                  (~kind
                    ~@(-> specs canonicalize-specs process-reloads!)))
      {:merge true :line 1 :column 1})))

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Eval handling fns ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(defn forward-success!
  "Handles the case when the evaluation returned success.
  Supports the following options (opts = option map):

  * :no-pr-str-on-value avoids wrapping value in pr-str."
  ([opts cb value]
   (cb {:success? true
        :value (if-not (:no-pr-str-on-value opts)
                 (pr-str value)
                 value)})))

(defn forward-error!
  "Handles the case when the evaluation returned error.
  Always pushes a js/Error to the callback."
  ([opts cb error]
   {:pre [(instance? js/Error error)]}
   (set! *e error)
   (cb {:success? false
        :error error})))

(defn reset-last-warning!
  []
  (swap! app-env assoc :last-eval-warning nil))

(defn custom-warning-handler
  "Handles the case when the evaluation returns a warning and can be
  passed as a warning handler when partially applied. At the moment it
  treats warnings as errors."
  [opts cb warning-type env extra]
  (when (:verbose opts)
    (debug-prn (str "Handling warning:\n" (with-out-str (pprint {:warning-type warning-type
                                                                 :env env
                                                                 :extra extra})))))
  (when (warning-type ana/*cljs-warnings*)
    (when-let [s (ana/error-message warning-type extra)]
      (swap! app-env assoc :last-eval-warning (ana/message env s)))))

(defn handle-eval-result!
  "Handles the evaluation result, calling the callback in the right way,
  based on the success or error of the evaluation and executing
  (side-effect!) *before* the callback is called. It expects the same
  map as ClojureScript's cljs.js callback, that is :value if success
  and :error if not.

  ** Every function in this namespace should call handle-eval-result! as
  single point of exit and therefore respect its contract. **

  It supports the following opts (map):

  * :verbose will enable the the evaluation logging, defaults to false.
  * :no-pr-str-on-value avoids wrapping successful value in a pr-str

  Notes

  1. The opts map passed here overrides the environment options.
  2. This function will also clear the :last-eval-warning flag in
     app-env.
  3. There is also an arity for differentiating the side effect based on
     success or error."
  ([opts cb res]
   (handle-eval-result! opts cb identity res))
  ([opts cb side-effect! {:keys [value error] :as res}]
   (handle-eval-result! opts cb side-effect! side-effect! res))
  ([opts cb on-success! on-error! res]
   {:pre [(map? res) (or (find res :error) (find res :value))]}
   (when (:verbose opts)
     (debug-prn "Handling result:\n" (with-out-str (pprint res))))
   (if-let [warning-msg (:last-eval-warning @app-env)]
     (do (when (:verbose opts)
           (debug-prn "Last warning message: " warning-msg))
         (on-error!)
         (reset-last-warning!)
         (forward-error! opts cb (ex-info warning-msg ex-info-data)))
     (let [{:keys [value error]} res]
       (if-not error
         (do (on-success!) (reset-last-warning!) (forward-success! opts cb value))
         (do (on-error!) (reset-last-warning!) (forward-error! opts cb error)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Processing fns - from mfikes/plank ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process-require
  [opts cb kind specs]
  ;; TODO - cannot find a way to handle (require something) correctly, note no quote
  (if-not (= 'quote (ffirst specs))
    (handle-eval-result! opts cb (common/error-argument-must-be-symbol "require" ex-info-data))
    (let [is-self-require? (and (= :kind :require) (self-require? specs))
          [target-ns restore-ns] (if-not is-self-require?
                                   [(:current-ns @app-env) nil]
                                   ['cljs.user (:current-ns @app-env)])
          ns-form (make-ns-form kind specs target-ns)]
      (when (:verbose opts)
        (debug-prn "Processing" kind "via" (pr-str ns-form)))
      (cljs/eval st
                 ns-form
                 (make-base-eval-opts! opts)
                 (fn [error]
                   (handle-eval-result! opts cb
                                        #(when is-self-require?
                                           (swap! app-env assoc :current-ns restore-ns))
                                        (if error
                                          (common/wrap-error error)
                                          (common/wrap-success nil))))))))

(defn process-doc
  [cb env sym]
  (handle-eval-result! {:no-pr-str-on-value true}
                       cb
                       (common/wrap-success
                        (with-out-str
                          (cond
                            (docs/special-doc-map sym) (repl/print-doc (docs/special-doc sym))
                            (docs/repl-special-doc-map sym) (repl/print-doc (docs/repl-special-doc sym))
                            :else (repl/print-doc (get-var env sym)))))))

(defn process-pst
  [opts cb expr]
  (if-let [expr (or expr '*e)]
    (cljs/eval st
               expr
               (make-base-eval-opts! opts)
               (fn [res]
                 (let [[opts msg] (if res
                                    [(assoc opts :no-pr-str-on-value true) (common/extract-message res true true)]
                                    [opts res])]
                   (handle-eval-result! opts cb (common/wrap-success msg)))))
    (handle-eval-result! opts cb (common/wrap-success nil))))

(defn process-in-ns
  [opts cb ns-string]
  (cljs/eval
   st
   ns-string
   (make-base-eval-opts! opts)
   (fn [result]
     (if (and (map? result) (:error result))
       (handle-eval-result! opts cb result)
       (let [ns-symbol result]
         (when (:verbose opts)
           (debug-prn "in-ns argument is symbol? " (symbol? ns-symbol)))
         (if-not (symbol? ns-symbol)
           (handle-eval-result! opts cb
                                (common/error-argument-must-be-symbol "in-ns" ex-info-data))
           (if (some (partial = ns-symbol) (known-namespaces))
             (handle-eval-result! opts cb
                                  #(swap! app-env assoc :current-ns ns-symbol)
                                  (common/wrap-success nil))
             (let [ns-form `(~'ns ~ns-symbol)]
               (cljs/eval
                st
                ns-form
                (make-base-eval-opts! opts)
                (fn [error]
                  (handle-eval-result! opts
                                       cb
                                       #(swap! app-env assoc :current-ns ns-symbol)
                                       identity
                                       (if error
                                         (common/wrap-error error)
                                         (common/wrap-success nil)))))))))))))

(defn process-repl-special
  [opts cb expression-form]
  (let [env (assoc (ana/empty-env) :context :expr
                   :ns {:name (:current-ns @app-env)})
        argument (second expression-form)]
    (case (first expression-form)
      in-ns (process-in-ns opts cb argument)
      require (process-require opts cb :require (rest expression-form))
      require-macros (handle-eval-result! opts cb (common/error-keyword-not-supported "require-macros" ex-info-data)) ;; (process-require :require-macros identity (rest expression-form))
      import (process-require  opts cb :import (rest expression-form))
      doc (process-doc cb env argument)
      source (handle-eval-result! opts cb (common/error-keyword-not-supported "source" ex-info-data))                 ;; (println (fetch-source (get-var env argument)))
      pst (process-pst opts cb argument)
      load-file (handle-eval-result! opts cb (common/error-keyword-not-supported "load-file" ex-info-data)))))        ;; (process-load-file argument opts)

(defn process-1-2-3
  [expression-form value]
  (when-not (or ('#{*1 *2 *3 *e} expression-form)
                (ns-form? expression-form))
    (set! *3 *2)
    (set! *2 *1)
    (set! *1 value)))

;;;;;;;;;;;;;;;;;;;;
;;; External API ;;;
;;;;;;;;;;;;;;;;;;;;

(defn init-repl!
  "The init-repl function."
  [opts]
  (assert (= cljs.analyzer/*cljs-ns* 'cljs.user))
  (set! *target* "default")
  (swap! app-env merge (valid-opts opts))
  (set! (.. js/window -cljs -user) #js {}))

(defn update-to-initializing
  [old-app-env]
  (if (and (not (:initializing? old-app-env))
           (:needs-init? old-app-env))
    (assoc old-app-env :initializing? true)
    (assoc old-app-env :needs-init? false)))

(defn update-to-initialized
  [old-app-env]
  {:pre [(:needs-init? old-app-env) (:initializing? old-app-env)]}
  (merge old-app-env {:initializing? false
                      :needs-init? false}))

(defn init-repl-if-necessary!
  [opts cb]
  (when (:needs-init? (swap! app-env update-to-initializing))
    (try
      (do (init-repl! opts)
          (swap! app-env update-to-initialized))
      (catch :default e
        (handle-eval-result! opts cb (common/wrap-error (str "INIT - "
                                                             (common/extract-message e))))))))

(defn read-eval-call
  "Reads, evaluates and calls back with the evaluation result.

  The first parameter is a map of configuration options, currently
  supporting:

  * :verbose will enable the the evaluation logging, defaults to false.

  The second parameter cb, should be a 1-arity function which receives
  the result map.

  Therefore, given cb (fn [result] ...), the main map keys are:

  ```
  { :success? ;; a boolean indicating if everything went right
    :value    ;; (if (success? result)) will contain the actual yield of the evaluation
    :error    ;; (if (not (success? result)) will contain a js/Error }
  ```

  It initializes the repl harness if necessary."
  [opts cb source]
  (init-repl-if-necessary! opts cb)
  (try
    (let [expression-form (repl-read-string source)
          opts (valid-opts opts)]
      (binding [ana/*cljs-warning-handlers* [(partial custom-warning-handler opts cb)]]
        (if (docs/repl-special? expression-form)
          (process-repl-special opts cb expression-form)
          (cljs/eval-str st
                         source
                         source
                         ;; opts (map)
                         (merge (make-base-eval-opts!)
                                {:source-map false
                                 :def-emits-var true}
                                opts)
                         (fn [ret]
                           (when (:verbose opts)
                             (debug-prn "Evaluation returned: " ret))
                           (handle-eval-result! opts
                                                cb
                                                #(do (process-1-2-3 expression-form (:value ret))
                                                     (swap! app-env assoc :current-ns (:ns ret)))
                                                identity
                                                ret))))))
    (catch :default e
      (handle-eval-result! opts cb (common/wrap-error e)))))

(defn reset-env!
  "It dons the following (in order):

  1. remove the input namespaces from the compiler environment
  2. set *e to nil
  3. in-ns to cljs.user

  It accepts a sequence of symbols or strings."
  ([]
   (reset-env! nil))
  ([namespaces]
   (doseq [ns namespaces]
     (remove-ns st (symbol ns)))
   (read-eval-call {} identity "(set! *e nil)")
   (read-eval-call {} identity "(in-ns 'cljs.user)")))
