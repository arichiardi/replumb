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

(deftest result-getters
  (is (success? successful-map))
  (is (valid-eval-result? (unwrap-result successful-map)))
  (is (not (valid-eval-error? (unwrap-result successful-map))))
  (is (= "This is a result" (unwrap-result successful-map)))
  (is (not (success? unsuccessful-map)))
  (is (not (valid-eval-result? (unwrap-result unsuccessful-map))))
  (is (valid-eval-error? (unwrap-result unsuccessful-map)))
  (is (= "This is an error" (extract-message (unwrap-result unsuccessful-map)))))
