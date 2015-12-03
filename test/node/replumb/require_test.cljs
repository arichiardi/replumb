(ns replumb.require-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [nodejs-options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.load :as load]
            [replumb.nodejs.io :as io]))

(def validated-echo-cb (partial repl/validated-call-back! echo-callback))

(def test-paths ["dev-resources/private/test/src/cljs"
                 "dev-resources/private/test/src/clj"])

(nodejs/enable-util-print!)

(deftest process-require
  ;; Damian - Add COMPILED flag to cljs eval to turn off namespace already declared errors
  ;; AR - COMPILED goes here not in the runner otherwise node does not execute doo tests
  (set! js/COMPILED true)
  (let [target-opts (merge (nodejs-options test-paths io/read-file!)
                           {:verbose true})]
    ;; AR - Test for "No *load-fn* when requiring a namespace in browser #35"
    ;; Note there these are tests with a real *load-fn*
    (let [res (repl/read-eval-call target-opts validated-echo-cb "(require 'foo.bar.baz)")
          out (unwrap-result res)]
      (println res)
      (is (success? res) "(require 'foo.bar.baz) should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) should not change namespace")
      (is (= "nil" out) "(require 'foo.bar.baz) should return \"nil\"")
      (repl/reset-env! ['foo.bar.baz])))
  (set! js/COMPILED false))
