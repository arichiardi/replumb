(ns replumb.node
  "Utilities for operating within Node.js"
  (:require [replumb.core :as replumb]
            [replumb.load :as load]
            [cljs.nodejs :as nodejs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; File-based load-fn ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(def fs (nodejs/require "fs"))

(defn- node-read-file
  "Accepts a filename to read and a callback. Upon success, invokes
  callback with the source. Otherwise invokes the callback with nil."
  [filename cb]
  (.readFile fs filename "utf-8"
    (fn [err source]
      (cb (when-not err
            source)))))

(defn make-load-fn
  "Makes a load-fn for use within Node.js that will search for source
  files in a supplied sequence of src-paths."
  [src-paths]
  (load/make-load-fn src-paths node-read-file))

;;;;;;;;;;;;;;;;;;;
;;; Simple REPL ;;;
;;;;;;;;;;;;;;;;;;;

(defn read-eval-print-loop
  [load-fn]
  (let [node-readline (nodejs/require "readline")
        rl (.createInterface node-readline
             #js {:input  (.-stdin js/process)
                  :output (.-stdout js/process)})]
    (doto rl
      (.setPrompt (replumb/get-prompt))
      (.on "line"
        (fn [cmd]
          (replumb/read-eval-call
            {:verbose  false
             :load-fn! load-fn}
            (fn [res]
              (-> res
                replumb/result->string
                println)
              (.setPrompt rl (replumb/get-prompt))
              (.prompt rl))
            cmd)))
      (.prompt))))

(comment
  (read-eval-print-loop (make-load-fn ["src" "/foo/src2"]))
  )
