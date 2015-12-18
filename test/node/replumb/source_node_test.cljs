(ns replumb.source-node-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [nodejs-options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.load :as load]
            [replumb.nodejs.io :as io]))

(let [src-paths ["dev-resources/private/test/node/compiled/out"
                 "dev-resources/private/test/src/cljs"
                 "dev-resources/private/test/src/clj"]
      validated-echo-cb (partial repl/validated-call-back! echo-callback)
      target-opts (nodejs-options src-paths io/read-file!)
      read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)]

  (deftest source-in-cljs-core
    (let [res (read-eval-call "(source max)")
          source-string (unwrap-result res)
          expected "(defn ^number max
  \"Returns the greatest of the nums.\"
  ([x] x)
  ([x y] (cljs.core/max x y))
  ([x y & more]
   (reduce max (cljs.core/max x y) more)))"]
      (is (success? res) "(source max) should succeed.")
      (is (valid-eval-result? source-string) "(source max) should be a valid result")
      (is (= expected source-string) "(source max) should return valid source")
      (repl/reset-env!))

    (let [res (read-eval-call "(source nil?)")
          source-string (unwrap-result res)
          expected "(defn ^boolean nil?
  \"Returns true if x is nil, false otherwise.\"
  [x]
  (coercive-= x nil))"]
      (is (success? res) "(source nil?) should succeed.")
      (is (valid-eval-result? source-string) "(source nil?) should be a valid result")
      (is (= expected source-string) "(source nil?) should return valid source")
      (repl/reset-env!))

    (let [res (read-eval-call "(source not-existing)")
          source-string (unwrap-result res)]
      (is (success? res) "(source not-existing) should succeed.")
      (is (valid-eval-result? source-string) "(source not-existing) should be a valid result")
      (is (= "nil" source-string) "(source not-existing) should return nil")
      (repl/reset-env!)))

  (deftest source-in-non-core-ns
    (let [res (do (read-eval-call "(require 'clojure.set)")
                  (read-eval-call "(source clojure.set/union)"))
          source-string (unwrap-result res)
          expected "(defn union
  \"Return a set that is the union of the input sets\"
  ([] #{})
  ([s1] s1)
  ([s1 s2]
     (if (< (count s1) (count s2))
       (reduce conj s2 s1)
       (reduce conj s1 s2)))
  ([s1 s2 & sets]
     (let [bubbled-sets (bubble-max-key count (conj sets s2 s1))]
       (reduce into (first bubbled-sets) (rest bubbled-sets)))))"]
      (is (success? res) "(source clojure.set/union) should succeed.")
      (is (valid-eval-result? source-string) "(source clojure.set/union) should be a valid result")
      (is (= expected source-string) "(source clojure.set/union) should return valid source")
      (repl/reset-env! ['clojure.set]))

    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(source clojure.string/trim)"))
          source-string (unwrap-result res)
          expected "(defn trim
  \"Removes whitespace from both ends of string.\"
  [s]
  (gstring/trim s))"]
      (is (success? res) "(source clojure.string/trim) should succeed.")
      (is (valid-eval-result? source-string) "(source clojure.string/trim) should be a valid result")
      (is (= expected source-string) "(source clojure.string/trim) should return valid source")
      (repl/reset-env! '[clojure.string goog.string goog.string.StringBuffer]))

    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(source clojure.string/not-existing)"))
          source-string (unwrap-result res)]
      (is (success? res) "(source clojure.string/not-existing) should succeed.")
      (is (valid-eval-result? source-string) "(source clojure.string/not-existing) should be a valid result")
      (is (= "nil" source-string) "(source clojure.string/not-existing) should return valid source")
      (repl/reset-env! '[clojure.string goog.string goog.string.StringBuffer]))

    ;; https://github.com/ScalaConsultants/replumb/issues/86
    (let [res (do (read-eval-call "(require '[clojure.string :as s])")
                  (read-eval-call "(source s/trim)"))
          docstring (unwrap-result res)]
      (is (success? res) "(require '[clojure.string :as s]) and (doc s/trim) should succeed.")
      (is (valid-eval-result? docstring) "(require '[clojure.string :as s]) and (doc s/trim) should be a valid result")
      (is (re-find #"Removes whitespace from both ends of string" docstring) "(require '[clojure.string :as s]) and (doc s/trim) should return valid docstring")
      (repl/reset-env! '[clojure.string goog.string goog.string.StringBuffer])))

  (deftest source-in-custom-ns
    (let [res (do (read-eval-call "(require 'foo.bar.baz)")
                  (read-eval-call "(source foo.bar.baz/a)"))
          source-string (unwrap-result res)
          expected "(def a \"whatever\")"]
      (is (success? res) "(source foo.bar.baz/a) should succeed.")
      (is (valid-eval-result? source-string) "(source foo.bar.baz/a) should be a valid result")
      (is (= expected source-string) "(source foo.bar.baz/a) should return valid source")
      (repl/reset-env! ['foo.bar.baz]))

    ;; https://github.com/ScalaConsultants/replumb/issues/86
    (let [res (do (read-eval-call "(require '[foo.bar.baz :as baz])")
                  (read-eval-call "(source baz/a)"))
          source-string (unwrap-result res)
          expected "(def a \"whatever\")"]
      (is (success? res) "(require '[foo.bar.baz :as baz]) and (source baz/a) should succeed.")
      (is (valid-eval-result? source-string) "(require '[foo.bar.baz :as baz]) and (source baz/a) should be a valid result")
      (is (= expected source-string) "(require '[foo.bar.baz :as baz]) and (source baz/a) should return valid source")
      (repl/reset-env! ['foo.bar.baz])))

  ;; see "RUNNING TESTS" section for explanation of `test-ns-hook` special function
  ;; https://clojure.github.io/clojure/clojure.test-api.html
  (defn test-ns-hook []
    (repl/force-init!)
    (source-in-cljs-core)
    (source-in-non-core-ns)
    (source-in-custom-ns)))

(let [validated-echo-cb (partial repl/validated-call-back! echo-callback)
      target-opts (nodejs-options load/no-resource-load-fn!)
      read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)]
  (deftest source-when-read-file-return-nil
    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(source clojure.string/trim)"))
          source-string (unwrap-result res)]
      (is (success? res) "(source clojure.string/trim) should succeed.")
      (is (valid-eval-result? source-string) "(source clojure.string/trim) should be a valid result")
      (is (= "nil" source-string) "(source clojure.string/trim) should return nil")
      (repl/reset-env! '[clojure.string goog.string goog.string.StringBuffer]))))
