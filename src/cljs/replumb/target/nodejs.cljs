(ns replumb.target.nodejs)

(defn init-fn!
  []
  (set! (.. js/global -cljs -user) #js {}))

(def default-opts
  "Node.js default set of options for read-eval-call.
  It is intentionally missing :load-fn! that will need to be added
  before calling read-eval-call. See nodejs-opts."
  {:target :nodejs
   :init-fns [init-fn!]})
