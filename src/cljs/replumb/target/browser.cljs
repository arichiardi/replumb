(ns replumb.target.browser)

(defn init-fn!
  []
  (set! (.. js/window -cljs -user) #js {}))

(def default-opts
  "Browser default set of options for read-eval-call."
  {:target :default
   :init-fns [init-fn!]})
