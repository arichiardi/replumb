(ns replumb.target.nodejs
  (:require [clojure.string :as string]))

(defn init-fn!
  []
  (set! (.. js/global -cljs -user) #js {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; File-based load-fn infrastructure ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- filename->lang
  "Converts a filename to a lang keyword by inspecting the file
  extension."
  [filename]
  (if (string/ends-with? filename ".js")
    :js
    :clj))

(defn- read-some
  "Reads the first filename in a sequence of supplied filenames,
  using a supplied read-file-fn, calling back upon first successful
  read, otherwise calling back with nil."
  [[filename & more-filenames] read-file-fn cb]
  (if filename
    (read-file-fn
     filename
     (fn [source]
       (if source
         (cb {:lang (filename->lang filename)
              :source source})
         (read-some more-filenames read-file-fn cb))))
    (cb nil)))

(defn- filenames-to-try
  "Produces a sequence of filenames to try reading, in the
  order they should be tried."
  [src-paths macros path]
  (let [extensions (if macros
                     [".clj" ".cljc"]
                     [".cljs" ".cljc" ".js"])]
    (for [extension extensions
          src-path src-paths]
      (str src-path "/" path extension))))

(defn make-load-fn
  "Makes a load function that will read from a sequence of src-paths
  using a supplied read-file-fn. It returns a cljs.js-compatible
  *load-fn*.

  Read-file-fn is a 2-arity function (fn [filename source-cb] ...) where
  source-cb is itself a function (fn [source] ...) that needs to be called
  with the source of the library (as string)."
  [src-paths read-file-fn]
  (fn [{:keys [macros path]} cb]
    (read-some (filenames-to-try src-paths macros path) read-file-fn cb)))
