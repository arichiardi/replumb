(ns replumb.test-env
  (:require [replumb.core :as core]
            [replumb.browser.io :as io]))

(def ^:dynamic *src-paths* ["js/compiled/out"])

(def ^:dynamic *target-opts* (core/options :browser *src-paths* io/fetch-file!))

(def ^:dynamic *read-file-fn* io/fetch-file!)

(def ^:dynamic *write-file-fn* nil)

(def ^:dynamic *delete-file-fn* nil)

(def ^:dynamic *rename-file-fn* nil)

(def ^:dynamic *file-exists-fn* nil)
