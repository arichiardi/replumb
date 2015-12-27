(ns replumb.load
  (:require [cljs.js :as cljs]
            [clojure.string :as string]
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
  [file-name]
  (if (string/ends-with? file-name ".js") :js :clj))

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
                                             :source source})
                                (do
                                  (when verbose?
                                    (common/debug-prn "No source found..."))
                                  (read-files-and-callback! verbose? (rest file-names) read-file-fn! load-fn-cb))))))
    (load-fn-cb nil)))

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

(defn file-paths-for-closure
  "Produces a sequence of filenames to try reading crafted for goog
  libraries, in the order they should be tried."
  [src-paths goog-path]
  (for [src-path src-paths]
    (str (common/normalize-path src-path) goog-path ".js")))

(defn skip-load?
  [{:keys [name macros]}]
  (or
   (= name 'cljs.core)
   (= name 'cljs.analyzer)
   (and (= name 'cljs.pprint) macros)
   (and (= name 'cljs.test) macros)
   (and (= name 'clojure.template) macros)))
