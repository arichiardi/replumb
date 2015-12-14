(ns replumb.require-node-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [nodejs-options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.nodejs.io :as io]))

;; Damian - Add js/COMPILED flag to cljs eval to turn off namespace already declared errors
;; AR - js/COMPILED goes here not in the runner otherwise node does not execute doo tests
;; AR - js/COMPILED is not needed after having correctly bootstrapped the
;; nodejs environment, see PR #57
(let [src-paths ["dev-resources/private/test/node/compiled/out"
                 "dev-resources/private/test/src/cljs"
                 "dev-resources/private/test/src/clj"]
      validated-echo-cb (partial repl/validated-call-back! echo-callback)
      target-opts (nodejs-options src-paths io/read-file!)
      read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)
      read-eval-call-verbose (partial repl/read-eval-call (merge target-opts {:verbose true}) validated-echo-cb)]

  (deftest require+doc
    ;; https://github.com/ScalaConsultants/replumb/issues/47
    (let [res (do (read-eval-call "(require 'clojure.set)")
                  (read-eval-call "(doc clojure.set)"))
          docstring (unwrap-result res)]
      (is (success? res) "(require ...) and (doc clojure.set) should succeed.")
      (is (valid-eval-result? docstring) "(require ...) and (doc clojure.set) should be a valid result")
      (is (re-find #"Set operations such as union/intersection" docstring) "(require ...) and (doc clojure.set) should return valid docstring")
      (repl/reset-env! ['clojure.set]))

    ;; https://github.com/ScalaConsultants/replumb/issues/59
    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(doc clojure.string/trim)"))
          docstring (unwrap-result res)]
      (is (success? res) "(require ...) and (doc clojure.string/trim) should succeed.")
      (is (valid-eval-result? docstring) "(require ...) and (doc clojure.string/trim) should be a valid result")
      (is (re-find #"Removes whitespace from both ends of string" docstring) "(require ...) and (doc clojure.string/trim) should return valid docstring")
      (repl/reset-env! '[clojure.string goog.string goog.string.StringBuffer])))

  (deftest process-require
    ;; AR - Test for "No *load-fn* when requiring a namespace in browser #35"
    ;; Note there these are tests with a real *load-fn*
    (let [res (read-eval-call "(require 'foo.bar.baz)")
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) should not change namespace")
      (is (= "nil" out) "(require 'foo.bar.baz) should return \"nil\"")
      (repl/reset-env! ['foo.bar.baz]))

    (let [res (do (read-eval-call "(require 'foo.bar.baz)")
                  (read-eval-call "foo.bar.baz/a"))
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) and foo.bar.baz/a should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/a should not change namespace")
      (is (= "\"whatever\"" out) "(require 'foo.bar.baz) and foo.bar.baz/a should return \"whatever\"")
      (repl/reset-env! ['foo.bar.baz]))

    ;; https://github.com/ScalaConsultants/replumb/issues/39
    (let [res (do (read-eval-call "(require 'foo.bar.baz)")
                  (read-eval-call "foo.bar.baz/const-a"))
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) and foo.bar.baz/const-a should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/const-a should not change namespace")
      (is (= "1024" out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should return \"1024\"")
      (repl/reset-env! ['foo.bar.baz]))

    ;; AR - Upstream problem (already solved)
    ;; https://github.com/ScalaConsultants/replumb/issues/66
    #_(let [res (do (read-eval-call "(require '[foo.bar.baz :refer [a]])")
                    (read-eval-call "a"))
            out (unwrap-result res)]
        (is (success? res) "(require '[foo.bar.baz :refer [a]]) and a should succeed")
        (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [a]]) and a should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require '[foo.bar.baz :refer [a]]) and a should not change namespace")
        (is (= "\"whatever\"" out) "(require '[foo.bar.baz :refer [a]]) and a should return \"whatever\"")
        (repl/reset-env! ['foo.bar.baz 'a]))
    #_(let [res (do (read-eval-call "(require '[foo.bar.baz :refer [const-a]])")
                    (read-eval-call "const-a"))
            out (unwrap-result res)]
        (is (success? res) "(require '[foo.bar.baz :refer [const-a]]) and const-a should succeed")
        (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [const-a]]) and const-a should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require '[foo.bar.baz :refer [const-a]]) and const-a should not change namespace")
        (is (= "1024" out) "(require '[foo.bar.baz :refer [const-a]]) and const-a should return 1024")
        (repl/reset-env! ['foo.bar.baz 'const-a])))

  (deftest process-goog-import
    ;; AR - requiring clojure.string in turns imports goog.string
    ;; Node that goog.string should be never required but imported
    (let [res (read-eval-call "(require 'clojure.string)")
          out (unwrap-result res)]
      (is (success? res) "(require 'clojure.string) should succeed")
      (is (valid-eval-result? out) "(require 'clojure.string) should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'clojure.string) should not change namespace")
      (is (= "nil" out) "(require 'clojure.string) should return \"nil\"")
      (repl/reset-env! '[clojure.string goog.string goog.string.StringBuffer]))

    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(clojure.string/reverse \"clojurescript\")"))
          out (unwrap-result res)]
      (is (success? res) "(require 'clojure.string) and clojure.string/reverse should succeed")
      (is (valid-eval-result? out) "(require 'clojure.string) and clojure.string/reverse should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'clojure.string) and clojure.string/reverse should not change namespace")
      (is (= "\"tpircserujolc\"" out) "(require 'clojure.string) and clojure.string/reverse should return \"tpircserujolc\"")
      (repl/reset-env! '[clojure.string goog.string goog.string.StringBuffer]))

    (let [res (do (read-eval-call "(import 'goog.string.StringBuffer)")
                  (read-eval-call "(let [sb (StringBuffer. \"clojure\")]
                                     (.append sb \"script\")
                                     (.toString sb))"))
          out (unwrap-result res)]
      (is (success? res) "(import 'goog.string.StringBuffer) and .toString should succeed")
      (is (valid-eval-result? out) "(import 'goog.string.StringBuffer) and .toString should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(import 'goog.string.StringBuffer) and .toString should not change namespace")
      (is (= "\"clojurescript\"" out) "(import 'goog.string.StringBuffer) and .toString should return \"clojurescript\"")
      (repl/reset-env! '[goog.string goog.string.StringBuffer])))

  (deftest process-reload
    (let [alterable-core-path "dev-resources/private/test/src/cljs/alterable/core.cljs"
          pre-content "(ns alterable.core)\n\n(def b \"pre\")"
          post-content "(ns alterable.core)\n\n(def b \"post\")"]
      ;; Writing "pre" version of alterable.core
      (io/write-file! alterable-core-path pre-content)
      (let [res (do (read-eval-call "(require 'alterable.core)")
                    (read-eval-call "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core) and alterable.core/b should not change namespace")
        (is (= "\"pre\"" out) "(require 'alterable.core) and alterable.core/b should return \"pre\"")
        (repl/reset-env! ['alterable.core]))

      ;; Writing "post" version of alterable.core
      (io/write-file! alterable-core-path post-content)
      (let [res (do (read-eval-call "(require 'alterable.core :reload)")
                    (read-eval-call "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core :reload) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core :reload) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core :reload) and alterable.core/b should not change namespace")
        (is (= "\"post\"" out) "(require 'alterable.core :reload) and alterable.core/b should return \"post\"")
        (repl/reset-env! ['alterable.core]))

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
      (let [res (do (read-eval-call "(require 'alterable.core)")
                    (read-eval-call "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core) and alterable.core/b should not change namespace")
        (is (= "\"pre\"" out) "(require 'alterable.core) and alterable.core/b should return \"pre\"")
        (repl/reset-env! '[alterable.core alterable.utils]))

      (let [res (do (read-eval-call "(require 'alterable.utils)")
                    (read-eval-call "alterable.utils/c"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.utils) and alterable.utils/c should succeed")
        (is (valid-eval-result? out) "(require 'alterable.utils) and alterable.utils/c should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.utils) and alterable.utils/c should not change namespace")
        (is (= "\"pre\"" out) "(require 'alterable.utils) and alterable.utils/c should return \"pre\"")
        (repl/reset-env! '[alterable.utils]))

      ;; Writing "post" version of alterable.core & alterable.utils
      (io/write-file! alterable-utils-path utils-post-content)
      (let [res (do (read-eval-call "(require 'alterable.core :reload-all)")
                    (read-eval-call "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core :reload) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core :reload) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core :reload) and alterable.core/b should not change namespace")
        (is (= "\"post\"" out) "(require 'alterable.core :reload) and alterable.core/b should return \"post\"")
        (repl/reset-env! '[alterable.core alterable.utils]))
      (io/delete-file! alterable-core-path)
      (io/delete-file! alterable-utils-path)))

  ;; AR - we need to force the order so that we can force re-init at the beginning
  (defn test-ns-hook []
    (repl/force-init!)
    (require+doc)
    (process-require)
    (process-goog-import)
    (process-reload)
    (process-reload-all)))
