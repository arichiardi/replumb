(ns replumb.load)

;; From mfikes/planck
;; For now there is no load from file

(defn js-load
  [{:keys [name macros path file] :as full} cb]
  ;; (cond
  ;; (skip-load? full) (cb {:lang   :js
  ;; :source ""})
  ;; file (do-load-file file cb)
  ;; (re-matches #"^goog/.*" path) (do-load-goog name cb)
  ;; :else (do-load-other path macros cb))
  (cb {:lang   :js
       :source ""}))
