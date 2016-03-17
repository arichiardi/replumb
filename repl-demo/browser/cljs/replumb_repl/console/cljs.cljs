(ns replumb-repl.console.cljs
  (:require [reagent.core :as reagent]
            [replumb.core :as replumb]
            [replumb.browser.io :as io]
            [replumb.load :as load]
            [replumb-repl.app :as app]
            [replumb-repl.console :as console]))

(defn handle-result!
  [console result]
  (let [write-fn (if (replumb/success? result) console/write-return! console/write-exception!)]
    (write-fn console (replumb/unwrap-result result))))

(defn cljs-read-eval-print!
  [console repl-opts line]
  (try
    (replumb/read-eval-call repl-opts (partial handle-result! console) line)
    (catch js/Error err
      (println "Caught js/Error during read-eval-print: " err)
      (console/write-exception! console err))))

(defn cljs-console-prompt!
  [console repl-opts]
  (doto console
    (.Prompt true (fn [input]
                    (cljs-read-eval-print! console repl-opts input)
                    (.SetPromptLabel console (replumb/get-prompt)) ;; necessary for namespace changes
                    (cljs-console-prompt! console repl-opts)))))

(defn cljs-console-did-mount
  [console-opts]
  (js/$
   (fn []
     (let [repl-opts (merge (replumb/options :browser
                                             ["/src/cljs" "/js/compiled/out"]
                                             io/fetch-file!)
                            {:warning-as-error true
                             :verbose true})
           jqconsole (console/new-jqconsole "#cljs-console"
                                            (merge {:prompt-label (replumb/get-prompt)
                                                    :disable-auto-focus false}
                                                   console-opts))]
       (app/add-console! :cljs-console jqconsole)
       (cljs-console-prompt! jqconsole repl-opts)))))

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
