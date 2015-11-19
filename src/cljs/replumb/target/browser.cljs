(ns replumb.target.browser)

(defn init-fn!
  []
  (set! (.. js/window -cljs -user) #js {}))

(defn load-fn!
  "This load function just calls: (cb nil)"
  [_ cb]
  (cb nil))
