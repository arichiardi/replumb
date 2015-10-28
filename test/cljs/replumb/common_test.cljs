(ns ^:figwheel-load replumb.common-test
  (:require [cljs.test :refer-macros [deftest is]]
            [replumb.common :as common :refer [extract-message]]))

(def empty-err {})
(def single-err #(ex-info "Could not eval -)" {:tag :cljs/reader-exception}))
(def nested-err #(ex-info "Could not eval -)"
                         {:tag :cljs/analysis-error}
                         (ex-info "Unmatched delimiter )" {:type :reader-exception, :line 1, :column 3, :file "-"})))

(def err-with-ERROR #(ex-info "ERROR"
                              {:tag :filter-me}
                              (ex-info "Write this"
                                       {:tag :write-this-exception}
                                       (ex-info "ERROR"
                                                {:tag :filter-me}
                                                (ex-info "and this please" {:tag :write-this-exception})))))

(deftest error-message
  (let [msg (extract-message (single-err))]
    (is (= "Could not eval -)" msg))
    (is (string? msg)))
  (let [msg (extract-message (nested-err))]
    (is (= "Could not eval -) - Unmatched delimiter )" msg))
    (is (string? msg)))
  (let [msg (extract-message empty-err)]
    (is (= "Error" msg))
    (is (string? msg)))
  (let [msg (extract-message (err-with-ERROR) true)]
    (is (re-find #"Write this.*and this please" msg))))
