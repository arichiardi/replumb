(ns nodejs-repl.core
  "Utilities for operating within Node.js"
  (:require [cljs.nodejs :as nodejs]
            [clojure.string :as string]
            [replumb.core :as replumb]
            [replumb.nodejs.io :as io]))

(nodejs/enable-util-print!)

;;;;;;;;;;;;;;;;;;;
;;; Simple REPL ;;;
;;;;;;;;;;;;;;;;;;;

(defn read-eval-print-loop
  [opts]
  (let [node-readline (nodejs/require "readline")
        rl (.createInterface node-readline
                             #js {:input  (.-stdin js/process)
                                  :output (.-stdout js/process)})]
    (doto rl
      (.setPrompt (replumb/get-prompt))
      (.on "line"
           (fn [cmd]
             (replumb/read-eval-call
              opts
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
  (println "Usage:\n\nnode path-to/nodejs-repl.js [--verbose] src-path1:src-path2:src-path3"))

(defn -main [& args]
  (println args)
  (if (or (empty? args) (> (count args) 2))
    (print-usage)
    (let [verbose? (= "--verbose" (first args))
          opts (merge (replumb/options :nodejs
                                       (arg->src-paths (if-not verbose?
                                                         (first args)
                                                         (second args)))
                                       io/read-file!)
                      {:verbose verbose?})]
      (print "Starting Node.js sample repl:\n"
             (find opts :target) "\n"
             (find opts :src-paths) "\n"
             (find opts :verbose))
      (read-eval-print-loop opts))))

(set! *main-cli-fn* -main)
