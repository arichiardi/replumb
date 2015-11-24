(ns nodejs-repl.core
  "Utilities for operating within Node.js"
  (:require [cljs.nodejs :as nodejs]
            [clojure.string :as string]
            [replumb.core :as replumb]
            [replumb.target.nodejs.io :as io]))

(nodejs/enable-util-print!)

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
              (replumb/nodejs-options src-paths io/read-file!)
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
