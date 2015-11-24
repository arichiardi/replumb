(ns replumb.target
  (:require [replumb.load :as load]
            [replumb.target.browser :as browser]
            [replumb.target.nodejs :as nodejs]))

(defn default-opts
  "Given user provided options, returns the default option map
  its :target (string or keyword). Defaults to :default (browser,
  aka :js target).

  The user options should be validated beforehand according to
  replumb.repl/valid-opts-set."
  [user-opts]
  (let [def-opts (condp = (keyword (:target user-opts))
                   :nodejs nodejs/default-opts
                   browser/default-opts)]
    (merge def-opts {:init-fns (remove nil? (conj (:init-fns def-opts)
                                                  (:init-fn! user-opts)))
                     :load-fn! (or (:load-fn! user-opts)
                                   (if-let [read-file-fn (:read-file-fn! user-opts)]
                                     (load/make-load-fn (:src-paths user-opts) read-file-fn)))})))
