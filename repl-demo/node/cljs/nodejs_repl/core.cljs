(ns nodejs-repl.core
  "Utilities for operating within Node.js"
  (:require [cljs.nodejs :as nodejs]
            [clojure.string :as string]
            [replumb.core :as replumb]))

(nodejs/enable-util-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;
;;; File system stuff ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def require-fs #(nodejs/require "fs"))

(defn- node-read-file
  "Accepts a filename to read and a callback. Upon success, invokes
  callback with the source. Otherwise invokes the callback with nil."
  [filename source-cb]
  (.readFile (require-fs) filename "utf-8"
             (fn [err source]
               (source-cb (when-not err
                            source)))))

;;;;;;;;;;;;;;;;;;;
;;; Simple REPL ;;;
;;;;;;;;;;;;;;;;;;;

(defn read-eval-print-loop
  [src-paths]
  (let [node-readline (nodejs/require "readline")
        rl (.createInterface node-readline
                             #js {:input  (.-stdin js/process)
                                  :output (.-stdout js/process)})]
    (doto rl
      (.setPrompt (replumb/get-prompt))
      (.on "line"
           (fn [cmd]
             (replumb/read-eval-call
              (replumb/nodejs-options src-paths node-read-file)
              (fn [res]
                (-> res
                    replumb/result->string
                    println)
                (.setPrompt rl (replumb/get-prompt))
                (.prompt rl))
              cmd)))
      (.prompt))))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Main, from mfikes/elbow ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn arg->src-paths
  [arg]
  (string/split arg #":"))

(defn print-usage
  []
  (println "Usage:\n\nnode dev-resources/private/node/compiled/nodejs-repl.js src-path1:src-path2:src-path3"))

(defn -main [& args]
  (if (empty? args)
    (print-usage)
    (read-eval-print-loop (arg->src-paths (first args)))))

(set! *main-cli-fn* -main)
