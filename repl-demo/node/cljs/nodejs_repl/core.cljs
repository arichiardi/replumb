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

;; first arg is verbosity true/false
;; second arg is the cache path
;; third arg is the classpath string (: separated)

(defn -main [& args]
  (println args)
  (assert (= (count args) 3) "Only three params are supported (in order): verbose, cache path and classpath string.")
  (let [verbose? (= "true" (first args))
        cache-path (second args)
        classpath-string (nth args 2)
        opts (merge (replumb/options :nodejs
                                     (arg->src-paths classpath-string)
                                     io/read-file!)
                    {:verbose verbose?
                     :cache {:path cache-path}})]
    (print "Starting Node.js sample repl:\n"
           (find opts :target) "\n"
           (find opts :src-paths) "\n"
           (find opts :verbose) "\n"
           (find opts :cache))
    (read-eval-print-loop opts)))

(set! *main-cli-fn* -main)
