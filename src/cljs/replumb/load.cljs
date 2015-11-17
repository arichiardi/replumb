(ns replumb.load)

;; From mfikes/planck
;; For now there is no load from file

(defn js-default-load
  "This load function just calls: (cb nil)"
  [_ cb]
  (cb nil))

(defn js-fake-load
  "This load function just calls:
  (cb {:lang   :js
       :source \"\"})"
  [_ cb]
  (cb {:lang   :js
       :source ""}))
