(ns ^:figwheel-load replumb.core-test
  (:require [cljs.test :refer-macros [deftest is]]
            [replumb.core :as core :refer [get-prompt success? unwrap-result]]
            [replumb.repl :as repl :refer [current-ns]]
            [replumb.common :as common :refer [valid-eval-result? valid-eval-error? extract-message]]))

(deftest prompt
  (is (not (empty? (re-seq #"=>" (get-prompt)))) "core/get-prompt should correcly return =>")
  (is (string? (get-prompt)) "core/get-prompt should be a string")
  (is (re-find (re-pattern (str (current-ns))) (get-prompt)) "core/get-prompt should contain the current namespace"))

(def successful-map {:success? true :value "This is a result"})
(def unsuccessful-map {:success? false :error (js/Error "This is an error")})
(def successful-map-with-warning {:success? true :value "This is a result" :warning "This is a warning"})

(deftest result-getters
  (is (success? successful-map) "(success? successful-map) should be true")
  (is (valid-eval-result? (unwrap-result successful-map)) "successful-map should have valid result")
  (is (not (valid-eval-error? (unwrap-result successful-map))) "successful-map should not be an valid error")
  (is (= "This is a result" (unwrap-result successful-map)) "successful-map should contain the correct message")

  (is (not (success? unsuccessful-map)) "(success? unsuccessful-map) should be false")
  (is (not (valid-eval-result? (unwrap-result unsuccessful-map))) "unsuccessful-map should not be an valid result")
  (is (valid-eval-error? (unwrap-result unsuccessful-map)) "unsuccessful-map should be an valid error" )
  (is (= "This is an error" (extract-message (unwrap-result unsuccessful-map))) "unsuccessful-map should contain the correct message")

  (is (success? successful-map-with-warning) "(success? successful-map-with-warning) should be true")
  (is (valid-eval-result? (unwrap-result successful-map-with-warning)) "successful-map-with-warning should have valid result")
  (is (not (valid-eval-error? (unwrap-result successful-map-with-warning))) "successful-map-with-warning should not be an valid error")
  (is (= "This is a warning" (unwrap-result successful-map-with-warning true)) "successful-map-with-warning should contain the correct message")
  (is (= "This is a result" (unwrap-result successful-map-with-warning false)) "successful-map-with-warning should contain the correct message"))
