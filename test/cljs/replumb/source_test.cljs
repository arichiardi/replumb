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
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (= expected source-string) (str _msg_ "should return valid source"))))

(h/read-eval-call-test e/*target-opts*
  ["(source nil?)"]
  (let [source-string (unwrap-result @_res_)
        expected "(defn ^boolean nil?
  \"Returns true if x is nil, false otherwise.\"
  [x]
  (coercive-= x nil))"]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (= expected source-string) (str _msg_ "should return valid source"))))

(h/read-eval-call-test e/*target-opts*
  ["(source not-existing)"]
  (let [source-string (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (= "nil" source-string) (str _msg_ "should return nil"))))

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
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (re-find #"core/defmacro or" source-string) (str _msg_ "should return correct source"))))

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
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (= expected source-string) (str _msg_ "should return valid source"))))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(source clojure.string/trim)"]
  (let [source-string (unwrap-result @_res_)
        expected "(defn trim
  \"Removes whitespace from both ends of string.\"
  [s]
  (gstring/trim s))"]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (= expected source-string) (str _msg_ "should return valid source"))))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(source clojure.string/not-existing)"]
  (let [source-string (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (= "nil" source-string) (str _msg_ "should return valid source"))))

;; https://github.com/ScalaConsultants/replumb/issues/86
(h/read-eval-call-test e/*target-opts*
  ["(require '[clojure.string :as s])"
   "(source s/trim)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? docstring) (str _msg_ "should be a valid result"))
    (is (re-find #"Removes whitespace from both ends of string" docstring) (str _msg_ "should return valid docstring"))))

(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"
   "(source foo.bar.baz/a)"]
  (let [source-string (unwrap-result @_res_)
        expected "(def a \"whatever\")"]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (= expected source-string) (str _msg_ "should return valid source"))))

;; https://github.com/ScalaConsultants/replumb/issues/86
(h/read-eval-call-test e/*target-opts*
  ["(require '[foo.bar.baz :as baz])"
   "(source baz/a)"]
  (let [source-string (unwrap-result @_res_)
        expected "(def a \"whatever\")"]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "should be a valid result"))
    (is (= expected source-string) (str _msg_ "should return valid source"))))
