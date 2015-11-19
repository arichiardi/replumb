(ns replumb-repl.core
  (:require [reagent.core :as reagent]
            [replumb-repl.console.cljs :as cljs]))

(enable-console-print!)

(defn page []
  [:div
   [cljs/cljs-component]])

(defn ^:export main []
  (println "In replumb-repl.core/main")
  (reagent/render [page]
                  (.getElementById js/document "app")))
