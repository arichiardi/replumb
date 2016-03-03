(ns replumb.source-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [replumb.core :as core :refer [success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result? extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.test-env :as e]
            [replumb.test-helpers :as h :refer-macros [read-eval-call-test]]))

(h/read-eval-call-test e/*target-opts*
  ["(source max)"]
  (let [source-string (unwrap-result @_res_)
        expected "(defn ^number max
  \"Returns the greatest of the nums.\"
  ([x] x)
  ([x y] (cljs.core/max x y))
  ([x y & more]
   (reduce max (cljs.core/max x y) more)))"]
    (is (success? @_res_) "(source max) should succeed.")
    (is (valid-eval-result? source-string) "(source max) should be a valid result")
    (is (= expected source-string) "(source max) should return valid source"))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(source nil?)"]
  (let [source-string (unwrap-result @_res_)
        expected "(defn ^boolean nil?
  \"Returns true if x is nil, false otherwise.\"
  [x]
  (coercive-= x nil))"]
    (is (success? @_res_) "(source nil?) should succeed.")
    (is (valid-eval-result? source-string) "(source nil?) should be a valid result")
    (is (= expected source-string) "(source nil?) should return valid source"))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(source not-existing)"]
  (let [source-string (unwrap-result @_res_)]
    (is (success? @_res_) "(source not-existing) should succeed.")
    (is (valid-eval-result? source-string) "(source not-existing) should be a valid result")
    (is (= "nil" source-string) "(source not-existing) should return nil"))
  (_reset!_))

;; AR - when bundling and https://github.com/ScalaConsultants/replumb/issues/69
;; will be hacked together, this will work. The reason is that we need
;; clojure/core.clj on the source path.
;; (let [res (read-eval-call "(source when)")
;; source-string (unwrap-result @_res_)]
;; (is (success? @_res_) "(source when) should succeed.")
;; (is (valid-eval-result? source-string) "(source when) should be a valid result")
;; (is (re-find #"core/defmacro when" source-string) "(source when) does not correspond to correct source")
;; (_reset!_))
(h/read-eval-call-test e/*target-opts*
  ["(source or)"]
  (let [source-string (unwrap-result @_res_)]
    (is (success? @_res_) "(source or) should succeed.")
    (is (valid-eval-result? source-string) "(source or) should be a valid result")
    (is (re-find #"core/defmacro or" source-string) "(source or) should return correct source"))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.set)"
   "(source clojure.set/union)"]
  (let [source-string (unwrap-result @_res_)
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
    (is (success? @_res_) "(source clojure.set/union) should succeed.")
    (is (valid-eval-result? source-string) "(source clojure.set/union) should be a valid result")
    (is (= expected source-string) "(source clojure.set/union) should return valid source"))
  (_reset!_ '[clojure.set]))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(source clojure.string/trim)"]
  (let [source-string (unwrap-result @_res_)
        expected "(defn trim
  \"Removes whitespace from both ends of string.\"
  [s]
  (gstring/trim s))"]
    (is (success? @_res_) "(source clojure.string/trim) should succeed.")
    (is (valid-eval-result? source-string) "(source clojure.string/trim) should be a valid result")
    (is (= expected source-string) "(source clojure.string/trim) should return valid source"))
  (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(source clojure.string/not-existing)"]
  (let [source-string (unwrap-result @_res_)]
    (is (success? @_res_) "(source clojure.string/not-existing) should succeed.")
    (is (valid-eval-result? source-string) "(source clojure.string/not-existing) should be a valid result")
    (is (= "nil" source-string) "(source clojure.string/not-existing) should return valid source"))
  (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))

;; https://github.com/ScalaConsultants/replumb/issues/86
(h/read-eval-call-test e/*target-opts*
  ["(require '[clojure.string :as s])"
   "(source s/trim)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) "(require '[clojure.string :as s]) and (doc s/trim) should succeed.")
    (is (valid-eval-result? docstring) "(require '[clojure.string :as s]) and (doc s/trim) should be a valid result")
    (is (re-find #"Removes whitespace from both ends of string" docstring) "(require '[clojure.string :as s]) and (doc s/trim) should return valid docstring")
    (_reset!_ '[clojure.string goog.string goog.string.StringBuffer])))

(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"
   "(source foo.bar.baz/a)"]
  (let [source-string (unwrap-result @_res_)
        expected "(def a \"whatever\")"]
    (is (success? @_res_) "(source foo.bar.baz/a) should succeed.")
    (is (valid-eval-result? source-string) "(source foo.bar.baz/a) should be a valid result")
    (is (= expected source-string) "(source foo.bar.baz/a) should return valid source"))
  (_reset!_ '[foo.bar.baz]))

;; https://github.com/ScalaConsultants/replumb/issues/86
(h/read-eval-call-test e/*target-opts*
  ["(require '[foo.bar.baz :as baz])"
   "(source baz/a)"]
  (let [source-string (unwrap-result @_res_)
        expected "(def a \"whatever\")"]
    (is (success? @_res_) "(require '[foo.bar.baz :as baz]) and (source baz/a) should succeed.")
    (is (valid-eval-result? source-string) "(require '[foo.bar.baz :as baz]) and (source baz/a) should be a valid result")
    (is (= expected source-string) "(require '[foo.bar.baz :as baz]) and (source baz/a) should return valid source"))
  (_reset!_ '[foo.bar.baz]))
