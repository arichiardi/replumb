(ns ^:figwheel-load replumb.options-test
  (:require [cljs.test :refer-macros [deftest is]]
            [replumb.repl :refer [valid-opts-set valid-opts normalize-opts]]))

(deftest options-valid-set
  ;; AR - just not to forget adding them in valid-opts-set
  (let [opts {:verbose :true
              :load-fn! "fn"
              :warning-as-error true
              :target "default"
              :init-fn! "fn"
              :read-file-fn! "fn"
              :src-paths ["src/one" "src/two"]
              :no-pr-str-on-value true
              :write-file-fn! "fn"
              :cache {:path "cache/path" :src-paths-lookup? true}}]
    (is (every? valid-opts-set (keys (valid-opts opts))) "Please always add valid options to valid-opts-set")))

(deftest options-target
  (is (= :default (:target (normalize-opts {:target :browser}))) "Target :browser is actually :default")
  (is (= :default (:target (normalize-opts {:target :pizza}))) "Target :pizza in any case defaults to :default")
  (is (= :nodejs (:target (normalize-opts {:target :nodejs}))) "Target :nodejs is correct"))

(deftest options-init-fn
  (is (= 1 (count (:init-fns (normalize-opts {:target :browser})))) "Count of :init-fns should at least be 1 if no :init-fn!")
  (is (= 2 (count (:init-fns (normalize-opts {:target :browser :init-fn! #()})))) "Count of :init-fns should be 2 if :init-fn! is there")
  (is (every? (complement nil?) (:init-fns (normalize-opts {:target :browser :init-fn! #()}))) "The :init-fns option is a seq non-nil functions."))

(deftest options-src-paths
  (is (not (nil? (:src-paths (normalize-opts {:src-paths ["src/cljs" "src/clj"]})))) "Must be able to attach :src-paths")
  (is (vector? (:src-paths (normalize-opts {:src-paths ["src/cljs" "src/clj"]}))) "The :src-paths must be a vector")
  (is (empty? (:src-paths (normalize-opts {}))) "The :src-paths must be empty if not present"))

(deftest options-load-fn
  ;; AR - No cljs.test/function? here
  (is (not (nil? (:load-fn! (normalize-opts {:load-fn! #()})))) "Must be able to attach :load-fn!")
  (is (= "precedence" (:load-fn! (normalize-opts {:load-fn! "precedence" :read-file-fn! #()}))) "Must be able to give precedence to :load-fn! when present")
  (is (nil? (:load-fn! (normalize-opts {:read-file-fn! #()}))) "Must create :load-fn! only when both :read-file-fn! and :src-paths are present")
  (is (nil? (:load-fn! (normalize-opts {:src-paths {} :read-file-fn! #()}))) "Must create :load-fn! only when :src-paths is sequential")
  (is (nil? (:load-fn! (normalize-opts {:src-paths [:src "src"] :read-file-fn! #()}))) "Must create :load-fn! only when :src-paths are all strings")
  (is (not (nil? (:read-file-fn! (normalize-opts {:src-paths ["src"] :read-file-fn! #()})))) "Must NOT elide :read-file-fn! after having created :load-fn!")
  (is (not (nil? (:load-fn! (normalize-opts {:src-paths ["src"] :read-file-fn! #()})))) "Must create a brand new :load-fn! when :read-file-fn! is present"))

(deftest options-warning
  (is (= true (:warning-as-error (normalize-opts {:warning-as-error true}))) "Option :warning-as-error should be carried over"))
