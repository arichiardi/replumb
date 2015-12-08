(ns replumb.require-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [nodejs-options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.nodejs.io :as io]))

(def validated-echo-cb (partial repl/validated-call-back! echo-callback))

(def src-paths ["dev-resources/private/test/node/compiled/out"
                "dev-resources/private/test/src/cljs"
                "dev-resources/private/test/src/clj"])

(deftest process-require
  ;; Damian - Add js/COMPILED flag to cljs eval to turn off namespace already declared errors
  ;; AR - js/COMPILED goes here not in the runner otherwise node does not execute doo tests
  ;; AR - js/COMPILED is not needed after having correctly bootstrapped the
  ;; nodejs environment, see PR #57
  (let [target-opts (nodejs-options src-paths io/read-file!)
        read-eval-call (partial repl/read-eval-call target-opts)]
    ;; AR - Test for "No *load-fn* when requiring a namespace in browser #35"
    ;; Note there these are tests with a real *load-fn*
    (let [res (read-eval-call validated-echo-cb "(require 'foo.bar.baz)")
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) should not change namespace")
      (is (= "nil" out) "(require 'foo.bar.baz) should return \"nil\"")
      (repl/reset-env! ['foo.bar.baz]))
    (let [res (do (read-eval-call validated-echo-cb "(require 'foo.bar.baz)")
                  (read-eval-call validated-echo-cb "foo.bar.baz/a"))
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) and foo.bar.baz/a should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/a should not change namespace")
      (is (not (= "whatever" out)) "(require 'foo.bar.baz) and foo.bar.baz/a should return \"whatever\"")
      (repl/reset-env! ['foo.bar.baz]))
    ;; https://github.com/ScalaConsultants/replumb/issues/39
    (let [res (do (read-eval-call validated-echo-cb "(require 'foo.bar.baz)")
                  (read-eval-call validated-echo-cb "foo.bar.baz/const-a"))
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) and foo.bar.baz/const-a should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/const-a should not change namespace")
      (is (= "1024" out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should return \"1024\"")
      (repl/reset-env! ['foo.bar.baz]))))
