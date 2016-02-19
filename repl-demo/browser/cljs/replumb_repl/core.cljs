(ns replumb-repl.core
  (:require [reagent.core :as reagent]
            [devtools.core :as devtools]
            [replumb-repl.console.cljs :as cljs]))

(devtools/enable-feature! :sanity-hints :dirac)
(devtools/install!)

(enable-console-print!)

(set! *print-fn* (fn [& args]
                   (.apply (.-debug js/console) js/console (into-array args))))

(defn page []
  [:div
   [cljs/cljs-component]])

(defn ^:export main []
  (println "In replumb-repl.core/main")
  (reagent/render [page]
                  (.getElementById js/document "app")))
