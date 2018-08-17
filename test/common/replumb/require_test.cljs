(ns replumb.require-test
  (:require [clojure.string :as s]
            [cljs.test :refer-macros [is]]
            [replumb.core :as core :refer [success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result? extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.test-env :as e]
            [replumb.test-helpers :as h :include-macros true]))

;; Random notes:
;; Damian - Add js/COMPILED flag to cljs eval to turn off namespace already declared errors
;; AR - js/COMPILED goes here not in the runner otherwise node does not execute doo tests
;; AR - js/COMPILED is not needed after having correctly bootstrapped the nodejs environment, see PR #57

;; https://github.com/Lambda-X/replumb/issues/66
(h/read-eval-call-test e/*target-opts*
  ["(require '[clojure.string :refer [drim]])"
   "(require '[clojure.string :refer [trim]])"
   "(doc trim)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? docstring) (str _msg_ "should be a valid result"))
    (is (re-find #"^-+" docstring) (str _msg_ "should start with -----------"))
    (is (re-find #"Removes whitespace from both ends of string" docstring) (str _msg_ "should return valid docstring"))))

;; https://github.com/ScalaConsultants/replumb/issues/47
(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.set)"
   "(doc clojure.set)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? docstring) (str _msg_ "should be a valid result"))
    (is (re-find #"^-+" docstring) (str _msg_ "should start with -----------"))
    (is (re-find #"Set operations such as union/intersection" docstring) (str _msg_ "should return valid docstring"))))

;; https://github.com/ScalaConsultants/replumb/issues/59
(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(doc clojure.string/trim)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? docstring) (str _msg_ "should be a valid result"))
    (is (re-find #"^-+" docstring) (str _msg_ "should start with -----------"))
    (is (re-find #"Removes whitespace from both ends of string" docstring) (str _msg_ "should return valid docstring"))))

;; https://github.com/ScalaConsultants/replumb/issues/86
(h/read-eval-call-test e/*target-opts*
  ["(require '[clojure.string :as string])"
   "(doc string/trim)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? docstring) (str _msg_ "should be a valid result"))
    (is (re-find #"^-+" docstring) (str _msg_ "should start with -----------"))
    (is (re-find #"Removes whitespace from both ends of string" docstring) (str _msg_ "should return valid docstring"))))

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
    (is (valid-eval-result? dirstring) (str _msg_ "should be a valid result"))
    (is (= expected dirstring) (str _msg_ "should return valid docstring"))))

(h/read-eval-call-test e/*target-opts*
  ["(apropos \"join\")"]
  (let [result (unwrap-result @_res_)
        expected "(cljs.core/-disjoin cljs.core/-disjoin!)"]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? result) (str _msg_ "should be a valid result"))
    (is (= expected result) (str _msg_ "should return valid docstring"))))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(apropos \"join\")"]
  (let [result (unwrap-result @_res_)
        expected "(cljs.core/-disjoin cljs.core/-disjoin! clojure.string/join)"]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? result) (str _msg_ "should be a valid result"))
    (is (= expected result) (str _msg_ "should return valid docstring"))))

;; note the lack of require
(h/read-eval-call-test e/*target-opts*
  ["(find-doc \"union\")"]
  (let [result (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? result) (str _msg_ "should be a valid result"))
    (is (= "nil" result) (str _msg_ "should return nil because clojure.set has not been required."))))

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
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? result) (str _msg_ "should be a valid result"))
    (is (= expected result) (str _msg_ "should return a valid docstring."))))

;; without requiring clojure.string
(h/read-eval-call-test e/*target-opts*
  ["(find-doc \"[^(]newline[^s*]\")"]
  (let [result (unwrap-result @_res_)
        expected #"-------------------------\s+\*flush-on-newline\*\s+When set to true"]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? result) (str _msg_ "should  be a valid result"))
    (is (re-find expected result) (str _msg_ "should return valid docstring"))))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(find-doc \"[^(]newline[^s*]\")"]
  (let [result (unwrap-result @_res_)
        expected1 #"-------------------------\s+\*flush-on-newline\*\s+When set to true"
        expected2 #"-------------------------\s+trim-newline\s\(\[s\]\)\s+Removes all trailing newline"]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? result) (str _msg_ "should be a valid result"))
    (is (re-find expected1 result) (str _msg_ "should return expected (part 1) docstring"))
    (is (re-find expected2 result) (str _msg_ "should return expected (part 2) docstring"))))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/load.clj\")"]
  (let [result (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? result) (str _msg_ "should be a valid result"))
    (is (= "#'foo.load/c" result) (str _msg_ "should return #'foo.load/c (last evaluated expression)"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/load.clj\")"
   "(in-ns 'foo.load)"
   "(+ (b) (c 49))"]
  (let [result (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? result) (str _msg_ "should be a valid result"))
    (is (= "100" result) (str _msg_ "should return 100"))))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/probably-non-existing-file.clj\")"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed"))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (re-find #"Could not load file foo/probably-non-existing-file.clj" (extract-message error))
        (str _msg_ "should have correct error message"))))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/error_in_file.cljs\")"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed"))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (re-find #"ERROR - Cannot read property 'call' of undefined" (extract-message error))
        (str _msg_ "should have correct error message"))))

(h/read-eval-call-test e/*target-opts*
  ["(load-file \"foo/load_require.cljs\")"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed"))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (re-find #"ERROR - Cannot read property 'call' of undefined" (extract-message error))
        (str _msg_ "should have correct error message"))))

;; AR - Test for "No *load-fn* when requiring a namespace in browser #35"
;; Note there these are tests with a real *load-fn*
(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "nil" out) (str _msg_ "should return \"nil\""))))

(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"
   "foo.bar.baz/a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "\"whatever\"" out) (str _msg_ "should return \"nil\""))))

;; https://github.com/ScalaConsultants/replumb/issues/39
(h/read-eval-call-test e/*target-opts*
  ["(require 'foo.bar.baz)"
   "foo.bar.baz/const-a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "1024" out) (str _msg_ "should return \"1024\""))))

;; https://github.com/ScalaConsultants/replumb/issues/66
(h/read-eval-call-test e/*target-opts*
  ["(require '[foo.bar.baz :refer [a]])"
   "a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "\"whatever\"" out) (str _msg_ "should return \"whatever\""))))

(h/read-eval-call-test e/*target-opts*
  ["(require '[foo.bar.baz :refer [const-a]])"
   "const-a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "1024" out) (str _msg_ "should return \"1024\""))))

(h/read-eval-call-test e/*target-opts*
  ["(require 'yq)"
   :after (repl/purge-cljs-user! '[yq])]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed"))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (re-find #"No such namespace: yq" (extract-message error)) (str _msg_ "should have correct error message"))))

(h/read-eval-call-test (merge e/*target-opts* {:foreign-libs [{:file "yayquery.js"
                                                               :provides ["yq"]}]})
  ["(require 'yq)"
   "(.. js/yq yayQuery getMessage)"
   :after (repl/purge-cljs-user! '[yq])]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "\"Hello, world!\"" out) (str _msg_ "should return \"Hello, world!\""))))

;; AR - requiring clojure.string in turns imports goog.string
;; Node that goog.string should be never required but imported
(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "nil" out) (str _msg_ "should return \"nil\""))))

(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"
   "(clojure.string/reverse \"clojurescript\")"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "\"tpircserujolc\"" out) (str _msg_ "should return \"tpircserujolc\""))))

(h/read-eval-call-test e/*target-opts*
  ["(import 'goog.string.StringBuffer)"
   "(let [sb (StringBuffer. \"clojure\")]
      (.append sb \"script\")
      (.toString sb))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
    (is (= "\"clojurescript\"" out) (str _msg_ "should return \"clojurescript\""))))

;; see https://github.com/clojure/clojurescript/wiki/Differences-from-Clojure#namespaces
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use [clojure.string :as s :only (trim)]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed"))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (re-find #"offending spec: \[clojure.string :as s :only \(trim\)\]" (extract-message error))
        (str _msg_ "have correct error message"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use [clojure.string :only (trim)]))"
   "(trim \"   clojure   \")"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (re-find #"clojure" out) (str _msg_ "should include \"clojure\""))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [clojure.set :as s :refer [union]]))"
   "(s/difference (set (range 1 5)) (union #{1 2} #{2 3}))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "#{4}" out) (str _msg_ "should return #{4}"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require clojure.set))"
   "(clojure.set/difference (set (range 1 5)) (clojure.set/union #{1 2} #{2 3}))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "#{4}" out) (str _msg_ "should return #{4}"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [clojure set string]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed. Prefix lists are not supported."))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (re-find #"offending spec: \[clojure set string\]" (extract-message error)) (str _msg_ "should have correct error message."))))

;; http://stackoverflow.com/questions/24463469/is-it-possible-to-use-refer-all-in-a-clojurescript-require
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [clojure.string :refer :all]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed. :refer :all is not allowed."))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (re-find #"Could not eval \(ns my.namespace \(:require \[clojure.string :refer :all\]\)\)" (extract-message error))
        (str _msg_ "should have correct error message."))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:refer-clojure :rename {print core-print}))"
   "(core-print \"print with renamed core-print\")"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "nil" out) (str _msg_ "should return nil after the print"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:refer-clojure :exclude [max]))"
   "(max 1 2 3)"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should not succeed.")
    (is (valid-eval-error? error) "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should be an instance of js/Error")
    (is (re-find #"ERROR" (extract-message error))
        "(ns my.namespace (:refer-clojure ... :exclude)) and (max ...) should have correct error message.")))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:refer-clojure :exclude [max]))"
   "(min 1 2 3)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:refer-clojure ... :exclude)) and (min ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:refer-clojure ... :exclude)) and (min ...) should be a valid result.")
    (is (re-find #"1" out) "The result should be 1")))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.baz :refer [MyRecord]]))"
   "(apply str ((juxt :first :second) (MyRecord. \"ABC\" \"DEF\")))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ... )) and (apply str ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ... )) and (apply str ...) should be a valid result.")
    (is (re-find #"ABCDEF" out) "The result should be ABCDEF")))

;; even if not idiomatic, it should work also with "import"
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:import foo.bar.baz [MyRecord]))"
   "(apply str ((juxt :first :second) (foo.bar.baz.MyRecord. \"ABC\" \"DEF\")))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:import ... )) and (apply str ...) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:import ... )) and (apply str ...) should be a valid result.")
    (is (re-find #"ABCDEF" out) "The result should be ABCDEF")))

;; quux.clj is a single .clj file and namespace
;; baz.clj file is paired with baz.cljs (but with no require-macros in baz.cljs)
;; core.cljc requires macros.cljc
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.quux]))"
   "(foo.bar.quux/mul-quux 2 2)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result."))
    (is (= "4" out) (str _msg_ "should be 4"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.baz]))"
   "(foo.bar.baz/mul-baz 2 2)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result."))
    (is (= "4" out) (str _msg_ "should be 4"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.core]))"
   "(foo.bar.core/mul-core 30 1)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result."))
    (is (= "30" out) (str _msg_ "should be 30"))))

;; https://github.com/Lambda-X/replumb/issues/90
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.baz :as f]))"
   "(f/mul-baz 20 20)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result."))
    (is (= "400" out) (str _msg_ "should be 400"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.quux :refer [mul-quux]]))"
   "(mul-quux 3 3)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result."))
    (is (= "9" out) (str _msg_ "should be 9"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.baz :refer [mul-baz]]))"
   "(mul-baz 3 3)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result."))
    (is (= "9" out) (str _msg_ "should be 9"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require-macros [foo.bar.core :refer [mul-core]]))"
   "(mul-core 30 3)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "90" out) (str _msg_ "should be 90"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use-macros [foo.bar.quux :only [mul-quux]]))"
   "(mul-quux 5 5)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result."))
    (is (= "25" out) (str _msg_ "should be 25"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use-macros [foo.bar.baz :only [mul-baz]]))"
   "(mul-baz 5 5)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result."))
    (is (= "25" out) (str _msg_ "should be 25"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:use-macros [foo.bar.core :only [mul-core]]))"
   "(mul-core 30 4)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "120" out) (str _msg_ "should be 120"))))

;; cannot require clj file
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.quux]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed"))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of jsError."))
    (is (re-find #"No such namespace: foo.bar.quux" (extract-message error)) (str _msg_ "should have a valid error message."))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.core]))"
   "(foo.bar.core/add-five 30)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "35" out) (str _msg_ "should be 35"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.core :as f]))"
   "(f/add-five 31)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require...:as...])) and (f/add-five 31) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require...:as...])) and (f/add-five 31) should be a valid result")
    (is (= "36" out) "(f/add-five 31) should be 36")))

;; Was https://github.com/ScalaConsultants/replumb/issues/91
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.baz :include-macros true]))"
   "(foo.bar.baz/mul-baz 6 6)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ... :include-macros ...)) and (f/mul-baz 6 6) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ...:include-macros...)) and (f/mul-baz 6 6) should be a valid result.")
    (is (= "36" out) "(f/mul-baz 6 6) should be 36")))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.core :include-macros true]))"
   "(foo.bar.core/mul-core 30 5)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require...:include-macros...])) and (foo.bar.core/mul-core 30 5) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require...:as...])) and (foo.bar.core/mul-core 30 5) should be a valid result")
    (is (= "150" out) "(foo.bar.core/mul-core 30 5) should be 150")))

;; Was https://github.com/ScalaConsultants/replumb/issues/91
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.baz :refer-macros [mul-baz]]))"
   "(mul-baz 10 12)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ...:refer-macros...)) and (mul-baz 10 12) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ...:refer-macros)) and (mul-baz 10 12) should be a valid result")
    (is (= "120" out) "(mul-baz 10 12) should be equal to 120")))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.core :refer-macros [mul-core]]))"
   "(mul-core 10 20)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(ns my.namespace (:require ...:refer-macros...)) and (mul-core 10 20) should succeed")
    (is (valid-eval-result? out) "(ns my.namespace (:require ...:refer-macros...)) and (mul-core 10 20) should have a valid result")
    (is (= "200" out ) "(mul-core 10 20) should produce 200")))

;; see "loop" section here: http://blog.fikesfarm.com/posts/2015-12-18-clojurescript-macro-tower-and-loop.html
;; but it does not work in JS ClojureScript
;; AR - note that Node.js version 0.12.7 behaves differently locally and on
;; Jenkins. This is the reason of the two different error messages.
(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace (:require [foo.bar.self]))"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should not succeed"))
    (is (valid-eval-error? error) (str _msg_ "should be an instance of js/Error"))
    (is (or (re-find #"Maximum call stack size exceeded" (extract-message error))
            (re-find #"No such macros namespace" (extract-message error))) (str _msg_ "should have correct error message"))))

;; AR - The following writes on the filesystem
(when (and e/*write-file-fn* e/*delete-file-fn*)
  (let [alterable-core-path "dev-resources/private/test/src/cljs/alterable/core.cljs"
        pre-content "(ns alterable.core)\n\n(def b \"pre\")"
        post-content "(ns alterable.core)\n\n(def b \"post\")"]

    (h/read-eval-call-test e/*target-opts*
      [:before (e/*write-file-fn* alterable-core-path pre-content)
       "(require 'alterable.core)"
       "alterable.core/b"
       :after (e/*delete-file-fn* alterable-core-path)]
      (let [out (unwrap-result true @_res_)]
        (is (success? @_res_) (str _msg_ "should succeed"))
        (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
        (is (= 'cljs.user (repl/current-ns)) (str _msg_ "should not change namespace"))
        (is (= "\"pre\"" out) " should return \"pre\"")))

    (h/read-eval-call-test e/*target-opts*
      [:before (e/*write-file-fn* alterable-core-path post-content)
       "(require 'alterable.core :reload)"
       "alterable.core/b"
       :after (e/*delete-file-fn* alterable-core-path)]
      (let [out (unwrap-result @_res_)]
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (= 'cljs.user (repl/current-ns)) (str _msg_ " should not change namespace"))
        (is (= "\"post\"" out) (str _msg_ " should return \"post\"")))))

  ;; https://github.com/Lambda-X/replumb/issues/202
  (let [alterable-core-path "dev-resources/private/test/src/cljs/alterable/core.cljs"
        content "(ns alterable.core)\n\n(def b \"successful self-require!\")"]
    ;; AR - we don't swap any file here, we just make sure that a self-require
    ;; works
    (h/read-eval-call-test e/*target-opts*
      [:before (e/*write-file-fn* alterable-core-path content)
       "(require 'alterable.core)"
       "(in-ns 'alterable.core)"
       "(require 'alterable.core :reload)"
       "alterable.core/b"
       :after (e/*delete-file-fn* alterable-core-path)]
      (let [out (unwrap-result @_res_)]
        (is (success? @_res_) (str _msg_ " should succeed"))
        (is (valid-eval-result? out) (str _msg_ " should be a valid result"))
        (is (= 'alterable.core (repl/current-ns)) (str _msg_ " should change namespace"))
        (is (= "\"successful self-require!\"" out) (str _msg_ " should return \"post\"")))))

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
        (is (= "\"pre\"" out) (str _msg_ " should return \"pre\""))))

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
        (is (= "\"pre\"" out) (str _msg_ " should return \"pre\""))))

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
        (is (= "\"post\"" out) (str _msg_ " should return \"post\""))))))

(h/read-eval-call-test e/*target-opts*
  [:before (repl/force-init!)
   "(fun3 2)"]
  (let [res (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should fail."))
    (is (valid-eval-error? res) (str _msg_ "should be an error"))
    (is (re-find #"Cannot read property 'call' of undefined" (extract-message res)) (str _msg_ "should be 60"))))

(h/read-eval-call-test (assoc e/*target-opts* :preloads '[init-require.test1 init-require.test2])
  [:before (repl/force-init!)
   "(+ (init-require.test1/fun1 2) (init-require.test2/fun2 2))"]
  (let [res (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ " (where :preloads is a sequence of symbols) should succeed."))
    (is (valid-eval-result? res) (str _msg_ " (where :preloads is a sequence of symbols) should be a valid result"))
    (is (= "60" res) (str _msg_ "should be 60"))))

(h/read-eval-call-test (assoc e/*target-opts* :preloads {:use '#{[init-require.test1 :only [fun1]]
                                                                 [init-require.test2 :only [fun2]]}})
  [:before (repl/force-init!)
   "(+ (fun1 2) (fun2 2))"]
  (let [res (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? res) (str _msg_ "should be a valid result"))
    (is (= "60" res) (str _msg_ "should be 60"))))

(h/read-eval-call-test (assoc e/*target-opts* :preloads {:require '#{[init-require.test1 :as test1]
                                                                     [init-require.test2 :as test2]}})
  [:before (repl/force-init!)
   "(+ (test1/fun1 2) (test2/fun2 2))"]
  (let [res (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? res) (str _msg_ "should be a valid result"))
    (is (= "60" res) (str _msg_ "should be 120"))))

(h/read-eval-call-test (assoc e/*target-opts* :preloads {:require-macros '#{[init-require.test3 :refer [fun3]]}
                                                         :require '#{[init-require.test2 :refer [fun2]]
                                                                     init-require.test3}})
  [:before (repl/force-init!)
   "(+ (fun3 2) (fun2 2))"
   :after (repl/purge-cljs-user! '[init-require.test3])]
  (let [res (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? res) (str _msg_ "should be a valid result"))
    (is (= "100" res) (str _msg_ "should be 120"))))

(h/read-eval-call-test (assoc e/*target-opts* :preloads {:import #{foo.bar.baz.MyRecord}})
  [:before (repl/force-init!)
   "(apply str ((juxt :first :second) (foo.bar.baz.MyRecord. \"ABC\" \"DEF\")))"]
  (let [res (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? res) (str _msg_ "should be a valid result"))
    (is (re-find #"ABCDEF" res) "The result should be ABCDEF")))

(h/read-eval-call-test (assoc e/*target-opts* :preloads {:require #{}})
  [:before (repl/force-init!)
   "(def a 3)
   a"]
  (let [res (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? res) (str _msg_ "should be a valid result"))
    (is (= "3" res) (str _msg_ "should be 3"))))
