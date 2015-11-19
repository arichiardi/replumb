(ns replumb-repl.console.cljs
  (:require [reagent.core :as reagent]
            [replumb.core :as replumb]
            [replumb-repl.app :as app]
            [replumb-repl.console :as console]))

(defn handle-result!
  [console result]
  (let [write-fn (if (replumb/success? result) console/write-return! console/write-exception!)]
    (write-fn console (replumb/unwrap-result result))))

(defn cljs-read-eval-print!
  [console line]
  (try
    (replumb/read-eval-call (partial handle-result! console) line)
    (catch js/Error err
      (println "Caught js/Error during read-eval-print: " err)
      (console/write-exception! console err))))

(defn cljs-console-prompt!
  [console]
  (doto console
    (.Prompt true (fn [input]
                    (cljs-read-eval-print! console input)
                    (.SetPromptLabel console (replumb/get-prompt)) ;; necessary for namespace changes
                    (cljs-console-prompt! console)))))

(defn cljs-console-did-mount
  [console-opts]
  (js/$
   (fn []
     (let [jqconsole (console/new-jqconsole "#cljs-console"
                                            (merge {:prompt-label (replumb/get-prompt)
                                                    :disable-auto-focus true}
                                                   console-opts))]
       (app/add-console! :cljs-console jqconsole)
       (cljs-console-prompt! jqconsole)))))

(defn cljs-console-render []
  [:div.console-container
   [:div#cljs-console.console.cljs-console]])

(defn cljs-component
  "Creates a new instance of e which loads on the input
  selector (any jQuery selector will work) and configuration. The
  options are passed as named parameters and follow the jq-console ones.

  * :welcome-string is the string to be shown when the terminal is first
    rendered. Defaults to nil.
  * :prompt-label is the label to be shown before the input when using
    Prompt(). Defaults to namespace=>.
  * :continue-label is the label to be shown before the continued lines
    of the input when using Prompt(). Defaults to nil.
  * :disable-auto-focus is a boolean indicating whether we should disable
    the default auto-focus behavior. Defaults to true, the console never
    takes focus."
  []
  (fn [console-opts]
    (println "Building ClojureScript React component")
    (reagent/create-class {:display-name "cljs-console-component"
                           :reagent-render cljs-console-render
                           :component-did-mount #(cljs-console-did-mount console-opts)})))
