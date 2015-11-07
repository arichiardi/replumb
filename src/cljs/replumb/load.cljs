(ns replumb.load)

;; From mfikes/planck
;; For now there is no load from file

(defn js-default-load
  "This load function just calls:
  (cb {:lang   :js
       :source \"\"})"
  [{:keys [name macros path file] :as full} cb]
  (cb {:lang   :js
       :source ""}))
