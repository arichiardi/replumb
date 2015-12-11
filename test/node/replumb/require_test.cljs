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

;; Damian - Add js/COMPILED flag to cljs eval to turn off namespace already declared errors
;; AR - js/COMPILED goes here not in the runner otherwise node does not execute doo tests
;; AR - js/COMPILED is not needed after having correctly bootstrapped the
;; nodejs environment, see PR #57
(let [target-opts (nodejs-options src-paths io/read-file!)
      read-eval-call (partial repl/read-eval-call target-opts)]
  (deftest process-require
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
      (is (= "\"whatever\"" out) "(require 'foo.bar.baz) and foo.bar.baz/a should return \"whatever\"")
      (repl/reset-env! ['foo.bar.baz]))
    ;; https://github.com/ScalaConsultants/replumb/issues/39
    (let [res (do (read-eval-call validated-echo-cb "(require 'foo.bar.baz)")
                  (read-eval-call validated-echo-cb "foo.bar.baz/const-a"))
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) and foo.bar.baz/const-a should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/const-a should not change namespace")
      (is (= "1024" out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should return \"1024\"")
      (repl/reset-env! ['foo.bar.baz]))
    (let [res (do (read-eval-call validated-echo-cb "(require '[foo.bar.baz :refer [a]])")
                  (read-eval-call validated-echo-cb "a"))
          out (unwrap-result res)]
      (is (success? res) "(require '[foo.bar.baz :refer [a]]) and a should succeed")
      (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [a]]) and a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require '[foo.bar.baz :refer [a]]) and a should not change namespace")
      (is (= "\"whatever\"" out) "(require '[foo.bar.baz :refer [a]]) and a should return \"whatever\"")
      (repl/reset-env! ['foo.bar.baz]))
    (let [res (do (read-eval-call validated-echo-cb "(require '[foo.bar.baz :refer [const-a]])")
                  (read-eval-call validated-echo-cb "const-a"))
          out (unwrap-result res)]
      (is (success? res) "(require '[foo.bar.baz :refer [const-a]]) and const-a should succeed")
      (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [const-a]]) and const-a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require '[foo.bar.baz :refer [const-a]]) and const-a should not change namespace")
      (is (= "1024" out) "(require '[foo.bar.baz :refer [const-a]]) and const-a should return 1024")
      (repl/reset-env! ['foo.bar.baz])))

  (deftest process-reload
    (let [alterable-core-path "dev-resources/private/test/src/cljs/alterable/core.cljs"
          pre-content "(ns alterable.core)\n\n(def b \"pre\")"
          post-content "(ns alterable.core)\n\n(def b \"post\")"]
      ;; Writing "pre" version of alterable.core
      (io/write-file! alterable-core-path pre-content)
      (let [res (do (read-eval-call validated-echo-cb "(require 'alterable.core)")
                    (read-eval-call validated-echo-cb "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core) and alterable.core/b should not change namespace")
        (is (= "\"pre\"" out) "(require 'alterable.core) and alterable.core/b should return \"pre\""))
      ;; Writing "post" version of alterable.core
      (io/write-file! alterable-core-path post-content)
      (let [res (do (read-eval-call validated-echo-cb "(require 'alterable.core :reload)")
                    (read-eval-call validated-echo-cb "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core :reload) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core :reload) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core :reload) and alterable.core/b should not change namespace")
        (is (= "\"post\"" out) "(require 'alterable.core :reload) and alterable.core/b should return \"post\""))
      (repl/reset-env! ['alterable.core])
      (io/delete-file! alterable-core-path)))

  (deftest process-reload-all
    (let [alterable-core-path "dev-resources/private/test/src/cljs/alterable/core.cljs"
          alterable-utils-path "dev-resources/private/test/src/cljs/alterable/utils.cljs"
          utils-pre-content "(ns alterable.utils)\n\n(def c \"pre\")"
          utils-post-content "(ns alterable.utils)\n\n(def c \"post\")"
          core-pre-content "(ns alterable.core\n  (:require alterable.utils))\n\n(def b (str alterable.utils/c))"
          core-post-content "(ns alterable.core\n  (:require alterable.utils))\n\n(def b (str alterable.utils/c))"]
      ;; Writing "pre" version of alterable.core & alterable.utils
      (io/write-file! alterable-utils-path utils-pre-content)
      (io/write-file! alterable-core-path core-pre-content)
      (let [res (do (read-eval-call validated-echo-cb "(require 'alterable.core)")
                    (read-eval-call validated-echo-cb "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core) and alterable.core/b should not change namespace")
        (is (= "\"pre\"" out) "(require 'alterable.core) and alterable.core/b should return \"pre\""))
      (let [res (do (read-eval-call validated-echo-cb "(require 'alterable.utils)")
                    (read-eval-call validated-echo-cb "alterable.utils/c"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.utils) and alterable.utils/c should succeed")
        (is (valid-eval-result? out) "(require 'alterable.utils) and alterable.utils/c should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.utils) and alterable.utils/c should not change namespace")
        (is (= "\"pre\"" out) "(require 'alterable.utils) and alterable.utils/c should return \"pre\""))
      ;; Writing "post" version of alterable.core & alterable.utils
      (io/write-file! alterable-utils-path utils-post-content)
      (let [res (do (read-eval-call validated-echo-cb "(require 'alterable.core :reload-all)")
                    (read-eval-call validated-echo-cb "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core :reload) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core :reload) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core :reload) and alterable.core/b should not change namespace")
        (is (= "\"post\"" out) "(require 'alterable.core :reload) and alterable.core/b should return \"post\""))
      (repl/reset-env! ['alterable.core 'alterable.utils])
      (io/delete-file! alterable-core-path)
      (io/delete-file! alterable-utils-path))))
