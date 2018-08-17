(ns replumb.cache-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.load :as load]
            [replumb.test-env :as e]
            [replumb.test-helpers :as h :refer-macros [read-eval-call-test]]))

(defn rename-file-names-to-tmp
  [paths]
  (doseq [path paths]
    (e/*rename-file-fn* path (str path ".tmp"))
    (is (not (e/*file-exists-fn* path)) (str path " should not exist"))))

(defn revert-file-names
  [paths]
  (doseq [path paths]
    (let [tmp-file (str path ".tmp")]
      (e/*rename-file-fn* tmp-file (apply str (drop-last 4 tmp-file))))))

;;; important: if the ClojureScript version is updated rembember to update
;;; the files in the cache folder as well (in order to match the compiler
;;; version)

(binding [e/*src-paths* ["dev-resources/private/test/node/compiled/out"]]
  (let [cache-path "dev-resources/private/test/cache"
        path "clojure/set"
        cache-files-to-search [(str (common/normalize-path cache-path) path ".js")
                               (str (common/normalize-path cache-path) path ".cache.json")]
        src-paths-files-to-search (flatten (load/cache-file-paths-for-load-fn e/*src-paths* false path))]

    ;; read-no-files-in-cache-path-no-src-paths
    (h/read-eval-call-test (assoc e/*target-opts* :cache {})
      [:before (do (rename-file-names-to-tmp cache-files-to-search)
                   (rename-file-names-to-tmp src-paths-files-to-search))
       "(def a \"bogus-op\")"
       :after (do (revert-file-names cache-files-to-search)
                  (revert-file-names src-paths-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:path cache-path})
      [:before (do (rename-file-names-to-tmp cache-files-to-search)
                   (rename-file-names-to-tmp src-paths-files-to-search))
       "(def a \"bogus-op\")"
       :after (do (revert-file-names cache-files-to-search)
                  (revert-file-names src-paths-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:path cache-path}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:src-paths-lookup? true})
      [:before (do (rename-file-names-to-tmp cache-files-to-search)
                   (rename-file-names-to-tmp src-paths-files-to-search))
       "(def a \"bogus-op\")"
       :after (do (revert-file-names cache-files-to-search)
                  (revert-file-names src-paths-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:src-paths-lookup? true}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:src-paths-lookup? true :path cache-path})
      [:before (do (rename-file-names-to-tmp cache-files-to-search)
                   (rename-file-names-to-tmp src-paths-files-to-search))
       "(def a \"bogus-op\")"
       :after (do (revert-file-names cache-files-to-search)
                  (revert-file-names src-paths-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:src-paths-lookup? true :path cache-path}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    ;; read-no-files-in-cache-path-yes-src-paths
    (h/read-eval-call-test (assoc e/*target-opts* :cache {})
      [:before (do (rename-file-names-to-tmp cache-files-to-search))
       "(def a \"bogus-op\")"
       :after (do (revert-file-names src-paths-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:path cache-path})
      [:before (do (rename-file-names-to-tmp cache-files-to-search))
       "(def a \"bogus-op\")"
       :after (do (revert-file-names cache-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:path cache-path}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:src-paths-lookup? true})
      [:before (do (rename-file-names-to-tmp cache-files-to-search))
       "(require 'clojure.set :reload)"
       "(doc clojure.set)"
       :after (do (revert-file-names cache-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:src-paths-lookup? true}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :js) "The resulting language should be js")
        (is (contains? load-res :cache) "The resulting map should contain :cache.")
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (not (re-find #"nil" out)) (str _msg_ " should not return nil"))))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:src-paths-lookup? true :path cache-path})
      [:before (do (rename-file-names-to-tmp cache-files-to-search))
       "(require 'clojure.set :reload)"
       "(doc clojure.set)"
       :after (do (revert-file-names cache-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:src-paths-lookup? true :path cache-path}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :js) "The resulting language should be js")
        (is (contains? load-res :cache) "The resulting map should contain :cache.")
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (not (re-find #"nil" out)) (str _msg_ " should not return nil"))))

    ;; read-yes-files-in-cache-path-yes-src-paths
    (h/read-eval-call-test (assoc e/*target-opts* :cache {})
      ["(def a \"bogus-op\")"]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:path cache-path})
      ["(require 'clojure.set :reload)"
       "(doc clojure.set)"]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:path cache-path}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :js) "The resulting language should be js")
        (is (contains? load-res :cache) "The resulting map should contain :cache.")
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (not (re-find #"nil" out)) (str _msg_ " should not return nil"))))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:src-paths-lookup? true})
      ["(require 'clojure.set :reload)"
       "(doc clojure.set)"]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:src-paths-lookup? true}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :js) "The resulting language should be js")
        (is (contains? load-res :cache) "The resulting map should contain :cache.")
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (not (re-find #"nil" out)) (str _msg_ " should not return nil"))))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:path cache-path :src-paths-lookup? true})
      ["(require 'clojure.set :reload)"
       "(doc clojure.set)"]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:path cache-path :src-paths-lookup? true}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :js) "The resulting language should be js")
        (is (contains? load-res :cache) "The resulting map should contain :cache.")
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (not (re-find #"nil" out)) (str _msg_ " should not return nil"))))

    ;; read-yes-files-in-cache-path-no-src-paths
    (h/read-eval-call-test (assoc e/*target-opts* :cache {})
      [:before (rename-file-names-to-tmp src-paths-files-to-search)
       "(def a \"bogus-op\")"
       :after (revert-file-names src-paths-files-to-search)]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:path cache-path})
      [:before (do (rename-file-names-to-tmp src-paths-files-to-search))
       "(require 'clojure.set :reload)"
       "(doc clojure.set)"
       :after (do (revert-file-names src-paths-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:path cache-path}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :js) "The resulting language should be js")
        (is (contains? load-res :cache) "The resulting map should contain :cache.")
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (not (re-find #"nil" out)) (str _msg_ " should not return nil"))))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:src-paths-lookup? true})
      [:before (do (rename-file-names-to-tmp src-paths-files-to-search))
       "(def a \"bogus-op\")"
       :after (do (revert-file-names src-paths-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:src-paths-lookup? true}))
            load-res (load-fn {:macros false :path path} identity)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts* :cache {:path cache-path :src-paths-lookup? true})
      [:before (do (rename-file-names-to-tmp src-paths-files-to-search))
       "(require 'clojure.set :reload)"
       "(doc clojure.set)"
       :after (do (revert-file-names src-paths-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:path cache-path :src-paths-lookup? true}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)]
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :js) "The resulting language should be js")
        (is (contains? load-res :cache) "The resulting map should contain :cache.")
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (not (re-find #"nil" out)) (str _msg_ " should not return nil"))))

    ;; no-write-file-fn
    (h/read-eval-call-test (assoc e/*target-opts*  :cache {:path cache-path} :write-file-fn! nil)
      [:before (do (rename-file-names-to-tmp cache-files-to-search))
       "(require 'clojure.set)"
       :after (do (revert-file-names cache-files-to-search))]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:cache {:path cache-path} :write-file-fn! nil}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)
            [set-js-path set-json-path] cache-files-to-search]
        (is (success? @_res_) "(require 'clojure.set) should succeed and write to cache.")
        (is (valid-eval-result? out) "(require 'clojure.set) should be a valid result")
        (is (not (e/*file-exists-fn* set-js-path)) "clojure_SLASH_set.js should not exist")
        (is (not (e/*file-exists-fn* set-json-path)) "clojure_SLASH_set.cache.json should not exist")
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    (h/read-eval-call-test (assoc e/*target-opts*  :cache {:path cache-path} :write-file-fn! nil)
      [:before #_(rename-file-names-to-tmp cache-files-to-search)
       "(require 'foo.bar.baz)"
       :after #_(revert-file-names cache-files-to-search)]
      (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:cache {:path cache-path} :write-file-fn! nil}))
            load-res (load-fn {:macros false :path path} identity)
            out (unwrap-result @_res_)
            [set-js-path set-json-path] cache-files-to-search]
        (is (success? @_res_) "(require 'clojure.set) should succeed and write to cache.")
        (is (valid-eval-result? out) "(require 'clojure.set) should be a valid result")
        (is (not (e/*file-exists-fn* set-js-path)) "clojure_SLASH_set.js should not exist")
        (is (not (e/*file-exists-fn* set-json-path)) "clojure_SLASH_set.cache.json should not exist")
        (is (contains? load-res :source) "Loading should succeed and the resulting map contain :source")
        (is (contains? load-res :lang) "Loading should succeed and the resulting map contain :lang")
        (is (= (:lang load-res) :clj) "The resulting language should be clj")
        (is (not (contains? load-res :cache)) "The resulting map should not contain :cache.")))

    ;; write-files-to-cache
    ;; AR - this is a weird one, because we skip loading clojure.set, we also skip writing.
    ;; There probably is a problem in the make-js-eval-fn part that writes the cache.
    ;; (h/read-eval-call-test (assoc e/*target-opts* :cache {:path cache-path} :write-file-fn! e/*write-file-fn*)
    ;;   [:before (do (rename-file-names-to-tmp cache-files-to-search))
    ;;    "(require 'clojure.set :reload)"
    ;;    ;; delete the generated files because could be different from the commited version
    ;;    ;; leaving the git repo with modified files
    ;;    :after (do (doseq [file cache-files-to-search]
    ;;                 (e/*delete-file-fn* file))
    ;;               (revert-file-names cache-files-to-search))]
    ;;   (let [load-fn (repl/make-load-fn (assoc e/*target-opts* :cache {:path cache-path} :write-file-fn! e/*write-file-fn*))
    ;;         load-res (load-fn {:macros false :path path} identity)
    ;;         out (unwrap-result @_res_)
    ;;         [set-js-path set-json-path] cache-files-to-search]
    ;;     (is (success? @_res_) "(require 'clojure.set) should succeed and write to cache.")
    ;;     (is (valid-eval-result? out) "(require 'clojure.set) should be a valid result")
    ;;     (is (e/*file-exists-fn* set-js-path) (str set-js-path " should exist"))
    ;;     (is (e/*file-exists-fn* set-json-path) (str set-json-path " should exist"))))

    ))
