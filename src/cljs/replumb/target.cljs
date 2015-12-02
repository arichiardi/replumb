(ns replumb.target
  (:require [replumb.load :as load]
            [replumb.target.browser :as browser]
            [replumb.target.nodejs :as nodejs]))

(defn default-opts
  "Given user provided options, returns the default option map for
  its :target (string or keyword). Defaults to :default (browser,
  aka :js target)."
  [user-opts]
  (condp = (keyword (:target user-opts))
    :nodejs nodejs/default-opts
    browser/default-opts))
