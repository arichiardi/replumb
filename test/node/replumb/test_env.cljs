(ns replumb.test-env
  (:require [replumb.core :as core]
            [replumb.nodejs.io :as io]))

(def ^:dynamic *src-paths* ["dev-resources/private/test/node/compiled/out"
                            "dev-resources/private/test/src/cljs"
                            "dev-resources/private/test/src/clj"
                            "dev-resources/private/test/src/cljc"
                            "dev-resources/private/test/src/js"])

(def ^:dynamic *target-opts* (core/options :nodejs *src-paths* io/read-file!))

(def ^:dynamic *read-file-fn* io/read-file!)

(def ^:dynamic *write-file-fn* io/write-file!)

(def ^:dynamic *delete-file-fn* io/delete-file!)

(def ^:dynamic *rename-file-fn* io/safely-rename-file!)

(def ^:dynamic *file-exists-fn* io/file-exists?)
