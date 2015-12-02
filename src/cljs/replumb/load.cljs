(ns replumb.load
  (:require [clojure.string :as string]
            [replumb.common :as common]))

(defn fake-load-fn!
  "This load function just calls:
  (cb {:lang   :js
       :source \"\"})"
  [_ cb]
  (cb {:lang   :js
       :source ""}))

(defn no-resource-load-fn!
  "Mimics \"Resource not found\" as it just calls: (cb nil)"
  [_ cb]
  (cb nil))

(defn filename->lang
  "Converts a filename to a lang keyword by inspecting the file
  extension."
  [filename]
  (if (string/ends-with? filename ".js")
    :js
    :clj))

(defn read-files-and-callback!
  "Loop on the file-names using a supplied read-file-fn (fn [file-name
  src-cb] ...), calling back cb upon first successful read, otherwise
  calling back with nil."
  [verbose? file-names read-file-fn cb]
  (loop [names file-names]
    (if (seq names)
      (let [name (first names)]
        (when verbose?
          (common/debug-prn "Reading" name "..."))
        (read-file-fn name (fn [source]
                             (if source
                               (cb {:lang (filename->lang name)
                                    :source source})
                               (recur (rest names))))))
      (cb nil))))

(defn filenames-to-try
  "Produces a sequence of filenames to try reading, in the
  order they should be tried."
  [src-paths macros path]
  (let [extensions (if macros
                     [".clj" ".cljc"]
                     [".cljs" ".cljc" ".js"])]
    (for [extension extensions
          src-path src-paths]
      ;; AR - will there be a need for
      ;; https://nodejs.org/docs/latest/api/path.html ?
      (str src-path (when-not (= "/" (last src-path)) "/") path extension))))

;; AR - just for reference
;; (defn load-and-callback!
;;   [path lang cache-prefix cb]
;;   (let [[raw-load [source modified]] [js/PLANCK_LOAD (js/PLANCK_LOAD path)]
;;         [raw-load [source modified]] (if source
;;                                        [raw-load [source modified]]
;;                                        [js/PLANCK_READ_FILE (js/PLANCK_READ_FILE path)])]
;;     (when source
;;       (cb (merge
;;            {:lang   lang
;;             :source source}
;;            (when-not (= :js lang)
;;              (cached-callback-data path cache-prefix source modified raw-load))))
;;       :loaded)))

;; AR - TODO build closure index
;; (defn build-closure-index!
;;   [path-to-goog-deps read-file-fn]
;;   (let [paths-to-provides
;;         (map (fn [[_ path provides]]
;;                [path (map second
;;                           (re-seq #"'(.*?)'" provides))])
;;              (re-seq #"\ngoog\.addDependency\('(.*)', \[(.*?)\].*"
;;                      (first (js/PLANCK_LOAD "goog/deps.js"))))]
;;     (into {}
;;           (for [[path provides] paths-to-provides
;;                 provide provides]
;;             [(symbol provide) (str "goog/" (second (re-find #"(.*)\.js$" path)))]))))

;; (def closure-index-mem (memoize closure-index))

;; (defn- do-load-goog
;;   [name cb]
;;   (if-let [goog-path (get (build-closure-index-mem) name)]
;;     (when-not (load-and-callback! (str goog-path ".js") :js nil cb)
;;       (cb nil))
;;     (cb nil)))

(defn skip-load?
  [{:keys [name macros]}]
  (or
   (= name 'cljs.core)
   (= name 'cljs.analyzer)
   (and (= name 'cljs.pprint) macros)
   (and (= name 'cljs.test) macros)
   (and (= name 'clojure.template) macros)))

(defn make-load-fn
  "Makes a load function that will read from a sequence of src-paths
  using a supplied read-file-fn. It returns a cljs.js-compatible
  *load-fn*.

  Read-file-fn is an async 2-arity function (fn [filename src-cb] ...)
  where src-cb is itself a function (fn [source] ...) that needs to be
  called with the full source of the library (as string)."
  [verbose? src-paths read-file-fn]
  (fn [{:keys [name macros path] :as load-map} cb]
    (cond
      (skip-load? load-map) (fake-load-fn! load-map cb)
      ;; (re-matches #"^goog/.*" path) (do-load-goog name cb) ;; AR - handle goog
      :else (read-files-and-callback! verbose?
                                      (filenames-to-try src-paths macros path)
                                      read-file-fn
                                      cb))))
