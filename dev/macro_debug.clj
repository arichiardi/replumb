(ns ^{:doc
      "Thanks to Michael Bradley's following file we are able to debug macros in Clojurescript
       https://gist.github.com/michaelsbradleyjr/7509505"}
    macro-debug
  (:require [cljs.analyzer :as cljs]
            clojure.walk))

(declare ap
         cljs-macroexpand*
         cljs-macroexpand-1*
         cljs-macroexpand-all*

         cljs-macroexpand
         cljs-macroexpand-1
         cljs-macroexpand-all
         debug-*

         ->compiler-console
         ->js-log

         debug-*->*
         debug->cons
         debug-1->cons
         debug-all->cons
         debug->js
         debug-1->js
         debug-all->js
         defalias

         debug
         debug-1
         debug-all
         expand-symbol
         ppstring
         prccon)

(def ^:dynamic *out-fn* nil)

;; macros and supporting funcs for macro dev and debug in ClojureScript projects
;; -----------------------------------------------------------------------------

(defmacro ^{:private true} ap
  [f]
  `(apply ~f (conj 'args '&env)))

(defn cljs-macroexpand*
  [env form]
  (let [ex (cljs-macroexpand-1* env form)]
    (if (identical? ex form)
      form
      (recur env ex))))

(defn cljs-macroexpand-1*
  [env form]
  (cljs/macroexpand-1 env form))

(defn cljs-macroexpand-all*
  [env form]
  (clojure.walk/prewalk (fn [x] (if (seq? x) (cljs-macroexpand* env x) x)) form))

(defmacro cljs-macroexpand
  [& args]
  (ap cljs-macroexpand*))

(defmacro cljs-macroexpand-1
  [& args]
  (ap cljs-macroexpand-1*))

(defmacro cljs-macroexpand-all
  [& args]
  (ap cljs-macroexpand-all*))

(defn- debug-*
  [env which expander args]
  (let [args (if (< (count args) 2) (conj args nil) args)
        [descrip form] args
        expanded (expander env form)
        expstrng (str "\n"
                      (str "DESCRIPTION: " (or descrip "(no description supplied)"))
                      "\n"
                      "\n"
                      (str "BEFORE " which
                           "\n"
                           "\n"
                           (ppstring form))
                      "\n"
                      (str "AFTER " which
                           "\n"
                           "\n"
                           (ppstring expanded))
                      "\n")]
    (*out-fn* expstrng)))

(defn- ->compiler-console
  [val]
  `(do ~(prccon val)))

(defn- ->js-log
  [val]
  `(do (.log js/console ~val)))

(defmacro ^{:private true} debug-*->*
  [which out-fn]
  (list 'binding ['*out-fn* out-fn]
        (list 'debug-* '&env which (symbol (str "cljs-" which "*")) 'args)))

(defmacro debug->cons
  [& args]
  (debug-*->* "macroexpand" ->compiler-console))

(defmacro debug-1->cons
  [& args]
  (debug-*->* "macroexpand-1" ->compiler-console))

(defmacro debug-all->cons
  [& args]
  (debug-*->* "macroexpand-all" ->compiler-console))

(defmacro debug->js
  [& args]
  (debug-*->* "macroexpand" ->js-log))

(defmacro debug-1->js
  [& args]
  (debug-*->* "macroexpand-1" ->js-log))

(defmacro debug-all->js
  [& args]
  (debug-*->* "macroexpand-all" ->js-log))

(defmacro ^{:private true} defalias
  ([name orig]
   `(do
      (alter-meta!
       (if (.hasRoot (var ~orig))
         (def ~name (.getRawRoot (var ~orig)))
         (def ~name))
       #(conj (dissoc % :macro)
              (apply dissoc (meta (var ~orig)) (remove #{:macro} (keys %)))))
      (var ~name)))
  ([name orig doc]
   (list `defalias (with-meta name (assoc (meta name) :doc doc)) orig)))

(defalias debug debug->cons)

(defalias debug-1 debug-1->cons)

(defalias debug-all debug-all->cons)

(defn expand-symbol
  "Resolves a symbol to a namespaced symbol, relative to some environment as
  obtained with &env within a macro definition."
  [env sym]
  (:name (cljs/resolve-var env sym)))

(defn ppstring
  [v]
  (with-out-str (clojure.pprint/pprint v)))

(defn prccon
  "Prints to the compiler's console at compile time. Sprinkle as needed within
  macros and their supporting functions to facilitate debugging during
  development."
  [& args]
  (binding [*out* *err*]
    (apply println args)))
