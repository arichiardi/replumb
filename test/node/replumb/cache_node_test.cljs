(ns replumb.cache-node-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.load :as load]
            [replumb.nodejs.io :as io]))

(defn rename-file-names-to-tmp
  [paths]
  (doseq [path paths]
    (io/safely-rename-file! path (str path ".tmp"))
    (is (false? (io/file-exists? path)) (str path " should not exist"))))

(defn revert-file-names
  [paths]
  (doseq [path paths]
    (let [tmp-file (str path ".tmp")]
      (io/safely-rename-file! tmp-file (apply str (drop-last 4 tmp-file))))))

(let [src-paths ["dev-resources/private/test/node/compiled/out"]
      cache-path "dev-resources/private/test/cache"
      path "clojure/set"
      macros false
      require-expr "(require 'clojure.set :reload)"
      test-expr "(doc clojure.set)"
      set-js-path (str (common/normalize-path cache-path) (munge path) ".js")
      set-json-path (str (common/normalize-path cache-path) (munge path) ".cache.json")
      fns-from-cache-opts (fn [cache-opts]
                            (let [target-opts (merge (options :nodejs src-paths io/read-file! io/write-file!) cache-opts)
                                  validated-echo-cb (partial repl/validated-call-back! target-opts echo-callback)
                                  read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)
                                  load-fn (repl/make-load-fn target-opts)]
                              {:load-fn #(load-fn {:macros macros :path path} identity)
                               :read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)
                               :reset-env! (partial repl/reset-env! target-opts)}))]

  (deftest read-no-files-in-cache-path-no-src-paths
    (rename-file-names-to-tmp [set-js-path set-json-path])
    (rename-file-names-to-tmp (flatten (load/cache-file-paths-for-load-fn src-paths macros path)))
    (let [cache-opts {}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache."))

    (let [cache-opts {:cache {:path cache-path}}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache."))

    (let [cache-opts {:cache {:src-paths-lookup? true}}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache."))

    (let [cache-opts {:cache {:path cache-path :src-paths-lookup? true}}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache.")))

  (deftest read-no-files-in-cache-path-yes-src-paths
    (revert-file-names (flatten (load/cache-file-paths-for-load-fn src-paths macros path)))
    (let [cache-opts {}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache."))

    (let [cache-opts {:cache {:path cache-path}}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache."))

    (let [cache-opts {:cache {:src-paths-lookup? true}}
          {:keys [load-fn read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)
          res (do (read-eval-call require-expr)
                  (read-eval-call test-expr))
          out (unwrap-result res)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :js) "The resulting language should be js")
      (is (contains? load-res :cache) "The resulting map should contain :cache.")
      (is (success? res) (str test-expr " should succeed"))
      (is (valid-eval-result? out) (str test-expr " should be a valid result"))
      (is (not (re-find #"nil" out)) (str test-expr " should not return nil"))
      (reset-env! '[clojure.set]))

    (let [cache-opts {:cache {:path cache-path :src-paths-lookup? true}}
          {:keys [load-fn read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)
          res (do (read-eval-call require-expr)
                  (read-eval-call test-expr))
          out (unwrap-result res)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :js) "The resulting language should be js")
      (is (contains? load-res :cache) "The resulting map should contain :cache.")
      (is (success? res) (str test-expr " should succeed"))
      (is (valid-eval-result? out) (str test-expr " should be a valid result"))
      (is (not (re-find #"nil" out)) (str test-expr " should not return nil"))
      (reset-env! '[clojure.set])))

  (deftest read-yes-files-in-cache-path-yes-src-paths
    (revert-file-names [set-js-path set-json-path])
    (let [cache-opts {}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache."))

    (let [cache-opts {:cache {:path cache-path}}
          {:keys [load-fn read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)
          res (do (read-eval-call require-expr)
                  (read-eval-call test-expr))
          out (unwrap-result res)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :js) "The resulting language should be js")
      (is (contains? load-res :cache) "The resulting map should contain :cache.")
      (is (success? res) (str test-expr " should succeed"))
      (is (valid-eval-result? out) (str test-expr " should be a valid result"))
      (is (not (re-find #"nil" out)) (str test-expr " should not return nil"))
      (reset-env! '[clojure.set]))

    (let [cache-opts {:cache {:src-paths-lookup? true}}
          {:keys [load-fn read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)
          res (do (read-eval-call require-expr)
                  (read-eval-call test-expr))
          out (unwrap-result res)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :js) "The resulting language should be js")
      (is (contains? load-res :cache) "The resulting map should contain :cache.")
      (is (success? res) (str test-expr " should succeed"))
      (is (valid-eval-result? out) (str test-expr " should be a valid result"))
      (is (not (re-find #"nil" out)) (str test-expr " should not return nil"))
      (reset-env! '[clojure.set]))

    (let [cache-opts {:cache {:path cache-path :src-paths-lookup? true}}
          {:keys [load-fn read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)
          res (do (read-eval-call require-expr)
                  (read-eval-call test-expr))
          out (unwrap-result res)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :js) "The resulting language should be js")
      (is (contains? load-res :cache) "The resulting map should contain :cache.")
      (is (success? res) (str test-expr " should succeed"))
      (is (valid-eval-result? out) (str test-expr " should be a valid result"))
      (is (not (re-find #"nil" out)) (str test-expr " should not return nil"))
      (reset-env! '[clojure.set])))

  (deftest read-yes-files-in-cache-path-no-src-paths
    (rename-file-names-to-tmp (flatten (load/cache-file-paths-for-load-fn src-paths macros path)))
    (let [cache-opts {}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache."))

    (let [cache-opts {:cache {:path cache-path}}
          {:keys [load-fn read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)
          res (do (read-eval-call require-expr)
                  (read-eval-call test-expr))
          out (unwrap-result res)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :js) "The resulting language should be js")
      (is (contains? load-res :cache) "The resulting map should contain :cache.")
      (is (success? res) (str test-expr " should succeed"))
      (is (valid-eval-result? out) (str test-expr " should be a valid result"))
      (is (not (re-find #"nil" out)) (str test-expr " should not return nil"))
      (reset-env! '[clojure.set]))

    (let [cache-opts {:cache {:src-paths-lookup? true}}
          {:keys [load-fn]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache."))

    (let [cache-opts {:cache {:path cache-path :src-paths-lookup? true}}
          {:keys [load-fn read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          load-res (load-fn)
          res (do (read-eval-call require-expr)
                  (read-eval-call test-expr))
          out (unwrap-result res)]
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :js) "The resulting language should be js")
      (is (contains? load-res :cache) "The resulting map should contain :cache.")
      (is (success? res) (str test-expr " should succeed"))
      (is (valid-eval-result? out) (str test-expr " should be a valid result"))
      (is (not (re-find #"nil" out)) (str test-expr " should not return nil"))
      (reset-env! '[clojure.set]))
    (revert-file-names (flatten (load/cache-file-paths-for-load-fn src-paths macros path))))

  (deftest no-write-file-fn
    (rename-file-names-to-tmp [set-js-path set-json-path])
    (let [cache-opts {:cache {:path cache-path} :write-file-fn! nil}
          {:keys [load-fn read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          res (read-eval-call "(require 'clojure.set)")
          out (unwrap-result res)
          load-res (load-fn)]
      (is (success? res) "(require 'clojure.set) should succeed and write to cache.")
      (is (valid-eval-result? out) "(require 'clojure.set) should be a valid result")
      (is (false? (io/file-exists? set-js-path)) "clojure_SLASH_set.js should not exist")
      (is (false? (io/file-exists? set-json-path)) "clojure_SLASH_set.cache.json should not exist")
      (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
      (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
      (is (= (:lang load-res) :clj) "The resulting language should be clj")
      (is (false? (contains? load-res :cache)) "The resulting map should not contain :cache.")
      (reset-env! '[clojure.set])))

  (deftest write-files-to-cache
    (let [cache-opts {:cache {:path cache-path}}
          {:keys [read-eval-call reset-env!]} (fns-from-cache-opts cache-opts)
          res (read-eval-call "(require 'clojure.set)")
          out (unwrap-result res)]
      (is (success? res) "(require 'clojure.set) should succeed and write to cache.")
      (is (valid-eval-result? out) "(require 'clojure.set) should be a valid result")
      (is (true? (io/file-exists? set-js-path)) "clojure_SLASH_set.js should exist")
      (is (true? (io/file-exists? set-json-path)) "clojure_SLASH_set.cache.json should exist")

      ;; delete the generated files because could be different from the commited version
      ;; leaving the git repo with modified files
      (io/safely-delete! set-js-path)
      (io/safely-delete! set-json-path)

      (revert-file-names [set-js-path set-json-path])
      (reset-env! '[clojure.set]))))

;;; the order in this case is important
(defn test-ns-hook []
  (repl/force-init!)
  (read-no-files-in-cache-path-no-src-paths)
  (read-no-files-in-cache-path-yes-src-paths)
  (read-yes-files-in-cache-path-yes-src-paths)
  (read-yes-files-in-cache-path-no-src-paths)
  (no-write-file-fn)
  (write-files-to-cache))
