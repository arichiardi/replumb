(ns replumb.require-test
  (:require [clojure.string :as s]
            [cljs.test :refer-macros [is]]
            [replumb.core :as core :refer [success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result? extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.test-env :as e]
            [replumb.test-helpers :as h :include-macros true]))

;; Damian - Add js/COMPILED flag to cljs eval to turn off namespace already declared errors
;; AR - js/COMPILED goes here not in the runner otherwise node does not execute doo tests
;; AR - js/COMPILED is not needed after having correctly bootstrapped the
;; nodejs environment, see PR #57

;; https://github.com/ScalaConsultants/replumb/issues/47
(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.set)"
   "(doc clojure.set)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ " should succeed."))
    (is (valid-eval-result? docstring) (str _msg_ " should be a valid result"))
    (is (re-find #"Set operations such as union/intersection" docstring) (str _msg_ " should return valid docstring")))
  (_reset!_ '[clojure.set]))

;; https://github.com/ScalaConsultants/replumb/issues/59
(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(doc clojure.string/trim)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ " should succeed."))
    (is (valid-eval-result? docstring) (str _msg_ " should be a valid result"))
    (is (re-find #"Removes whitespace from both ends of string" docstring) (str _msg_ " should return valid docstring")))
  (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))

;; https://github.com/ScalaConsultants/replumb/issues/86
(h/read-eval-call-test e/*target-opts*
  ["(require '[clojure.string :as string])"
   "(doc string/trim)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ " should succeed."))
    (is (valid-eval-result? docstring) (str _msg_ " should be a valid result"))
    (is (re-find #"Removes whitespace from both ends of string" docstring) (str _msg_ " should return valid docstring")))
  (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.walk)"
   "(dir clojure.walk)"]
  (let [dirstring (unwrap-result @_res_)
        expected (s/join \newline ["keywordize-keys"
                                   "postwalk"
                                   "postwalk-replace"
                                   "prewalk"
                                   "prewalk-replace"
                                   "stringify-keys"
                                   "walk"])]
    (is (success? @_res_) (str _msg_ " should succeed"))
    (is (valid-eval-result? dirstring) (str _msg_ " should be a valid result"))
    (is (= expected dirstring) (str _msg_ " should return valid docstring")))
  (_reset!_ '[clojure.walk]))

(h/read-eval-call-test e/*target-opts*
  ["(apropos \"join\")"]
  (let [result (unwrap-result @_res_)
        expected "(cljs.core/-disjoin cljs.core/-disjoin!)"]
    (is (success? @_res_) (str _msg_ " should succeed"))
    (is (valid-eval-result? result) (str _msg_ " should be a valid result"))
    (is (= expected result) (str _msg_ " should return valid docstring")))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(apropos \"join\")"]
  (let [result (unwrap-result @_res_)
        expected "(cljs.core/-disjoin cljs.core/-disjoin! clojure.string/join)"]
    (is (success? @_res_) (str _msg_ " should succeed"))
    (is (valid-eval-result? result) (str _msg_ " should be a valid result"))
    (is (= expected result) (str _msg_ " should return valid docstring")))
  (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))

;; note the lack of require
(h/read-eval-call-test e/*target-opts*
  ["(find-doc \"union\")"]
  (let [result (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ " should succeed"))
    (is (valid-eval-result? result) (str _msg_ " should be a valid result"))
    (is (= "nil" result) (str _msg_ " should return nil because clojure.set has not been required.")))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.set)"
   "(find-doc \"union\")"]
  (let [result (unwrap-result @_res_)
        expected "-------------------------
union
([] [s1] [s1 s2] [s1 s2 & sets])
  Return a set that is the union of the input sets
-------------------------
clojure.set
  Set operations such as union/intersection.
"]
    (is (success? @_res_) (str _msg_ " should succeed"))
    (is (valid-eval-result? result) (str _msg_ " should be a valid result"))
    (is (= expected result) (str _msg_ " should return a valid docstring.")))
  (_reset!_ '[clojure.set]))

;; without requiring clojure.string
(h/read-eval-call-test e/*target-opts*
  ["(find-doc \"[^(]newline[^s*]\")"]
  (let [result (unwrap-result @_res_)
        expected "-------------------------
*flush-on-newline*
  When set to true, output will be flushed whenever a newline is printed.

  Defaults to true.
"]
    (is (success? @_res_) (str _msg_ " should succeed"))
    (is (valid-eval-result? result) (str _msg_ " should  be a valid result"))
    (is (= expected result) (str _msg_ " should return valid docstring")))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(find-doc \"[^(]newline[^s*]\")"]
  (let [result (unwrap-result @_res_)
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
    (is (success? @_res_) (str _msg_ " should succeed"))
    (is (valid-eval-result? result) (str _msg_ " should be a valid result"))
    (is (= expected result) (str _msg_ " should return valid docstring")))
  (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/load.clj\")"]
  (let [result (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ " should succeed"))
    (is (valid-eval-result? result) (str _msg_ " should be a valid result"))
    (is (= "#'foo.load/c" result) (str _msg_ " should return #'foo.load/c (last evaluated expression)"))
    (is (= (repl/current-ns) 'cljs.user) (str _msg_ " should not change namespace")))
  (_reset!_ '[foo.load foo.bar.baz clojure.string goog.string goog.string.StringBuffer]))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/load.clj\")"
   "(in-ns 'foo.load)"
   "(+ (b) (c 49))"]
  (let [result (unwrap-result @_res_)]
    (is (success? @_res_) "(load-file ...), (in-ns ...) and (+ (b) (c 49)) should succeed")
    (is (valid-eval-result? result) "(load-file ...), (in-ns ...) and (+ (b) (c 49)) be a valid result")
    (is (= "100" result) "(load-file ...), (in-ns ...) and (+ (b) (c 49)) should return 100"))
  (_reset!_ '[foo.load foo.bar.baz clojure.string goog.string goog.string.StringBuffer]))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/probably-non-existing-file.clj\")"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(load-file \"foo/probably-non-existing-file.clj\") should not succeed")
    (is (valid-eval-error? error) "(load-file \"foo/probably-non-existing-file.clj\") should be an instance of js/Error")
    (is (re-find #"Could not load file foo/probably-non-existing-file.clj" (extract-message error))
        "(load-file \"foo/probably-non-existing-file.clj\") should have correct error message"))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/error_in_file.cljs\")"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(load-file \"foo/error-in-file.cljs\") should not succeed")
    (is (valid-eval-error? error) "(load-file \"foo/error-in-file.cljs\") should be an instance of js/Error")
    (is (re-find #"ERROR - Cannot read property 'call' of undefined" (extract-message error))
        "(load-file \"foo/error-in-file.cljs\") should have correct error message"))
  (_reset!_ '[foo.error-in-file]))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/load_require.cljs\")"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(load-file \"foo/load_require.cljs\") should not succeed")
    (is (valid-eval-error? error) "(load-file \"foo/load_require.cljs\") should be an instance of js/Error")
    (is (re-find #"ERROR - Cannot read property 'call' of undefined" (extract-message error))
        "(load-file \"foo/load_require.cljs\") should have correct error message"))
  (_reset!_ '[foo.load-require]))

;; AR - Test for "No *load-fn* when requiring a namespace in browser #35"
;; Note there these are tests with a real *load-fn*
(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'foo.bar.baz) should succeed")
    (is (valid-eval-result? out) "(require 'foo.bar.baz) should be a valid result")
    (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) should not change namespace")
    (is (= "nil" out) "(require 'foo.bar.baz) should return \"nil\""))
  (_reset!_ '[foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"
   "foo.bar.baz/a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'foo.bar.baz) and foo.bar.baz/a should succeed")
    (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/a should be a valid result")
    (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/a should not change namespace")
    (is (= "\"whatever\"" out) "(require 'foo.bar.baz) and foo.bar.baz/a should return \"whatever\""))
  (_reset!_ '[foo.bar.baz]))

;; https://github.com/ScalaConsultants/replumb/issues/39
(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"
   "foo.bar.baz/const-a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'foo.bar.baz) and foo.bar.baz/const-a should succeed")
    (is (valid-eval-result? out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should be a valid result")
    (is (= 'cljs.user (repl/current-ns)) "(require 'foo.bar.baz) and foo.bar.baz/const-a should not change namespace")
    (is (= "1024" out) "(require 'foo.bar.baz) and foo.bar.baz/const-a should return \"1024\""))
  (_reset!_ '[foo.bar.baz]))

;; https://github.com/ScalaConsultants/replumb/issues/66
(h/read-eval-call-test e/*target-opts*
  ["(require '[foo.bar.baz :refer [a]])"
   "a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "\"whatever\"" out) (str _msg_ "should return \"whatever\"")))
  (_reset!_ '[foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(require '[foo.bar.baz :refer [const-a]])"
   "const-a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "1024" out) (str _msg_ "should return \"1024\"")))
  (_reset!_ '[foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(require 'yq)"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(require 'yq) should not succeed")
    (is (valid-eval-error? error) "(require 'yq) should be an instance of js/Error")
    (is (re-find #"No such namespace: yq" (extract-message error)) "(require 'yq) should have a valid error message."))
  (_reset!_ '[yg]))

(h/read-eval-call-test (merge e/*target-opts* {:foreign-libs [{:file "yayquery.js"
                                                               :provides ["yq"]}]})
  ["(require 'yq)"
   "(.. js/yq yayQuery getMessage)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'yq) and (.. js/yq yayQuery getMessage) should succeed")
    (is (valid-eval-result? out) "(require 'yq) and (.. js/yq yayQuery getMessage) should be a valid result")
    (is (= "\"Hello, world!\"" out) "(require 'yq) and (.. js/yq yayQuery getMessage) should return \"Hello, world!\""))
  (_reset!_ '[yq]))

;; AR - requiring clojure.string in turns imports goog.string
;; Node that goog.string should be never required but imported
(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'clojure.string) should succeed")
    (is (valid-eval-result? out) "(require 'clojure.string) should be a valid result")
    (is (= 'cljs.user (repl/current-ns)) "(require 'clojure.string) should not change namespace")
    (is (= "nil" out) "(require 'clojure.string) should return \"nil\""))
  (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(clojure.string/reverse \"clojurescript\")"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'clojure.string) and clojure.string/reverse should succeed")
    (is (valid-eval-result? out) "(require 'clojure.string) and clojure.string/reverse should be a valid result")
    (is (= 'cljs.user (repl/current-ns)) "(require 'clojure.string) and clojure.string/reverse should not change namespace")
    (is (= "\"tpircserujolc\"" out) "(require 'clojure.string) and clojure.string/reverse should return \"tpircserujolc\""))
  (_reset!_ '[clojure.string goog.string goog.string.StringBuffer]))

(h/read-eval-call-test e/*target-opts*
  ["(import 'goog.string.StringBuffer)"
   "(let [sb (StringBuffer. \"clojure\")]
      (.append sb \"script\")
      (.toString sb))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(import 'goog.string.StringBuffer) and .toString should succeed")
    (is (valid-eval-result? out) "(import 'goog.string.StringBuffer) and .toString should be a valid result")
    (is (= 'cljs.user (repl/current-ns)) "(import 'goog.string.StringBuffer) and .toString should not change namespace")
    (is (= "\"clojurescript\"" out) "(import 'goog.string.StringBuffer) and .toString should return \"clojurescript\""))
  (_reset!_ '[goog.string goog.string.StringBuffer]))

;; see https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#namespaces
;; for reference
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use [clojure.string :as s :only (trim)]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should not succeed")
    (is (valid-eval-error? error) "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should be an instance of js/Error")
    (is (re-find #"Only \[lib.ns :only \(names\)\] specs supported in :use / :use-macros;" (extract-message error))
        "(ns my.namespace (:use [clojure.string :as s :only (trim)])) should have correct error message"))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use [clojure.string :only (trim)]))"
   "(trim \"   clojure   \")"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:use ... )) and (trim ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:use ... )) and (trim ...) should be a valid result.")
    (is (re-find #"clojure" out) "The result should be \"clojure\""))
  (_reset!_ '[my.namespace clojure.string goog.string goog.string.StringBuffer]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [clojure.set :as s :refer [union]]))"
   "(s/difference (set (range 1 5)) (union #{1 2} #{2 3}))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ... )) and (s/difference ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (s/difference ...) should be a valid result.")
    (is (re-find #"\{4\}" out) "The result should be #{4}"))
  (_reset!_ '[my.namespace clojure.set]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require clojure.set))"
   "(clojure.set/difference (set (range 1 5)) (clojure.set/union #{1 2} #{2 3}))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ... )) and (clojure.set/difference ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (clojure.set/difference ...) should be a valid result.")
    (is (re-find #"\{4\}" out) "The result should be #{4}"))
  (_reset!_ '[my.namespace clojure.set]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [clojure set string]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(ns my.namespace (:require [clojure set string])) should not succeed. Prefix lists are not supported.")
    (is (valid-eval-error? error) "(ns my.namespace (:require [clojure set string])) should be an instance of js/Error")
    (is (re-find #"Only :as and :refer options supported in :require / :require-macros;" (extract-message error))
        "(ns my.namespace (:require [clojure set string])) should have correct error message."))
  (_reset!_))

;; http://stackoverflow.com/questions/24463469/is-it-possible-to-use-refer-all-in-a-clojurescript-require
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [clojure.string :refer :all]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(ns my.namespace (:require [clojure.string :refer :all])) should not succeed. :refer :all is not allowed.")
    (is (valid-eval-error? error) "(ns my.namespace (:require [clojure.string :refer :all])) should be an instance of js/Error")
    (is (re-find #":refer must be followed by a sequence of symbols in :require / :require-macros;" (extract-message error))
        "(ns my.namespace (:require [clojure.string :refer :all])) should have correct error message."))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:refer-clojure :rename {print core-print}))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(ns my.namespace (:refer-clojure ...)) should not succeed. Only :exlude is allowed for :refer-clojure.")
    (is (valid-eval-error? error) "(ns my.namespace (:refer-clojure :rename {print core-print})) should be an instance of js/Error")
    (is (re-find #"Only \[:refer-clojure :exclude \(names\)\] form supported" (extract-message error))
        "(ns my.namespace (:refer-clojure :rename {print core-print})) should have correct error message."))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:refer-clojure :exclude [max]))"
   "(max 1 2 3)"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should not succeed.")
    (is (valid-eval-error? error) "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should be an instance of js/Error")
    (is (re-find #"ERROR" (extract-message error))
        "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should have correct error message."))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:refer-clojure :exclude [max]))"
   "(min 1 2 3)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:refer-clojure ... :exclude)) and (min ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:refer-clojure ... :exclude)) and (min ...) should be a valid result.")
    (is (re-find #"1" out) "The result should be 1"))
  (_reset!_ '[my.namespace clojure.set]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.baz :refer [MyRecord]]))"
   "(apply str ((juxt :first :second) (MyRecord. \"ABC\" \"DEF\")))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ... )) and (apply str ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (apply str ...) should be a valid result.")
    (is (re-find #"ABCDEF" out) "The result should be ABCDEF"))
  (_reset!_ '[my.namespace foo.bar.baz]))

;; even if not idiomatic, it should work also with "import"
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:import foo.bar.baz [MyRecord]))"
   "(apply str ((juxt :first :second) (foo.bar.baz.MyRecord. \"ABC\" \"DEF\")))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:import ... )) and (apply str ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:import ... )) and (apply str ...) should be a valid result.")
    (is (re-find #"ABCDEF" out) "The result should be ABCDEF"))
  (_reset!_ '[my.namespace foo.bar.baz]))

;; quux.clj is a single .clj file and namespace
;; baz.clj file is paired with baz.cljs (but with no require-macros in baz.cljs)
;; core.cljc requires macros.cljc
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.quux]))"
   "(foo.bar.quux/mul-quux 2 2)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require-macros ...)) and (foo.bar.quux/mul-quux 2 2) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...)) and (foo.bar.quux/mul-quux 2 2) should be a valid result.")
    (is (= "4" out) "(foo.quux/mul-quux 2 2) should be 4"))
  (_reset!_ '[my.namespace foo.bar.quux]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.baz]))"
   "(foo.bar.baz/mul-baz 2 2)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require-macros ...)) and (foo.bar.baz/mul-baz 2 2) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...)) and (foo.bar.bar/mul-baz 2 2) should be a valid result.")
    (is (= "4" out) "(foo.bar.bar/mul-baz 2 2) should be 4"))
  (_reset!_ '[my.namespace foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.core]))"
   "(foo.bar.core/mul-core 30 1)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require-macros ...)) and (foo.bar.core/mul-core 30 1) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...])) and (foo.bar.core/mul-core 30 1) should be a valid result.")
    (is (= "30" out) "(foo.bar.core/mul-core 30 1) should be 30"))
  (_reset!_ '[my.namespace foo.bar.core foo.bar.macros]))

;; TB - this test fails but shouldn't, see https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#lisp
;; see also http://dev.clojure.org/jira/browse/CLJS-1449
;; I'm leaving it here for reference, it needs to be changed when the bug will be resolved
;; the issue in replumb is https://github.com/ScalaConsultants/replumb/issues/90
;; AR - this now succeeds, we were not correctly clearing the AST
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.baz :as f]))"
   "(f/mul-baz 20 20)"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should not succeed")
    (is (valid-eval-error? error) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should be an instance of js/Error")
    (is (re-find #"ERROR" (extract-message error)) "(ns my.namespace (:require-macros ...:as...)) and (f/mul-baz 20 20) should have correct error message"))
  (_reset!_ '[my.namespace foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.quux :refer [mul-quux]]))"
   "(mul-quux 3 3)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require-macros ... :refer ...)) and (mul-quux 3 3) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...:refer...)) and (mul-quux 3 3) should be a valid result.")
    (is (= "9" out) "(mul-quux 3 3) should be 9"))
  (_reset!_ '[my.namespace foo.bar.quux]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.baz :refer [mul-baz]]))"
   "(mul-baz 3 3)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require-macros ... :refer ...)) and (mul-baz 3 3) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require-macros ...:refer...)) and (mul-baz 3 3) should be a valid result.")
    (is (= "9" out) "(mul-baz 3 3) should be 9"))
  (_reset!_ '[my.namespace foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.core :refer [mul-core]]))"
   "(mul-core 30 3)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require-macros...:refer...])) and (mul-core 30 3) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require-macros...:refer...)) and (mul-core 30 3) should be a valid result")
    (is (= "90" out) "(mul-core 30 3) should be 90")
    (_reset!_ '[my.namespace foo.bar.core foo.bar.macros])))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use-macros [foo.bar.quux :only [mul-quux]]))"
   "(mul-quux 5 5)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:use-macros ...)) and (mul-quux 5 5) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:use-macros ...)) and (mul-quux 5 5) should be a valid result.")
    (is (= "25" out) "(mul-quux 25) should be 25"))
  (_reset!_ '[my.namespace foo.bar.quux]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use-macros [foo.bar.baz :only [mul-baz]]))"
   "(mul-baz 5 5)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:use-macros ...)) and (mul-baz 5 5) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:use-macros ...)) and (mul-baz 5 5) should be a valid result.")
    (is (= "25" out) "(mul-baz 5 5) should be 25"))
  (_reset!_ '[my.namespace foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use-macros [foo.bar.core :only [mul-core]]))"
   "(mul-core 30 4)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:use-macros...:only...])) and (mul-core 30 4) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:use-macros...:only...)) and (mul-core 30 4) should be a valid result")
    (is (= "120" out) "(mul-core 30 4) should be 120"))
  (_reset!_ '[my.namespace foo.bar.core foo.bar.macros]))

;; cannot require clj file
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.quux]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(ns my.namespace (:require [foo.bar.quux])) should not succeed")
    (is (valid-eval-error? error) "(ns my.namespace (:require [foo.bar.quux])) should be an instance of jsError.")
    (is (re-find #"No such namespace: foo.bar.quux" (extract-message error)) "(ns my.namespace (:require [foo.bar.quux])) should have a valid error message."))
  (_reset!_ '[my.namespace]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.core]))"
   "(foo.bar.core/add-five 30)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require...])) and (foo.bar.core/add-five 30) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require...])) and (foo.bar.core/add-five 30) should be a valid result")
    (is (= "35" out) "(foo.bar.core/add-five 30) should be 35"))
  (_reset!_ '[my.namespace foo.bar.core foo.bar.macros]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.core :as f]))"
   "(f/add-five 31)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require...:as...])) and (f/add-five 31) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require...:as...])) and (f/add-five 31) should be a valid result")
    (is (= "36" out) "(f/add-five 31) should be 36"))
  (_reset!_ '[my.namespace foo.bar.core foo.bar.macros]))

;; Was https://github.com/ScalaConsultants/replumb/issues/91
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.baz :include-macros true]))"
   "(foo.bar.baz/mul-baz 6 6)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ... :include-macros ...)) and (f/mul-baz 6 6) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ...:include-macros...)) and (f/mul-baz 6 6) should be a valid result.")
    (is (= "36" out) "(f/mul-baz 6 6) should be 36"))
  (_reset!_ '[my.namespace foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.core :include-macros true]))"
   "(foo.bar.core/mul-core 30 5)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require...:include-macros...])) and (foo.bar.core/mul-core 30 5) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require...:as...])) and (foo.bar.core/mul-core 30 5) should be a valid result")
    (is (= "150" out) "(foo.bar.core/mul-core 30 5) should be 150"))
  (_reset!_ '[my.namespace foo.bar.core foo.bar.macros]))

;; Was https://github.com/ScalaConsultants/replumb/issues/91
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.baz :refer-macros [mul-baz]]))"
   "(mul-baz 10 12)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ...:refer-macros...)) and (mul-baz 10 12) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ...:refer-macros)) and (mul-baz 10 12) should be a valid result")
    (is (= "120" out) "(mul-baz 10 12) should be equal to 120"))
  (_reset!_ '[my.namespace foo.bar.baz]))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.core :refer-macros [mul-core]]))"
   "(mul-core 10 20)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ...:refer-macros...)) and (mul-core 10 20) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ...:refer-macros...)) and (mul-core 10 20) should have a valid result")
    (is (= "200" out ) "(mul-core 10 20) should produce 200"))
  (_reset!_ '[my.namespace foo.bar.core foo.bar.macros]))

;; see "loop" section here: http://blog.fikesfarm.com/posts/2015-12-18-clojurescript-macro-tower-and-loop.html
;; but it does not work in JS ClojureScript
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.self]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed"))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (re-find #"Maximum call stack size exceeded" (extract-message error)) (str _msg_ "should have correct error message"))))

;; AR - The following test interact with the filesystem
(when (and e/*write-file-fn* e/*delete-file-fn*)
  (let [alterable-core-path "dev-resources/private/test/src/cljs/alterable/core.cljs"
        pre-content "(ns alterable.core)\n\n(def b \"pre\")"
        post-content "(ns alterable.core)\n\n(def b \"post\")"]

    (h/read-eval-call-test e/*target-opts*
      [:before (e/*write-file-fn* alterable-core-path pre-content)
       "(require 'alterable.core)"
       "alterable.core/b"
       :after (e/*delete-file-fn* alterable-core-path)]
      (let [out (unwrap-result @_res_ true)]
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (= 'cljs.user (repl/current-ns)) (str _msg_ " should not change namespace"))
        (is (= "\"pre\"" out) " should return \"pre\""))
      (_reset!_ '[alterable.core]))

    (h/read-eval-call-test e/*target-opts*
      [:before (e/*write-file-fn* alterable-core-path post-content)
       "(require 'alterable.core :reload)"
       "alterable.core/b"
       :after (e/*delete-file-fn* alterable-core-path)]
      (let [out (unwrap-result @_res_)]
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (= 'cljs.user (repl/current-ns)) (str _msg_ " should not change namespace"))
        (is (= "\"post\"" out) (str _msg_ " should return \"post\"")))
      (_reset!_ '[alterable.core])))

  (let [alterable-core-path "dev-resources/private/test/src/cljs/alterable/core.cljs"
        alterable-utils-path "dev-resources/private/test/src/cljs/alterable/utils.cljs"
        utils-pre-content "(ns alterable.utils)\n\n(def c \"pre\")"
        utils-post-content "(ns alterable.utils)\n\n(def c \"post\")"
        core-pre-content "(ns alterable.core\n  (:require alterable.utils))\n\n(def b (str alterable.utils/c))"
        core-post-content "(ns alterable.core\n  (:require alterable.utils))\n\n(def b (str alterable.utils/c))"]

    (h/read-eval-call-test e/*target-opts*
      [:before (do (e/*write-file-fn* alterable-utils-path utils-pre-content)
                   (e/*write-file-fn* alterable-core-path core-pre-content))
       "(require 'alterable.core)"
       "alterable.core/b"
       :after (do (e/*delete-file-fn* alterable-core-path)
                  (e/*delete-file-fn* alterable-utils-path))]
      (let [out (unwrap-result @_res_)]
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (= 'cljs.user (repl/current-ns)) (str _msg_ " should not change namespace"))
        (is (= "\"pre\"" out) (str _msg_ " should return \"pre\"")))
      (_reset!_ '[alterable.core alterable.utils]))

    (h/read-eval-call-test e/*target-opts*
      [:before (do (e/*write-file-fn* alterable-utils-path utils-pre-content)
                   (e/*write-file-fn* alterable-core-path core-pre-content))
       "(require 'alterable.utils)"
       "alterable.utils/c"
       :after (do (e/*delete-file-fn* alterable-core-path)
                  (e/*delete-file-fn* alterable-utils-path))]
      (let [out (unwrap-result @_res_)]
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (= 'cljs.user (repl/current-ns)) (str _msg_ " should not change namespace"))
        (is (= "\"pre\"" out) (str _msg_ " should return \"pre\"")))
      (_reset!_ '[alterable.utils]))

    (h/read-eval-call-test e/*target-opts*
      [:before (do (e/*write-file-fn* alterable-utils-path utils-post-content)
                   (e/*write-file-fn* alterable-core-path core-post-content))
       "(require 'alterable.core :reload-all)"
       "alterable.core/b"
       :after (do (e/*delete-file-fn* alterable-core-path)
                  (e/*delete-file-fn* alterable-utils-path))]
      (let [out (unwrap-result @_res_)]
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (= 'cljs.user (repl/current-ns)) (str _msg_ " should not change namespace"))
        (is (= "\"post\"" out) (str _msg_ " should return \"post\"")))
      (_reset!_ '[alterable.core alterable.utils]))))
