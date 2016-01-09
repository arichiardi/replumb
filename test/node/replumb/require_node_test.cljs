(ns replumb.require-node-test
  (:require [cljs.test :refer-macros [deftest is]]
            [clojure.string :as s]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [nodejs-options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.ast :as ast]
            [replumb.load :as load]
            [replumb.nodejs.io :as io]))

;; Damian - Add js/COMPILED flag to cljs eval to turn off namespace already declared errors
;; AR - js/COMPILED goes here not in the runner otherwise node does not execute doo tests
;; AR - js/COMPILED is not needed after having correctly bootstrapped the
;; nodejs environment, see PR #57
(let [src-paths ["dev-resources/private/test/node/compiled/out"
                 "dev-resources/private/test/src/cljs"
                 "dev-resources/private/test/src/clj"
                 "dev-resources/private/test/src/cljc"]
      target-opts (nodejs-options src-paths io/read-file!)
      validated-echo-cb (partial repl/validated-call-back! target-opts echo-callback)
      reset-env! (partial repl/reset-env! target-opts)
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
      (reset-env! '[clojure.set]))

    ;; https://github.com/ScalaConsultants/replumb/issues/59
    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(doc clojure.string/trim)"))
          docstring (unwrap-result res)]
      (is (success? res) "(require ...) and (doc clojure.string/trim) should succeed.")
      (is (valid-eval-result? docstring) "(require ...) and (doc clojure.string/trim) should be a valid result")
      (is (re-find #"Removes whitespace from both ends of string" docstring) "(require ...) and (doc clojure.string/trim) should return valid docstring")
      (reset-env! '[clojure.string goog.string goog.string.StringBuffer]))

    ;; https://github.com/ScalaConsultants/replumb/issues/86
    (let [res (do (read-eval-call "(require '[clojure.string :as string])")
                  (read-eval-call "(doc string/trim)"))
          docstring (unwrap-result res)]
      (is (success? res) "(require '[clojure.string :as string]) and (doc string/trim) should succeed.")
      (is (valid-eval-result? docstring) "(require '[clojure.string :as string]) and (doc string/trim) should be a valid result")
      (is (re-find #"Removes whitespace from both ends of string" docstring) "(require '[clojure.string :as string]) and (doc string/trim) should return valid docstring")
      (reset-env! '[clojure.string goog.string goog.string.StringBuffer])))

  (deftest require+dir
    (let [res (do (read-eval-call "(require 'clojure.walk)")
                  (read-eval-call "(dir clojure.walk)"))
          dirstring (unwrap-result res)
          expected (s/join \newline ["keywordize-keys"
                                     "postwalk"
                                     "postwalk-replace"
                                     "prewalk"
                                     "prewalk-replace"
                                     "stringify-keys"
                                     "walk"])]
      (is (success? res) "(dir clojure.walk) should succeed")
      (is (valid-eval-result? dirstring) "(dir clojure.walk) should be a valid result")
      (is (= expected dirstring) "(dir walk) should return valid docstring")
      (reset-env! '[clojure.walk])))

  (deftest require+apropos
    (let [res (read-eval-call "(apropos \"join\")")
          result (unwrap-result res)
          expected "(cljs.core/-disjoin cljs.core/-disjoin!)"]
      (is (success? res) "(apropos \"join\") should succeed")
      (is (valid-eval-result? result) "(apropos \"join\") should be a valid result")
      (is (= expected result) "(apropos \"join\") should return valid docstring")
      (reset-env!))

    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(apropos \"join\")"))
          result (unwrap-result res)
          expected "(cljs.core/-disjoin cljs.core/-disjoin! clojure.string/join)"]
      (is (success? res) "(require ...) and (apropos \"join\") should succeed")
      (is (valid-eval-result? result) "(require ...) and (apropos \"join\") should be a valid result")
      (is (= expected result) "(require ...) and (apropos \"join\") should return valid docstring")
      (reset-env! '[clojure.string goog.string goog.string.StringBuffer])))

  (deftest require+find-doc
    ;; note the lack of require
    (let [res (read-eval-call "(find-doc \"union\")")
          result (unwrap-result res)]
      (is (success? res) "(find-doc \"union\") should succeed")
      (is (valid-eval-result? result) "(find-doc \"union\") should be a valid result")
      (is (= "nil" result) "(find-doc \"union\") should return nil because clojure.set has not been required.")
      (reset-env!))

    (let [res (do (read-eval-call "(require 'clojure.set)")
                  (read-eval-call "(find-doc \"union\")"))
          result (unwrap-result res)
          expected "-------------------------
union
([] [s1] [s1 s2] [s1 s2 & sets])
  Return a set that is the union of the input sets
-------------------------
clojure.set
  Set operations such as union/intersection.
"]
      (is (success? res) "(find-doc \"union\") should succeed")
      (is (valid-eval-result? result) "(find-doc \"union\") should be a valid result")
      (is (= expected result) "(find-doc \"union\") should return a valid docstring.")
      (reset-env! '[clojure.set]))

    ;; without requiring clojure.string
    (let [res (read-eval-call "(find-doc \"[^(]newline[^s*]\")")
          result (unwrap-result res)
          expected "-------------------------
*flush-on-newline*
  When set to true, output will be flushed whenever a newline is printed.

  Defaults to true.
"]
      (is (success? res) "(find-doc \"[^(]newline[^s*]\") should succeed")
      (is (valid-eval-result? result) "(find-doc \"[^(]newline[^s*]\") should be a valid result")
      (is (= expected result) "(find-doc \"[^(]newline[^s*]\") should return valid docstring")
      (reset-env!))

    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(find-doc \"[^(]newline[^s*]\")"))
          result (unwrap-result res)
          expected "-------------------------
*flush-on-newline*
  When set to true, output will be flushed whenever a newline is printed.

  Defaults to true.
-------------------------
trim-newline
([s])
  Removes all trailing newline \\n or return \\r characters from
  string.  Similar to Perl's chomp.
"]
      (is (success? res) "(require ...) and (find-doc \"[^(]newline[^s*]\") should succeed")
      (is (valid-eval-result? result) "(require ...) and (find-doc \"[^(]newline[^s*]\") should be a valid result")
      (is (= expected result) "(require ...) and (find-doc \"[^(]newline[^s*]\") should return valid docstring")
      (reset-env! '[clojure.string goog.string goog.string.StringBuffer])))

  (deftest process-load-file
    (let [res (read-eval-call "(load-file \"foo/load.clj\")")
          result (unwrap-result res)]
      (is (success? res) "(load-file \"foo/load.clj\") should succeed")
      (is (valid-eval-result? result) "(load-file \"foo/load.clj\") be a valid result")
      (is (= "#'foo.load/c" result) "(load-file \"foo/load.clj\") should return #'foo.load/c (last evaluated expression)")
      (is (= (repl/current-ns) 'cljs.user) "(load-file \"foo/load.clj\") should not change namespace")
      (reset-env! '[foo.load foo.bar.baz clojure.string goog.string goog.string.StringBuffer]))

    (let [res (do (read-eval-call "(load-file \"foo/load.clj\")")
                  (read-eval-call "(in-ns 'foo.load)")
                  (read-eval-call "(+ (b) (c 49))"))
          result (unwrap-result res)]
      (is (success? res) "(load-file ...), (in-ns ...) and (+ (b) (c 49)) should succeed")
      (is (valid-eval-result? result) "(load-file ...), (in-ns ...) and (+ (b) (c 49)) be a valid result")
      (is (= "100" result) "(load-file ...), (in-ns ...) and (+ (b) (c 49)) should return 100")
      (reset-env! '[foo.load foo.bar.baz clojure.string goog.string goog.string.StringBuffer]))

    (let [res (read-eval-call "(load-file \"foo/probably-non-existing-file.clj\")")
          error (unwrap-result res)]
      (is (not (success? res)) "(load-file \"foo/probably-non-existing-file.clj\") should not succeed")
      (is (valid-eval-error? error) "(load-file \"foo/probably-non-existing-file.clj\") should be an instance of js/Error")
      (is (re-find #"Could not load file foo/probably-non-existing-file.clj" (extract-message error))
          "(load-file \"foo/probably-non-existing-file.clj\") should have correct error message")
      (reset-env!))

    (let [res (read-eval-call "(load-file \"foo/error_in_file.cljs\")")
          error (unwrap-result res)]
      (is (not (success? res)) "(load-file \"foo/error-in-file.cljs\") should not succeed")
      (is (valid-eval-error? error) "(load-file \"foo/error-in-file.cljs\") should be an instance of js/Error")
      (is (re-find #"ERROR - Cannot read property 'call' of undefined" (extract-message error))
          "(load-file \"foo/error-in-file.cljs\") should have correct error message")
      (reset-env! '[foo.error-in-file]))

    (let [res (read-eval-call "(load-file \"foo/load_require.cljs\")")
          error (unwrap-result res)]
      (is (not (success? res)) "(load-file \"foo/load_require.cljs\") should not succeed")
      (is (valid-eval-error? error) "(load-file \"foo/load_require.cljs\") should be an instance of js/Error")
      (is (re-find #"ERROR - Cannot read property 'call' of undefined" (extract-message error))
          "(load-file \"foo/load_require.cljs\") should have correct error message")
      (reset-env! '[foo.load-require])))

  (deftest process-require
    ;; AR - Test for "No *load-fn* when requiring a namespace in browser #35"
    ;; Note there these are tests with a real *load-fn*
    (let [res (read-eval-call "(require 'foo.bar.baz)")
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) should not change namespace")
      (is (= "nil" out) "(require 'foo.bar.baz) should return \"nil\"")
      (reset-env! '[foo.bar.baz]))

    (let [res (do (read-eval-call "(require 'foo.bar.baz)")
                  (read-eval-call "foo.bar.baz/a"))
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) and foo.bar.baz/a should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/a should not change namespace")
      (is (= "\"whatever\"" out) "(require 'foo.bar.baz) and foo.bar.baz/a should return \"whatever\"")
      (reset-env! '[foo.bar.baz]))

    ;; https://github.com/ScalaConsultants/replumb/issues/39
    (let [res (do (read-eval-call "(require 'foo.bar.baz)")
                  (read-eval-call "foo.bar.baz/const-a"))
          out (unwrap-result res)]
      (is (success? res) "(require 'foo.bar.baz) and foo.bar.baz/const-a should succeed")
      (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/const-a should not change namespace")
      (is (= "1024" out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should return \"1024\"")
      (reset-env! '[foo.bar.baz]))

    ;; AR - Upstream problem (already solved)
    ;; https://github.com/ScalaConsultants/replumb/issues/66
    #_(let [res (do (read-eval-call "(require '[foo.bar.baz :refer [a]])")
                    (read-eval-call "a"))
            out (unwrap-result res)]
        (is (success? res) "(require '[foo.bar.baz :refer [a]]) and a should succeed")
        (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [a]]) and a should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require '[foo.bar.baz :refer [a]]) and a should not change namespace")
        (is (= "\"whatever\"" out) "(require '[foo.bar.baz :refer [a]]) and a should return \"whatever\"")
        (reset-env! '[foo.bar.baz]))
    #_(let [res (do (read-eval-call "(require '[foo.bar.baz :refer [const-a]])")
                    (read-eval-call "const-a"))
            out (unwrap-result res)]
        (is (success? res) "(require '[foo.bar.baz :refer [const-a]]) and const-a should succeed")
        (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [const-a]]) and const-a should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require '[foo.bar.baz :refer [const-a]]) and const-a should not change namespace")
        (is (= "1024" out) "(require '[foo.bar.baz :refer [const-a]]) and const-a should return 1024")
        (reset-env! '[foo.bar.baz])))

  (deftest process-goog-import
    ;; AR - requiring clojure.string in turns imports goog.string
    ;; Node that goog.string should be never required but imported
    (let [res (read-eval-call "(require 'clojure.string)")
          out (unwrap-result res)]
      (is (success? res) "(require 'clojure.string) should succeed")
      (is (valid-eval-result? out) "(require 'clojure.string) should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'clojure.string) should not change namespace")
      (is (= "nil" out) "(require 'clojure.string) should return \"nil\"")
      (reset-env! '[clojure.string goog.string goog.string.StringBuffer]))

    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(clojure.string/reverse \"clojurescript\")"))
          out (unwrap-result res)]
      (is (success? res) "(require 'clojure.string) and clojure.string/reverse should succeed")
      (is (valid-eval-result? out) "(require 'clojure.string) and clojure.string/reverse should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(require 'clojure.string) and clojure.string/reverse should not change namespace")
      (is (= "\"tpircserujolc\"" out) "(require 'clojure.string) and clojure.string/reverse should return \"tpircserujolc\"")
      (reset-env! '[clojure.string goog.string goog.string.StringBuffer]))

    (let [res (do (read-eval-call "(import 'goog.string.StringBuffer)")
                  (read-eval-call "(let [sb (StringBuffer. \"clojure\")]
                                     (.append sb \"script\")
                                     (.toString sb))"))
          out (unwrap-result res)]
      (is (success? res) "(import 'goog.string.StringBuffer) and .toString should succeed")
      (is (valid-eval-result? out) "(import 'goog.string.StringBuffer) and .toString should be a valid result")
      (is (= 'cljs.user (repl/current-ns)) "(import 'goog.string.StringBuffer) and .toString should not change namespace")
      (is (= "\"clojurescript\"" out) "(import 'goog.string.StringBuffer) and .toString should return \"clojurescript\"")
      (reset-env! '[goog.string goog.string.StringBuffer])))

  ;; see https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#namespaces
  ;; for reference
  (deftest ns-macro
    (let [res (read-eval-call "(ns my.namespace (:use [clojure.string :as s :only (trim)]))")
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should not succeed")
      (is (valid-eval-error? error) "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should be an instance of js/Error")
      (is (re-find #"Only \[lib.ns :only \(names\)\] specs supported in :use / :use-macros;" (extract-message error))
          "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should have correct error message")
      (reset-env!))

    (let [res (do (read-eval-call "(ns my.namespace (:use [clojure.string :only (trim)]))")
                  (read-eval-call "(trim \"   clojure   \")"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:use ... )) and (trim ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:use ... )) and (trim ...) should be a valid result.")
      (is (re-find #"clojure" out) "The result should be \"clojure\"")
      (reset-env! '[my.namespace clojure.string goog.string goog.string.StringBuffer]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [clojure.set :as s :refer [union]]))")
                  (read-eval-call "(s/difference (set (range 1 5)) (union #{1 2} #{2 3}))"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ... )) and (s/difference ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (s/difference ...) should be a valid result.")
      (is (re-find #"\{4\}" out) "The result should be #{4}")
      (reset-env! '[my.namespace clojure.set]))

    (let [res (do (read-eval-call "(ns my.namespace (:require clojure.set))")
                  (read-eval-call "(clojure.set/difference (set (range 1 5)) (clojure.set/union #{1 2} #{2 3}))"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ... )) and (clojure.set/difference ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (clojure.set/difference ...) should be a valid result.")
      (is (re-find #"\{4\}" out) "The result should be #{4}")
      (reset-env! '[my.namespace clojure.set]))

    (let [res (read-eval-call "(ns my.namespace (:require [clojure set string]))")
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:require [clojure set string])) should not succeed. Prefix lists are not supported.")
      (is (valid-eval-error? error) "(ns my.namespace (:require [clojure set string])) should be an instance of js/Error")
      (is (re-find #"Only :as and :refer options supported in :require / :require-macros;" (extract-message error))
          "(ns my.namespace (:require [clojure set string])) should have correct error message.")
      (reset-env!))

    ;; http://stackoverflow.com/questions/24463469/is-it-possible-to-use-refer-all-in-a-clojurescript-require
    (let [res (read-eval-call "(ns my.namespace (:require [clojure.string :refer :all]))")
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:require [clojure.string :refer :all])) should not succeed. :refer :all is not allowed.")
      (is (valid-eval-error? error) "(ns my.namespace (:require [clojure.string :refer :all])) should be an instance of js/Error")
      (is (re-find #":refer must be followed by a sequence of symbols in :require / :require-macros;" (extract-message error))
          "(ns my.namespace (:require [clojure.string :refer :all])) should have correct error message.")
      (reset-env!))

    (let [res (read-eval-call "(ns my.namespace (:refer-clojure :rename {print core-print}))")
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:refer-clojure ...)) should not succeed. Only :exlude is allowed for :refer-clojure.")
      (is (valid-eval-error? error) "(ns my.namespace (:refer-clojure :rename {print core-print})) should be an instance of js/Error")
      (is (re-find #"Only \[:refer-clojure :exclude \(names\)\] form supported" (extract-message error))
          "(ns my.namespace (:refer-clojure :rename {print core-print})) should have correct error message.")
      (reset-env!))

    (let [res (do (read-eval-call "(ns my.namespace (:refer-clojure :exclude [max]))")
                  (read-eval-call "(max 1 2 3)"))
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should not succeed.")
      (is (valid-eval-error? error) "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should be an instance of js/Error")
      (is (re-find #"ERROR" (extract-message error))
          "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should have correct error message.")
      (reset-env!))

    (let [res (do (read-eval-call "(ns my.namespace (:refer-clojure :exclude [max]))")
                  (read-eval-call "(min 1 2 3)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:refer-clojure ... :exclude)) and (min ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:refer-clojure ... :exclude)) and (min ...) should be a valid result.")
      (is (re-find #"1" out) "The result should be 1")
      (reset-env! '[my.namespace clojure.set]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar.baz :refer [MyRecord]]))")
                  (read-eval-call "(apply str ((juxt :first :second) (MyRecord. \"ABC\" \"DEF\")))"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ... )) and (apply str ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (apply str ...) should be a valid result.")
      (is (re-find #"ABCDEF" out) "The result should be ABCDEF")
      (reset-env! '[my.namespace foo.bar.baz]))

    ;; even if not idiomatic, it should work also with "import"
    (let [res (do (read-eval-call "(ns my.namespace (:import foo.bar.baz [MyRecord]))")
                  (read-eval-call "(apply str ((juxt :first :second) (foo.bar.baz.MyRecord. \"ABC\" \"DEF\")))"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:import ... )) and (apply str ...) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:import ... )) and (apply str ...) should be a valid result.")
      (is (re-find #"ABCDEF" out) "The result should be ABCDEF")
      (reset-env! '[my.namespace foo.bar.baz])))

  ;; quux.clj is a single .clj file and namespace
  ;; baz.clj file is paired with baz.cljs (but with no require-macros in baz.cljs)
  ;; core.cljc requires macros.cljc
  (deftest ns-macro-require-macros
    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar.quux]))")
                  (read-eval-call "(foo.bar.quux/mul-quux 2 2)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require-macros ...)) and (foo.bar.quux/mul-quux 2 2) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...)) and (foo.bar.quux/mul-quux 2 2) should be a valid result.")
      (is (= "4" out) "(foo.quux/mul-quux 2 2) should be 4")
      (reset-env! '[my.namespace foo.bar.quux]))

    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar.baz]))")
                  (read-eval-call "(foo.bar.baz/mul-baz 2 2)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require-macros ...)) and (foo.bar.baz/mul-baz 2 2) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...)) and (foo.bar.bar/mul-baz 2 2) should be a valid result.")
      (is (= "4" out) "(foo.bar.bar/mul-baz 2 2) should be 4")
      (reset-env! '[my.namespace foo.bar.baz]))

    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar.core]))")
                  (read-eval-call "(foo.bar.core/mul-core 30 1)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require-macros ...)) and (foo.bar.core/mul-core 30 1) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...])) and (foo.bar.core/mul-core 30 1) should be a valid result.")
      (is (= "30" out) "(foo.bar.core/mul-core 30 1) should be 30")
      (reset-env! '[my.namespace foo.bar.core foo.bar.macros])))

  (deftest ns-macro-require-macros-as
    ;; TB - this test fails but shouldn't, see https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#lisp
    ;; see also http://dev.clojure.org/jira/browse/CLJS-1449
    ;; I'm leaving it here for reference, it needs to be changed when the bug will be resolved
    ;; the issue in replumb is https://github.com/ScalaConsultants/replumb/issues/90
    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar.baz :as f]))")
                  (read-eval-call "(f/mul-baz 20 20)"))
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should not succeed")
      (is (valid-eval-error? error) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should be an instance of js/Error")
      (is (re-find #"ERROR" (extract-message error)) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should have correct error message")
      (reset-env! '[my.namespace foo.bar.baz])))

  (deftest ns-macro-require-macros-refer
    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar.quux :refer [mul-quux]]))")
                  (read-eval-call "(mul-quux 3 3)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require-macros ... :refer ...)) and (mul-quux 3 3) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...:refer...)) and (mul-quux 3 3) should be a valid result.")
      (is (= "9" out) "(mul-quux 3 3) should be 9")
      (reset-env! '[my.namespace foo.bar.quux]))

    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar.baz :refer [mul-baz]]))")
                  (read-eval-call "(mul-baz 3 3)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require-macros ... :refer ...)) and (mul-baz 3 3) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...:refer...)) and (mul-baz 3 3) should be a valid result.")
      (is (= "9" out) "(mul-baz 3 3) should be 9")
      (reset-env! '[my.namespace foo.bar.baz]))

    (let [res (do (read-eval-call "(ns my.namespace (:require-macros [foo.bar.core :refer [mul-core]]))")
                  (read-eval-call "(mul-core 30 3)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require-macros...:refer...])) and (mul-core 30 3) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require-macros...:refer...)) and (mul-core 30 3) should be a valid result")
      (is (= "90" out) "(mul-core 30 3) should be 90")
      (reset-env! '[my.namespace foo.bar.core foo.bar.macros])))

  (deftest ns-macro-use-macros
    (let [res (do (read-eval-call "(ns my.namespace (:use-macros [foo.bar.quux :only [mul-quux]]))")
                  (read-eval-call "(mul-quux 5 5)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:use-macros ...)) and (mul-quux 5 5) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:use-macros ...)) and (mul-quux 5 5) should be a valid result.")
      (is (= "25" out) "(mul-quux 25) should be 25")
      (reset-env! '[my.namespace foo.bar.quux]))

    (let [res (do (read-eval-call "(ns my.namespace (:use-macros [foo.bar.baz :only [mul-baz]]))")
                  (read-eval-call "(mul-baz 5 5)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:use-macros ...)) and (mul-baz 5 5) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:use-macros ...)) and (mul-baz 5 5) should be a valid result.")
      (is (= "25" out) "(mul-baz 5 5) should be 25")
      (reset-env! '[my.namespace foo.bar.baz]))

    (let [res (do (read-eval-call "(ns my.namespace (:use-macros [foo.bar.core :only [mul-core]]))")
                  (read-eval-call "(mul-core 30 4)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:use-macros...:only...])) and (mul-core 30 4) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:use-macros...:only...)) and (mul-core 30 4) should be a valid result")
      (is (= "120" out) "(mul-core 30 4) should be 120")
      (reset-env! '[my.namespace foo.bar.core foo.bar.macros])))

  (deftest ns-macro-require
    ;; cannot require clj file
    (let [res (read-eval-call "(ns my.namespace (:require [foo.bar.quux]))")
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:require [foo.bar.quux])) should not succeed")
      (is (valid-eval-error? error) "(ns my.namespace (:require [foo.bar.quux])) should be an instance of jsError.")
      (is (re-find #"No such namespace: foo.bar.quux" (extract-message error)) "(ns my.namespace (:require [foo.bar.quux])) should have a valid error message.")
      (reset-env! '[my.namespace]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar.core]))")
                  (read-eval-call "(foo.bar.core/add-five 30)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require...])) and (foo.bar.core/add-five 30) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require...])) and (foo.bar.core/add-five 30) should be a valid result")
      (is (= "35" out) "(foo.bar.core/add-five 30) should be 35")
      (reset-env! '[my.namespace foo.bar.core foo.bar.macros]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar.core :as f]))")
                  (read-eval-call "(f/add-five 31)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require...:as...])) and (f/add-five 31) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require...:as...])) and (f/add-five 31) should be a valid result")
      (is (= "36" out) "(f/add-five 31) should be 36")
      (reset-env! '[my.namespace foo.bar.core foo.bar.macros])))

  (deftest ns-macro-require-include-macros
    ;; Was https://github.com/ScalaConsultants/replumb/issues/91
    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar.baz :include-macros true]))")
                  (read-eval-call "(foo.bar.baz/mul-baz 6 6)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ... :include-macros ...)) and (f/mul-baz 6 6) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ...:include-macros...)) and (f/mul-baz 6 6) should be a valid result.")
      (is (= "36" out) "(f/mul-baz 6 6) should be 36")
      (reset-env! '[my.namespace foo.bar.baz]))

    ;; note that the test outputs (* nil nil) but the correct result is 150
    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar.core :include-macros true]))")
                  (read-eval-call "(foo.bar.core/mul-core 30 5)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require...:include-macros...])) and (foo.bar.core/mul-core 30 5) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require...:as...])) and (foo.bar.core/mul-core 30 5) should be a valid result")
      (is (= "150" out) "(foo.bar.core/mul-core 30 5) should be 150")
      (reset-env! '[my.namespace foo.bar.core foo.bar.macros])))

  (deftest ns-macro-require-refer-macros
    ;; Was https://github.com/ScalaConsultants/replumb/issues/91
    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar.baz :refer-macros [mul-baz]]))")
                  (read-eval-call "(mul-baz 10 12)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ...:refer-macros...)) and (mul-baz 10 12) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ...:refer-macros)) and (mul-baz 10 12) should be a valid result")
      (is (= "120" out) "(mul-baz 10 12) should be equal to 120")
      (reset-env! '[my.namespace foo.bar.baz]))

    (let [res (do (read-eval-call "(ns my.namespace (:require [foo.bar.core :refer-macros [mul-core]]))")
                  (read-eval-call "(mul-core 10 20)"))
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace (:require ...:refer-macros...)) and (mul-core 10 20) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace (:require ...:refer-macros...)) and (mul-core 10 20) should have a valid result")
      (is (= "200" out ) "(mul-core 10 20) should produce 200")
      (reset-env! '[my.namespace foo.bar.core foo.bar.macros])))

  (deftest ns-macro-self-requiring-namespace
    ;; see "loop" section here: http://blog.fikesfarm.com/posts/2015-12-18-clojurescript-macro-tower-and-loop.html
    ;; but it does not work in JS ClojureScript
    (let [res (read-eval-call "(ns my.namespace (:require [foo.bar.self]))")
          error (unwrap-result res)]
      (is (not (success? res)) "(ns my.namespace (:require [foo.bar.self])) should not succeed")
      (is (valid-eval-error? error) "(ns my.namespace (:require [foo.bar.self])) should be an instance of js/Error")
      (is (re-find #"Maximum call stack size exceeded" (extract-message error)) "(ns my.namespace (:require [foo.bar.self])) should have correct error message")
      (reset-env!)))

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
        (reset-env! '[alterable.core]))

      ;; Writing "post" version of alterable.core
      (io/write-file! alterable-core-path post-content)
      (let [res (do (read-eval-call "(require 'alterable.core :reload)")
                    (read-eval-call "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core :reload) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core :reload) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core :reload) and alterable.core/b should not change namespace")
        (is (= "\"post\"" out) "(require 'alterable.core :reload) and alterable.core/b should return \"post\"")
        (reset-env! '[alterable.core]))
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
        (reset-env! '[alterable.core alterable.utils]))

      (let [res (do (read-eval-call "(require 'alterable.utils)")
                    (read-eval-call "alterable.utils/c"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.utils) and alterable.utils/c should succeed")
        (is (valid-eval-result? out) "(require 'alterable.utils) and alterable.utils/c should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.utils) and alterable.utils/c should not change namespace")
        (is (= "\"pre\"" out) "(require 'alterable.utils) and alterable.utils/c should return \"pre\"")
        (reset-env! '[alterable.utils]))

      ;; Writing "post" version of alterable.core & alterable.utils
      (io/write-file! alterable-utils-path utils-post-content)
      (let [res (do (read-eval-call "(require 'alterable.core :reload-all)")
                    (read-eval-call "alterable.core/b"))
            out (unwrap-result res)]
        (is (success? res) "(require 'alterable.core :reload) and alterable.core/b should succeed")
        (is (valid-eval-result? out) "(require 'alterable.core :reload) and alterable.core/b should be a valid result")
        (is (= 'cljs.user (repl/current-ns)) "(require 'alterable.core :reload) and alterable.core/b should not change namespace")
        (is (= "\"post\"" out) "(require 'alterable.core :reload) and alterable.core/b should return \"post\"")
        (reset-env! '[alterable.core alterable.utils]))
      (io/delete-file! alterable-core-path)
      (io/delete-file! alterable-utils-path))))

(let [target-opts (nodejs-options load/no-resource-load-fn!)
      validated-echo-cb (partial repl/validated-call-back! target-opts echo-callback)
      reset-env! (partial repl/reset-env! target-opts)
      read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)]

  (deftest require-when-read-file-return-nil
    (let [res (do (read-eval-call "(require 'clojure.string)")
                  (read-eval-call "(source clojure.string/trim)"))
          out (unwrap-result res)]
      (is (success? res) "(source clojure.string/trim) should succeed.")
      (is (valid-eval-result? out) "(source clojure.string/trim) should be a valid result")
      (is (= "nil" out) "(source clojure.string/trim) should return nil")
      (reset-env! '[clojure.string goog.string goog.string.StringBuffer])))

  (deftest load-file-when-read-file-retuns-nil
    (let [res (read-eval-call "(load-file \"foo/load.clj\")")
          result (unwrap-result res)]
      (is (success? res) "(load-file \"foo/load.clj\") should succeed")
      (is (valid-eval-result? result) "(load-file \"foo/load.clj\") be a valid result")
      (is (= "nil" result) "(load-file \"foo/load.clj\") should return nil")
      (is (= (repl/current-ns) 'cljs.user) "(load-file \"foo/load.clj\") should not change namespace")
      (reset-env! '[foo.load]))))

;; AR - we need to force the order so that we can force re-init at the beginning
(defn test-ns-hook []
  (repl/force-init!)
  (require+doc)
  (require+dir)
  (require+apropos)
  (require+find-doc)
  (process-load-file)
  (process-require)
  (process-goog-import)
  (ns-macro)

  (ns-macro-require-refer-macros)
  (ns-macro-require-include-macros)
  (ns-macro-require-macros)
  (ns-macro-require-macros-refer)
  (ns-macro-use-macros)
  (ns-macro-require)
  (ns-macro-require-macros-as)
  (ns-macro-self-requiring-namespace)

  (process-reload)
  (process-reload-all)

  (require-when-read-file-return-nil)
  (load-file-when-read-file-retuns-nil))
