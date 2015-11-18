(ns replumb.target
  (:require [replumb.target.browser :as browser]
            [replumb.target.nodejs :as nodejs]))

(def browser-default-opts
  "Browser default set of options for read-eval-call."
  {:target :default
   :load-fn! browser/load-fn!
   :init-fns [browser/init-fn!]})

(def nodejs-default-opts
  "Node.js default set of options for read-eval-call.
  It is intentionally missing :load-fn! that will need to be added
  before calling read-eval-call. See nodejs-opts."
  {:target :nodejs
   :init-fns [nodejs/init-fn!]})

(defn default-opts
  "Given a target (string or keyword), returns the default option map
  for it. The no-arity version returns the browser default options."
  ([]
   (default-opts :default))
  ([target]
   (condp = (keyword target)
     :nodejs nodejs-default-opts
     browser-default-opts)))

(defn fake-load-fn!
  "This load function just calls:
  (cb {:lang   :js
       :source \"\"})"
  [_ cb]
  (cb {:lang   :js
       :source ""}))
