(ns replumb.load
  (:require goog.Promise
            [cljs.js :as cljs]
            [clojure.string :as string]
            [replumb.common :as common]
            [replumb.cache :as cache]
            [cljs.reader :as edn]))

(def loaded-js-set
  "A set containing namespaces already loaded."
  '#{cljs.analyzer
     cljs.compiler
     cljs.env
     cljs.reader
     cljs.source-map
     cljs.source-map.base64
     cljs.source-map.base64-vlq
     cljs.stacktrace
     cljs.tagged-literals
     cljs.tools.reader.impl.utils
     cljs.tools.reader.reader-types
     clojure.set
     clojure.string
     cognitect.transit})

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
  [file-name]
  (if (string/ends-with? file-name ".js") :js :clj))

(defn read-cache-source
  "Read the cache source depending on whether is a edn or json file"
  [cache-path cache-source]
  (if (string/ends-with? cache-path ".edn")
    (edn/read-string cache-source)
    (cache/transit-json->cljs cache-source)))

(defn extensions
  "Returns the correct file extensions to try (no dot prefix), following
  the cljs.js/*load-fn* docstring."
  ([]
   (extensions false))
  ([macros]
   (if macros ["clj" "cljc"] ["cljs" "cljc" "js"])))

(defn read-files-and-callback!
  "Loop on the file-names using a supplied read-file-fn (fn [file-name
  src-cb] ...), calling back cb upon first successful read, otherwise
  calling back with nil.
  This function does not check whether parameters are nil, please do it
  in the caller."
  [verbose? file-names read-file-fn! load-fn-cb]
  ;; AR - Can't make this function tail recursive as it is now
  (if-let [name (first file-names)]
    (do (when verbose?
          (common/debug-prn "Reading" name "..."))
        (read-file-fn! name (fn [source]
                              (if source
                                (load-fn-cb {:lang (filename->lang name)
                                             :source source
                                             :file name})
                                (do
                                  (when verbose?
                                    (common/debug-prn "No source found..."))
                                  (read-files-and-callback! verbose? (rest file-names) read-file-fn! load-fn-cb))))))
    (load-fn-cb nil)))

(defn read-cache-file
  [{:keys [verbose? read-file-fn! js-path cache-path try-next-files-pair-fn load-fn-cb]} js-source]
  (read-file-fn! cache-path
                 (fn [cache-source]
                   (if cache-source
                     (do
                       (when verbose?
                         (common/debug-prn (str "Retrieved JavaScript from: "
                                                (if js-source js-path "<skipped>")))
                         (common/debug-prn (str "Retrieved cache file from: " cache-path)))
                       (load-fn-cb {:lang (filename->lang js-path)
                                    :source js-source
                                    :cache (read-cache-source cache-path cache-source) }))
                     (try-next-files-pair-fn)))))

(defn read-js-file
  [{:keys [verbose? read-file-fn! js-path cache-path try-next-files-pair-fn] :as opts} read-cache-file-fn]
  (read-file-fn! js-path
                 (fn [js-source]
                   (if (and js-source (cache/cached-js-valid? js-source))
                     (do
                       (when verbose?
                         (common/debug-prn "Reading" cache-path "..."))
                       (read-cache-file-fn opts js-source))
                      (try-next-files-pair-fn)))))

(defn read-files-from-cache-and-callback!
  "Loops over cached-file-names in order to retrieve them. It needs to find
  both the related .js file and .cache.json file, otherwise keeps looping.
  If it does not find the cached files calls read-files-and-callback! and
  tries to load the unevaluated ones.
  This function does not check whether parameters are nil, please do it in
  the caller."
  [verbose? file-names read-file-fn! load-fn-cb cached-file-names name]
  (if-let [[js-path cache-path] (first cached-file-names)]
    (let [try-next-files-pair #(read-files-from-cache-and-callback! verbose?
                                                                    file-names
                                                                    read-file-fn!
                                                                    load-fn-cb
                                                                    (rest cached-file-names)
                                                                    name)
          cache-opts {:verbose? verbose?
                      :read-file-fn! read-file-fn!
                      :load-fn-cb load-fn-cb
                      :js-path js-path
                      :cache-path cache-path
                      :try-next-files-pair-fn try-next-files-pair}]
      (if (contains? loaded-js-set name)
        (do
          (when verbose?
            (common/debug-prn "Skipping js loading for " name))
          (read-cache-file cache-opts nil))
        (do
          (when verbose?
            (common/debug-prn "Reading" js-path "..."))
          (read-js-file cache-opts read-cache-file))))
    (do
      (when verbose?
        (common/debug-prn "Cannot load cache files..."))
      (read-files-and-callback! verbose? file-names read-file-fn! load-fn-cb))))

(defn file-paths
  "Produces a sequence of file paths based on src-paths and file-path (a
  path already including one or more \"/\" and an extension)."
  [src-paths file-path]
  (for [src-path src-paths]
    (str (common/normalize-path src-path) file-path)))

(defn file-paths-for-load-fn
  "Produces a sequence of file names to try reading from src-paths and
  file-path-without-ext (it should already include one or more
  \"/\"). The right order and extension is taken from cljs.js/*load-fn*
  docstring and takes into consideration the macros parameter."
  [src-paths macros file-path-without-ext]
  (for [extension (extensions macros)
        src-path (file-paths src-paths file-path-without-ext)]
    (str src-path "." extension)))

(defn cache-file-paths-for-load-fn
  "Produces a sequence of pairs containing the file paths to try reading for
  evaluation caching.
  The first file is always a \".js\" file while the second is the cache file
  and can be a \".json\" or \".edn\" file."
  [cache-paths macros file-path-without-ext]
  (for [extension (cons "" (map #(str "." %) (extensions macros)))
        ;; try both json and edn files
        cache-extension [".cache.json" ".cache.edn"]
        ;; try both eg. clojure/set and clojure_SLASH_set
        src-path (into (file-paths cache-paths file-path-without-ext)
                       (file-paths cache-paths
                                   (cache/cache-prefix-for-path file-path-without-ext macros)))]
    [(str src-path ".js" ) (str src-path extension cache-extension)]))

(defn file-paths-for-closure
  "Produces a sequence of file paths to try reading crafted for goog
  libraries, in the order they should be tried."
  [src-paths goog-path]
  (for [src-path src-paths]
    (str (common/normalize-path src-path) goog-path ".js")))

(defn goog-deps-map
  "Given the content of goog/deps.js file, create a map
  provide->path (without extension) of Google dependencies.

  Adapted from planck:
  https://github.com/mfikes/planck/blob/master/planck-cljs/src/planck/repl.cljs#L438-L451"
  [deps-js-content]
  (let [paths-to-provides (map (fn [[_ path provides]]
                                 [path (map second (re-seq #"'(.*?)'" provides))])
                               (re-seq #"\ngoog\.addDependency\('(.*)', \[(.*?)\].*"
                                       deps-js-content))]
    (into {} (for [[path provides] paths-to-provides
                   provide provides]
               [(symbol provide) (str "goog/" (second (re-find #"(.*)\.js$" path)))]))))

(defn read-goog-file-promise
  "Return a promise that resolves with the result of accumulating the
  actual call to replumb.repl/read-eval-call on the source with results."
  [verbose? read-file-fn! path results]
  {:pre [(map? results)]}
  (goog.Promise.
   (fn [resolve _]
     (let [deps-path (str (common/normalize-path path) "goog/deps.js")]
       (read-file-fn! deps-path
                      (fn [content]
                        (resolve (if content
                                   (do (when verbose?
                                         (common/debug-prn "Found valid" deps-path))
                                       (merge results (goog-deps-map content)))
                                   results))))))))

(defn goog-closure-index-promise!
  "Return a promise containing a map Google Closure symbol -> file
  path (string without extension) as in:

  {goog.a11y.aria.DropEffectValues \"goog/a11y/aria/attributes\"
   goog.labs.i18n.ListFormatSymbols_en_BW \"goog/labs/i18n/listsymbolsext\"
   ...}

  It merges maps if many deps.js are on the source path, precedence to
  the rightmost (as per merge)."
  [verbose? src-paths read-file-fn!]
  (let [read-promise (partial read-goog-file-promise verbose? read-file-fn!)]
    (if read-file-fn!
      (do (when verbose?
            (common/debug-prn "Discovering goog/deps.js in" src-paths))
          (reduce (fn [pmses path]
                    (.then pmses
                           #(read-promise path %)
                           #(read-promise path {}))) ;; AR - if there was an Error, we re-start from {}
                  (goog.Promise.resolve {})
                  src-paths))
      (do (when verbose?
            (common/debug-prn "No :read-file-fn! provided, skipping goog/deps.js discovering..."))
          (goog.Promise.resolve {})))))

(defn skip-load?
  [{:keys [name macros]}]
  (or
   (= name 'cljs.core)
   (= name 'cljs.analyzer)
   (and (= name 'cljs.pprint) macros)
   (and (= name 'cljs.test) macros)
   (and (= name 'clojure.template) macros)))
