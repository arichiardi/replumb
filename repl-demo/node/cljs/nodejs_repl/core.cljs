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

(defn arg->src-paths
  [arg]
  (string/split arg #":"))

(def in-memory-cache (atom {}))

(defn repl-read-fn!
  [file-path src-cb]
  (if-let [cache-data (get @in-memory-cache file-path)]
    (do
      (if cache-data
        (println "Hit" file-path "from in-memory cache.")
        (println "Miss" file-path "from in-memory cache."))
      (src-cb cache-data))
    ;; little dance, better to use .readFileSync directly here
    (io/read-file!
     file-path
     (fn [disk-data]
       (do (swap! in-memory-cache assoc file-path disk-data)
           (src-cb disk-data))))))

(defn repl-write-fn!
  [file-path data]
  (swap! in-memory-cache assoc file-path data)
  (println "Stored" file-path "in in-memory cache."))

;;;;;;;;;;;;
;;; Main ;;;
;;;;;;;;;;;;

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
                                     repl-read-fn!
                                     repl-write-fn!)
                    {:verbose verbose?
                     :cache {:path cache-path}})]
    (print "Starting Node.js sample repl:\n"
           (find opts :target) "\n"
           (find opts :src-paths) "\n"
           (find opts :verbose) "\n"
           (find opts :cache))
    (read-eval-print-loop opts)))

(set! *main-cli-fn* -main)
