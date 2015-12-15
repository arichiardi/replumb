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
  [file-name]
  (if (string/ends-with? file-name ".js")
    :js
    :clj))

(defn read-files-and-callback!
  "Loop on the file-names using a supplied read-file-fn (fn [file-name
  src-cb] ...), calling back cb upon first successful read, otherwise
  calling back with nil."
  [verbose? file-names read-file-fn load-fn-cb]
  ;; AR - Can't make this function tail recursive as it is now
  (if-let [name (first file-names)]
    (do (when verbose?
          (common/debug-prn "Reading" name "..."))
        (read-file-fn name (fn [source]
                             (if source
                               (load-fn-cb {:lang (filename->lang name)
                                            :source source})
                               (do
                                 (when verbose?
                                   (common/debug-prn "No source found..."))
                                 (read-files-and-callback! verbose? (rest file-names) read-file-fn load-fn-cb))))))
    (load-fn-cb nil)))

(defn file-paths-to-try
  "Produces a sequence of filenames to try reading, in the
  order they should be tried."
  [src-paths macros file-path]
  (let [extensions (if macros
                     [".clj" ".cljc"]
                     [".cljs" ".cljc" ".js"])]
    (for [extension extensions
          src-path src-paths]
      ;; AR - will there be a need for https://nodejs.org/docs/latest/api/path.html ?
      (str (common/normalize-path src-path) file-path extension))))

(defn file-paths-to-try-from-ns-symbol
  "Given the symbol of a namespace produces all possibile file names
  in which given ns could be found."
  [ns-sym src-paths]
  (let [without-extension (string/replace (string/replace (name ns-sym) #"\." "/") #"-" "_")]
    (file-paths-to-try src-paths false without-extension)))

(defn goog-file-paths-to-try
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
