(ns replumb.require-node-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [nodejs-options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.load :as load]
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

  ;; see https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#namespaces
  ;; for reference
  (deftest ns-macro
    (let [res (read-eval-call "(ns my.namespace (:use [clojure.string :as s :only (trim)]))")
          error (unwrap-result res)]
      (is (not (success? error)) "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should not succeed")
      (is (valid-eval-error? error) "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should be an instance of js/Error")
      (is (re-find #"Only \[lib.ns :only \(names\)\] specs supported in :use / :use-macros;" (extract-message error))
          "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should have correct error message")
      (repl/reset-env!))

    (let [res (do (read-eval-call "(ns my.namespace (:use [clojure.string :only (trim)]))")
                  (read-eval-call "(trim \"   clojure   \")"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:use ... )) and (trim ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:use ... )) and (trim ...) should be a valid result.")
      (is (re-find #"clojure" out) "The result should be \"clojure\"")
      (repl/reset-env! '[my.namespace clojure.string goog.string goog.string.StringBuffer]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [clojure.set :as s :refer [union]]))")
                  (read-eval-call "(s/difference (set (range 1 5)) (union #{1 2} #{2 3}))"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ... )) and (s/difference ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (s/difference ...) should be a valid result.")
      (is (re-find #"\{4\}" out) "The result should be #{4}")
      (repl/reset-env! '[my.namespace clojure.set]))

    (let [res (do (read-eval-call "(ns my.namespace (:require clojure.set))")
                  (read-eval-call "(clojure.set/difference (set (range 1 5)) (clojure.set/union #{1 2} #{2 3}))"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ... )) and (clojure.set/difference ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (clojure.set/difference ...) should be a valid result.")
      (is (re-find #"\{4\}" out) "The result should be #{4}")
      (repl/reset-env! '[my.namespace clojure.set]))

    (let [res (read-eval-call "(ns my.namespace (:require [clojure set string]))")
          error (unwrap-result res)]
      (is (not (success? error)) "(ns my.namespace (:require [clojure set string])) should not succeed. Prefix lists are not supported.")
      (is (valid-eval-error? error) "(ns my.namespace (:require [clojure set string])) should be an instance of js/Error")
      (is (re-find #"Only :as and :refer options supported in :require / :require-macros;" (extract-message error))
          "(ns my.namespace (:require [clojure set string])) should have correct error message.")
      (repl/reset-env!))

    ;; http://stackoverflow.com/questions/24463469/is-it-possible-to-use-refer-all-in-a-clojurescript-require
    (let [res (read-eval-call "(ns my.namespace (:require [clojure.string :refer :all]))")
          error (unwrap-result res)]
      (is (not (success? error)) "(ns my.namespace (:require [clojure.string :refer :all])) should not succeed. :refer :all is not allowed.")
      (is (valid-eval-error? error) "(ns my.namespace (:require [clojure.string :refer :all])) should be an instance of js/Error")
      (is (re-find #":refer must be followed by a sequence of symbols in :require / :require-macros;" (extract-message error))
          "(ns my.namespace (:require [clojure.string :refer :all])) should have correct error message.")
      (repl/reset-env!))

    (let [res (read-eval-call "(ns my.namespace (:refer-clojure :rename {print core-print}))")
          error (unwrap-result res)]
      (is (not (success? error)) "(ns my.namespace (:refer-clojure ...)) should not succeed. Only :exlude is allowed for :refer-clojure.")
      (is (valid-eval-error? error) "(ns my.namespace (:refer-clojure :rename {print core-print})) should be an instance of js/Error")
      (is (re-find #"Only \[:refer-clojure :exclude \(names\)\] form supported" (extract-message error))
          "(ns my.namespace (:refer-clojure :rename {print core-print})) should have correct error message.")
      (repl/reset-env!))

    (let [res (do (read-eval-call "(ns my.namespace (:refer-clojure :exclude [max]))")
                  (read-eval-call "(max 1 2 3)"))
          error (unwrap-result res)]
      (is (not (success? error)) "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should not succeed.")
      (is (valid-eval-error? error) "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should be an instance of js/Error")
      (is (re-find #"ERROR" (extract-message error))
          "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should have correct error message.")
      (repl/reset-env!))

    (let [res (do (read-eval-call "(ns my.namespace (:refer-clojure :exclude [max]))")
                  (read-eval-call "(min 1 2 3)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:refer-clojure ... :exclude)) and (min ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:refer-clojure ... :exclude)) and (min ...) should be a valid result.")
      (is (re-find #"1" out) "The result should be 1")
      (repl/reset-env! '[my.namespace clojure.set]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar.baz :refer [MyRecord]]))")
                  (read-eval-call "(apply str ((juxt :first :second) (MyRecord. \"ABC\" \"DEF\")))"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ... )) and (apply str ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (apply str ...) should be a valid result.")
      (is (re-find #"ABCDEF" out) "The result should be ABCDEF")
      (repl/reset-env! '[my.namespace foo.bar.baz]))

    ;; even if not idiomatic, it should work also with "import"
    (let [res (do (read-eval-call "(ns my.namespace (:import foo.bar.baz [MyRecord]))")
                  (read-eval-call "(apply str ((juxt :first :second) (foo.bar.baz.MyRecord. \"ABC\" \"DEF\")))"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:import ... )) and (apply str ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:import ... )) and (apply str ...) should be a valid result.")
      (is (re-find #"ABCDEF" out) "The result should be ABCDEF")
      (repl/reset-env! '[my.namespace foo.bar.baz])))

  (deftest ns-macro-with-macros
    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar]))")
                  (read-eval-call "(foo.bar/add 10 10)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require-macros ...)) and (foo.bar/add 10 10) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...)) and (foo.bar/add 10 10) should be a valid result.")
      (is (= "20" out) "(foo.bar/add 10 10) should be 20")
      (repl/reset-env! '[my.namespace foo.bar]))

    ;; TB - this test fails but shouldn't, see https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#lisp
    ;; see also http://dev.clojure.org/jira/browse/CLJS-1449
    ;; I'm leaving it here for reference, it needs to be changed when the bug will be resolved
    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar :as f]))")
                  (read-eval-call "(f/add 10 10)"))
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:require-macros ...:as...)) and (f/add 10 10) should not succeed")
      (is (valid-eval-error? error) "(ns my.namespace (:require-macros ...:as...)) and (f/add 10 10) should be an instance of js/Error")
      ;; (is (= "20" out) "(f/add 10 10) should be 20")
      (is (re-find #"ERROR" (extract-message error))
          "(ns my.namespace (:require-macros ...:as...)) and (f/add 10 10) should have correct error message")
      (repl/reset-env! '[my.namespace foo.bar]))

    (let [res (do (read-eval-call-verbose "(ns my.namespace (:require-macros [foo.bar :refer [add]]))")
                  (read-eval-call-verbose "(add 10 10)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require-macros ... :refer ...)) and (add 10 10) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...:refer...)) and (add 10 10) should be a valid result.")
      (is (= "20" out) "(add 10 10) should be 20")
      (repl/reset-env! '[my.namespace foo.bar]))

    (let [res (do (read-eval-call "(ns my.namespace (:use-macros [foo.baz :only [mul]]))")
                  (read-eval-call "(mul 10 10)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:use-macros ...)) and (mul 10 10) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:use-macros ...)) and (mul 10 10) should be a valid result.")
      (is (= "100" out) "(mul 10 10) should be 100")
      (repl/reset-env! '[my.namespace foo.baz]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar :as f]))")
                  (read-eval-call "(f/add 5 7)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ...)) and (f/add 5 7) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ...)) and (f/add 5 7) should be a valid result.")
      (is (re-find #"\(\+ nil nil\)" out) "(f/add 5 7) should produce (+ nil nil)")
      (repl/reset-env! '[my.namespace foo.bar]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar :as f :include-macros true]))")
                  (read-eval-call "(f/add 5 7)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ...)) and (f/add 5 7) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ...)) and (f/add 5 7) should be a valid result.")
      (is (= "12" out) "(f/add 5 7) should be 12")
      (repl/reset-env! '[my.namespace foo.bar]))

    ;; TB - this test fails but shouldn't, see "Inline macro specifications" section
    ;; here https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#namespaces
    ;; see also http://dev.clojure.org/jira/browse/CLJS-1507
    ;; I'm leaving it here for reference, it needs to be changed when the bug will be resolved
    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.baz :refer-macros [mul]]))")
                  (read-eval-call "(mul 10 12)"))
          error (unwrap-result res)]
      (is (not (success? error)) "(ns my.namespace (:require ...)) and (mul 10 12) should not succeed")
      (is (valid-eval-error? error) "(ns my.namespace (:require ...)) and (mul 10 12) should be an instance of js/Error")
      ;;(is (= "120" out) "(mul 10 12) should be 120")
      (is (re-find #"ERROR" (extract-message error))
          "(ns my.namespace (:require ...)) and (mul 10 12) should have correct error message")
      (repl/reset-env! '[my.namespace foo.baz])))

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
    (ns-macro)
    (ns-macro-with-macros)
    (process-reload)
    (process-reload-all)))

(let [validated-echo-cb (partial repl/validated-call-back! echo-callback)
      target-opts (nodejs-options load/no-resource-load-fn!)
      read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)]
  (deftest require-when-read-file-return-nil
    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(doc clojure.string/trim)"))
          out (unwrap-result res)]
      (is (success? res) "(doc clojure.string/trim) should succeed.")
      (is (valid-eval-result? out) "(source clojure.string/trim) should be a valid result")
      (is (= "nil" out) "(source clojure.string/trim) should return nil")
      (repl/reset-env! '[clojure.string goog.string goog.string.StringBuffer]))))
