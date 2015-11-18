(ns replumb-repl.app
  (:require [reagent.core :as reagent]))

(def initial-state {:consoles {}})

(defonce app-state (reagent/atom initial-state))

(defn reset-state!
  "Reset the app state."
  []
  (reset! app-state initial-state))

(defn add-console!
  "Add a new console instance to the app state."
  [key instance]
  (swap! app-state assoc-in [:consoles (name key)] instance))

(defn console
  "Given a console key, returns its instance or nil if not found."
  [key]
  (get-in @app-state [:consoles (name key)]))

(def console-created? "Was the console created? Returns a truey or falsey value."
  console)
