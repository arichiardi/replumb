(ns replumb.repl-test
  (:require [cljs.test :refer-macros [is]]
            [replumb.repl :as repl]
            [replumb.load :as load]
            [replumb.core :as core :refer [success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result? extract-message valid-eval-error? has-valid-warning?]]
            [replumb.test-env :as e]
            [replumb.test-helpers :as h :include-macros true]))

(h/read-eval-call-test e/*target-opts*
  ["def a \"bogus-op\""]
  (is (symbol? (repl/current-ns)) "The current ns should be a symbol"))

(h/read-eval-call-test e/*target-opts*
  ["def a \"bogus-op\""]
  (let [_ (repl/force-init!)]
    (is (not (:initializing? @repl/app-env)) "After force-init! :initializing? should be false before init")
    (is (:needs-init? @repl/app-env) "After force-init!, :needs-init? should be true before init")))

(h/read-eval-call-test e/*target-opts*
  ["def a \"bogus-op\""]
  (let [_ (repl/persist-init-opts! {:verbose true :src-paths ["src/a" "src/b"] :init-fn! #()})]
    (is (every? repl/init-option-set (keys (:previous-init-opts @repl/app-env))) "After persist-init-opts!, the app-env should contain the right init options")))

(h/read-eval-call-test e/*target-opts*
  ["def a \"bogus-op\""]
  (let [_ (repl/persist-init-opts! {:verbose true :load-fn! #() :custom :opts})]
    (is (not-any? repl/init-option-set (:previous-init-opts @repl/app-env)) "After persist-init-opts! but no option to persist, the app-env should not contain init options")))

(h/read-eval-call-test e/*target-opts*
  ["def a \"bogus-op\""]
  (let [init-map-atom (atom {})
        custom-init-fn (fn [init-map] (reset! init-map-atom init-map))
        _ (swap! repl/app-env merge {:initializing? false :needs-init? true})
        _ (repl/read-eval-call (merge e/*target-opts* {:init-fn! custom-init-fn}) (fn [_] nil) "(def c 4)")]
    (is (not (:initializing? @repl/app-env)) "Flag :initializing? should be false when the init exits")
    (is (not (:needs-init? @repl/app-env)) "Flag :needs-init? should be false when the init exits")
    (is (= '(def c 4) (:form @init-map-atom)) "Init map should have correct :form")
    (is (not (string? (:form @init-map-atom))) "Init map :form should not be a string")
    (is (= (repl/current-ns) (:ns @init-map-atom)) "Init map should have correct :ns")
    (is (symbol? (:ns @init-map-atom)) "Init map :ns should be a symbol")
    (is (= (:target e/*target-opts*) (:target @init-map-atom)) "Init map with custom init-fn! should have correct :target")))

(h/read-eval-call-test e/*target-opts*
  ["def a \"bogus-op\""]
  (let [_ (repl/read-eval-call (merge e/*target-opts* {:src-paths ["my/custom/path"]}) (fn [_] nil) "(def c 4)")
        previous-init-opts (:previous-init-opts @repl/app-env)]
    (is (some #{"my/custom/path"} (:src-paths previous-init-opts)) "After changing :src-paths the new app-env should contain the new path")))

(h/read-eval-call-test e/*target-opts*
  ["(throw (ex-info \"This is my custom error message %#FT%\" {:tag :exception}))"
   "*e"]
  (let [error (unwrap-result @_res_)]
    (is (success? @_res_) "Eval of *e with error should return successfully")
    (is (valid-eval-result? error) "Eval of *e with error should be a valid error")
    (is (re-find #"This is my custom error message %#FT%" error) "Eval of *e with error should return the correct message")))

(h/read-eval-call-test e/*target-opts*
  ["(throw (ex-info \"This is my custom error message %#FT%\" {:tag :exception}))"
   "(pst)"]
  (let [trace (unwrap-result @_res_)]
    (is (success? @_res_) "(pst) with previous error should return successfully")
    (is (valid-eval-result? trace) "(pst) with previous error should be a valid result")
    (is (re-find #"This is my custom error message %#FT%" trace) "(pst) with previous error should return the correct message")))

(h/read-eval-call-test e/*target-opts*
  ["(pst)"]
  (let [trace (unwrap-result @_res_)]
    (is (success? @_res_) "(pst) with no error should return successfully")
    (is (valid-eval-result? trace) "(pst) with no error should be a valid result")
    (is (= "nil" trace) "(pst) with no error should return nil")))

(h/read-eval-call-test e/*target-opts*
  ["(doc 'println)"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(doc 'symbol) should have correct error")
    (is (valid-eval-error? error) "(doc 'symbol) should result in an js/Error")))

(h/read-eval-call-test e/*target-opts*
  ["(doc println)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) "(doc symbol) should succeed")
    (is (valid-eval-result? docstring) "(doc symbol) should be a valid result")
    (is (re-find #"^-+" docstring) (str _msg_ "should start with -----------"))
    (is (re-find #"cljs\.core.{1}println" docstring) "(doc symbol) should return valid docstring")))

(h/read-eval-call-test e/*target-opts*
  ["(defn my-function \"This is my documentation\" [param] (param))"
   "(doc my-function)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? docstring) (str _msg_ "should be a valid result"))
    (is (re-find #"^-+" docstring) (str _msg_ "should start with -----------"))
    (is (re-find #"This is my documentation" docstring) (str _msg_ "should return valid docstring"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns myns.testns \"Docstring for namespace\")"
   "(doc myns.testns)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed." ))
    (is (valid-eval-result? docstring) (str _msg_ "should be a valid result"))
    (is (re-find #"^-+" docstring) (str _msg_ "should start with -----------"))
    (is (re-find #"Docstring for namespace" docstring) (str _msg_ "should return valid docstring"))))

(h/read-eval-call-test e/*target-opts*
  ["(doc ns-interns)"]
  (let [docstring (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed [issue #81]"))
    (is (valid-eval-result? docstring) (str _msg_ "should be a valid result [issue #81]"))
    (is (re-find #"^-+" docstring) (str _msg_ "should start with ----------- [issue #81]"))
    (is (re-find #"Returns a map of the intern mappings for the namespace" docstring) (str _msg_ "should return the correct docstring [issue #81]"))))

(h/read-eval-call-test e/*target-opts*
  ["(dir clojure.string)"]
  ;; note that we don't require first
  (let [dirstring (unwrap-result @_res_)]
    (is (success? @_res_) "(dir clojure.string) should succeed")
    (is (valid-eval-result? dirstring) "(dir clojure.string) should be a valid result")
    (is (= "nil" dirstring) "(dir clojure.string) should be \"nil\" because clojure.string has not been required first")))

;; test with string "tim"
(h/read-eval-call-test e/*target-opts*
  ["(apropos \"tim\")"]
  (let [result (unwrap-result @_res_)
        expected "(cljs.core/-deref-with-timeout cljs.core/dotimes cljs.core/system-time cljs.core/time)"]
    (is (success? @_res_) "(apropos \"tim\") should succeed")
    (is (valid-eval-result? result) "(apropos \"tim\") should be a valid result")
    (is (= expected result) "(apropos \"tim\") should return valid docstring")))

;; test with regular expression #"tim"
(h/read-eval-call-test e/*target-opts*
  ["(apropos #\"tim\")"]
  (let [result (unwrap-result @_res_)
        expected "(cljs.core/-deref-with-timeout cljs.core/dotimes cljs.core/system-time cljs.core/time)"]
    (is (success? @_res_) "(apropos #\"tim\") should succeed")
    (is (valid-eval-result? result) "(apropos #\"tim\") should be a valid result")
    (is (= expected result) "(apropos #\"tim\") should return valid docstring")))

;; test with regular expression #"t[i]me, containing metacharacters
(h/read-eval-call-test e/*target-opts*
  ["(apropos #\"t[i]me\")"]
  (let [result (unwrap-result @_res_)
        expected "(cljs.core/-deref-with-timeout cljs.core/dotimes cljs.core/system-time cljs.core/time)"]
    (is (success? @_res_) "(apropos  #\"t[i]me\") should succeed")
    (is (valid-eval-result? result) "(apropos  #\"t[i]me\") should be a valid result")
    (is (= expected result) "(apropos #\"t[i]me\") should return valid docstring")))

;; test with string "t[i]me"
;; the metacharacters in this case will be interpreted just as characteres
(h/read-eval-call-test e/*target-opts*
  ["(apropos \"t[i]me\")"]
  (let [result (unwrap-result @_res_)]
    (is (success? @_res_) "(apropos \"t[i]me\") should succeed")
    (is (valid-eval-result? result) "(apropos  #\"t[i]me\") should be a valid result")
    (is (= "nil" result) "(apropos \"t[i]me\") should return nil.")))

(h/read-eval-call-test e/*target-opts*
  ["(find-doc \"unguessable-string\")"]
  (let [result (unwrap-result @_res_)]
    (is (success? @_res_) "(find-doc \"unguessable-string\") should succeed")
    (is (valid-eval-result? result) "(find-doc \"unguessable-string\") should be a valid result")
    (is (= "nil" result) "(find-doc \"unguessable-string\") should return nil")))

(h/read-eval-call-test e/*target-opts*
  ["(find-doc \"^(di|a)ssoc.*!\")"]
  (let [result (unwrap-result @_res_)
        expected "-------------------------
assoc!
([tcoll key val] [tcoll key val & kvs])
  When applied to a transient map, adds mapping of key(s) to
  val(s). When applied to a transient vector, sets the val at index.
  Note - index must be <= (count vector). Returns coll.
-------------------------
dissoc!
([tcoll key] [tcoll key & ks])
  Returns a transient map that doesn't contain a mapping for key(s).
"]
    (is (success? @_res_) "(find-doc \"^(di|a)ssoc.*!\") should succeed")
    (is (valid-eval-result? result) "(find-doc \"^(di|a)ssoc.*!\") should be a valid result")
    (is (= expected result) "(find-doc \"^(di|a)ssoc.*!\") should return valid docstrings")))

(h/read-eval-call-test e/*target-opts*
  ["(find-doc \"select-keys\")"]
  (let [result (unwrap-result @_res_)
        expected "-------------------------
select-keys
([map keyseq])
  Returns a map containing only those entries in map whose key is in keys
"]
    (is (success? @_res_) "(find-doc \"select-keys\") should succeed")
    (is (valid-eval-result? result) "(find-doc \"select-keys\") should be a valid result")
    (is (= expected result) "(find-doc \"select-keys\") should return valid docstring")))

;; Damian - Add COMPILED flag to cljs eval to turn off namespace already declared errors
;; AR - COMPILED goes here not in the runner otherwise node does not execute doo tests
;; AR - js/COMPILED is not needed after having correctly bootstrapped the
;; browser environment, see PR #57
(h/read-eval-call-test e/*target-opts*
  ["(in-ns \"first.namespace\")"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(in-ns \"string\") should NOT succeed")
    (is (valid-eval-error? error) "(in-ns \"string\")  should result in an js/Error")
    (is (= "Argument to in-ns must be a symbol" (extract-message error)) "(in-ns \"string\") should have correct error")))

(h/read-eval-call-test e/*target-opts*
  ["(in-ns first.namespace)"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(in-ns symbol) should NOT succeed")
    (is (valid-eval-error? error) "(in-ns symbol)  should result in an js/Error")
    ;; Weird behavior of various runtime, each generating a different message
    (is (or (re-find #"is not defined" (extract-message error))
            (re-find #"Can't find variable" (extract-message error))
            (re-find #"Argument to in-ns must be a symbol" (extract-message error))) "(in-ns symbol) should have correct error")))

(h/read-eval-call-test e/*target-opts*
  ["(in-ns 'first.namespace)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(in-ns 'symbol) should succeed")
    (is (valid-eval-result? out) "(in-ns 'symbol) should be a valid result")
    (is (= "nil" out) "(in-ns 'symbol) should return nil")))

;; Note that (do (in-ns 'my.namespace) (def a 3) (in-ns 'cljs) my.namespace/a)
;; Does not work in ClojureScript!
(h/read-eval-call-test e/*target-opts*
  ["(in-ns 'first.namespace)"
   "(def a 3)"
   "(in-ns 'second.namespace)"
   "(require 'first.namespace)"
   "first.namespace/a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Deffing a var in an in-ns namespace and querying it should succeed")
    (is (valid-eval-result? out) "Deffing a var in an in-ns namespace and querying it should be a valid result")
    (is (= "3" out) "Deffing a var in an in-ns namespace and querying it should retrieve the interned var value")))

(h/read-eval-call-test e/*target-opts*
  ["(ns 'something.ns)"
   :after (repl/purge-cljs-user! '[something.ns])]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) (str _msg_ "should NOT succeed"))
    (is (valid-eval-error? error) (str _msg_ "should result in an js/Error"))
    (is (re-find #"Namespaces must be named by a symbol" (extract-message error)) (str _msg_ "should have correct error"))))

(h/read-eval-call-test e/*target-opts*
  ["(ns my.namespace)"
   :after (repl/purge-cljs-user! '[my.namespace])]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "nil" out) (str _msg_ "should return \"nil\""))))

;; AR - with fake load, we want to test functionality that don't depend on
;; source reading. This will stay until we will provide a mechanism to inject
;; *load-fn* that actuall read files.
(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/fake-load-fn!)
  ["(require something)"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(require something) should NOT succeed")
    (is (valid-eval-error? error) "(require something) should result in an js/Error")
    (is (re-find #"is not ISeqable" (extract-message error)) "(require something) should have correct error")))

(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/fake-load-fn!)
  ["(require \"something\")"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "(require \"something\") should NOT succeed")
    (is (valid-eval-error? error) "(require \"something\") should result in an js/Error")
    (is (re-find #"Argument to require must be a symbol" (extract-message error)) "(require \"something\") should have correct error")))

(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/fake-load-fn!)
  ["(require 'something.ns)"
   :after (repl/purge-cljs-user! '[something.ns])]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'something.ns) should succeed")
    (is (valid-eval-result? out) "(require 'something.ns) should be a valid result")
    (is (= "nil" out) "(require 'something.ns) should return nil")))

;; AR - this is failing with :optimizations :simple.
;; There is some shadowing going on when using single letter
;; namespaces because the google closure compiler happens to a,b,c,d when
;; renaming symbols
(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/fake-load-fn!)
  ["(ns a.ns)"
   "(def a 3)"
   "(ns b.ns)"
   "(require 'a.ns)"
   "(require 'a.ns)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'a.ns) from b.ns should succeed")
    (is (valid-eval-result? out) "(require 'a.ns) from b.ns should be a valid result")
    (is (= 'b.ns (repl/current-ns)) "(require 'a.ns) from b.ns should not change namespace")))

(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/fake-load-fn!)
  ["(ns foo.bar.baz)"
   "(def a 3)"
   "(ns foo.bar)"
   "(require 'foo.bar.baz)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require 'foo.bar.baz) from foo.bar should succeed")
    (is (valid-eval-result? out) "(require 'foo.bar.baz) from foo.bar should be a valid result")
    (is (= 'foo.bar (repl/current-ns)) "(require 'foo.bar.baz) from foo.bar should not change namespace")))

(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/fake-load-fn!)
  ["(ns foo.bar.baz)"
   "(def referred-a 3)"
   "(ns foo.bar)"
   "(require '[foo.bar.baz :refer [referred-a]])"
   "referred-a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(require '[foo.bar.baz :refer [referred-a]]) from foo.bar should succeed")
    (is (valid-eval-result? out) "(require '[foo.bar.baz :refer [referred-a]]) from foo.bar should be a valid result")
    (is (= 'foo.bar (repl/current-ns)) "(require '[foo.bar.baz :refer [referred-a]]) from foo.bar should not change namespace")
    (is (= "3" out) "(require '[foo.bar.baz :refer [referred-a]]) from foo.bar should retrieve the interned var value")))

;; https://github.com/Lambda-X/replumb/issues/117
(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/fake-load-fn!)
  ["(require '[cljs.tools.reader :as r])"
   "(binding [r/resolve-symbol (constantly 'cljs.user/x)] (r/read-string \"`x\"))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Test for issues #117 should succeed")
    (is (valid-eval-result? out) "Test for issues #117 should be a valid result")
    (is (= "(quote cljs.user/x)" out) "Test for issues #117 should return (quote cljs.user/x)")))

;; was require-node-test/require-when-read-file-return-nil
(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/no-resource-load-fn!)
  ["(require 'clojure.string)"
   "(source clojure.string/trim)"
   :after (repl/purge-cljs-user! '[clojure.string])]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "(source clojure.string/trim) should succeed.")
    (is (valid-eval-result? out) "(source clojure.string/trim) should be a valid result")
    (is (= "nil" out) "(source clojure.string/trim) should return nil")))

;; was require-node-test/load-file-when-read-file-retuns-nil
;; AR - This test loads the file anyways but it shouldn't !
;; (h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/no-resource-load-fn!)
;;   ["(load-file \"foo/load.clj\")"]
;;   (let [result (unwrap-result @_res_)]
;;     (is (success? @_res_) "(load-file \"foo/load.clj\") should succeed")
;;     (is (valid-eval-result? result) "(load-file \"foo/load.clj\") be a valid result")
;;     (is (= "nil" result) "(load-file \"foo/load.clj\") should return nil")
;;     (is (= (repl/current-ns) 'cljs.user) "(load-file \"foo/load.clj\") should not change namespace"))
;;   (_reset!_ '[foo.load]))

;; was source-node-test/source-corner-cases
(h/read-eval-call-test (assoc e/*target-opts* :load-fn! load/no-resource-load-fn!)
  ["(require 'clojure.string)"
   "(source clojure.string/trim)"
   :after (repl/purge-cljs-user! '[clojure.string])]
  (let [source-string (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "when *load-fn* returns nil should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "when *load-fn* returns nil should be a valid result"))
    (is (= "nil" source-string) (str _msg_ "when *load-fn* returns nil should return nil"))))

(h/read-eval-call-test (-> e/*target-opts*
                           (dissoc :load-fn!)
                           (assoc :read-file-fn! nil))
  ["(require 'clojure.string)"
   "(source clojure.string/trim)"
   :after (repl/purge-cljs-user! '[clojure.string])]
  (let [source-string (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "when :read-file-fn! is nil should succeed."))
    (is (valid-eval-result? source-string) (str _msg_ "when :read-file-fn! is nil should be a valid result"))
    (is (= "nil" source-string) (str _msg_ "when :read-file-fn! is nil should return nil"))))

;;;;;;;;;;;;;;;;;;;;;
;;  Warning tests  ;;
;;;;;;;;;;;;;;;;;;;;;
;; AR - There is only on case missing because you can't have an error and a
;; warning at the same time.

;; Response is :error and warning-as-error is true
(h/read-eval-call-test (assoc e/*target-opts* :warning-as-error true)
  ["(def a \"6\""]
  (let [out (unwrap-result @_res_)]
    (is (not (success? @_res_)) "Response is :error and warning-as-error is true should not succeed")
    (is (not (has-valid-warning? @_res_)) "Response is :error and warning-as-error is true should not contain :warning")
    (is (valid-eval-error? out) "Response is :error and warning-as-error is true should result in an js/Error")
    (is (re-find #"EOF" (extract-message out)) "Response is :error and warning-as-error is true should return EOF, the original error")))

;; Response is :error and warning-as-error is false
(h/read-eval-call-test e/*target-opts*
  ["(def a \"6\""]
  (let [out (unwrap-result @_res_)]
    (is (not (success? @_res_)) "Response is :error and warning-as-error is false should not succeed")
    (is (not (has-valid-warning? @_res_)) "Response is :error and warning-as-error is false should not contain :warning")
    (is (valid-eval-error? out) "Response is :error and warning-as-error is false should result in an js/Error")
    (is (re-find #"EOF" (extract-message out)) "Response is :error and warning-as-error is false should return EOF")))

;; Response is :value but warning was raised and warning-as-error is true
(h/read-eval-call-test (assoc e/*target-opts* :warning-as-error true)
  ["_arsenununpa42"]
  (let [out (unwrap-result @_res_)]
    (is (not (success? @_res_)) "Response is :value but warning was raised and warning-as-error is true should not succeed")
    (is (not (has-valid-warning? @_res_)) "Response is :value but warning was raised and warning-as-error is true should not contain warning")
    (is (valid-eval-error? out) "Response is :value but warning was raised and warning-as-error is true should result in an js/Error")
    (is (re-find #"undeclared.*_arsenununpa42" (extract-message out)) "Response is :value but warning was raised and warning-as-error is true should have the right error msg")))

;; Response is :value but warning was raised and warning-as-error is false
(h/read-eval-call-test e/*target-opts*
  ["_arsenununpa42"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Response is :value but warning was raised and warning-as-error is false should succeed")
    (is (has-valid-warning? @_res_) "Response is :value but warning was raised and warning-as-error is false should contain warning")
    (is (valid-eval-result? out) "Response is :value but warning was raised and warning-as-error is false should be a valid result")
    (is (= "nil" out) "Response is :value but warning was raised and warning-as-error is false should return nil")))

;; Response is :value, no warning and warning-as-error is false
(h/read-eval-call-test e/*target-opts*
  ["(def a 2)"
   "a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Response is :value, no warning and warning-as-error is false should succeed")
    (is (not (has-valid-warning? @_res_)) "Response is :value, no warning was raised and warning-as-error is false should not contain warning")
    (is (valid-eval-result? out) "Response is :value, no warning and warning-as-error is false should be a valid result")
    (is (= "2" out) "Response is :value, no warning and warning-as-error is false symbol should return 2")))

;; Response is :value, no warning and warning-as-error is true
(h/read-eval-call-test (assoc e/*target-opts* :warning-as-error true)
  ["(def a 2)"
   "a"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Response is :value, no warning and warning-as-error is true should succeed")
    (is (not (has-valid-warning? @_res_)) "Response is :value, no warning was raised and warning-as-error is true should not contain warning")
    (is (valid-eval-result? out) "Response is :value, no warning and warning-as-error is true should be a valid result")
    (is (= "2" out) "Response is :value, no warning and warning-as-error is true symbol should return 2")))

;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Warning tests end  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

;; AR - Don't need to test more as ClojureScript already has extensive tests on this
(h/read-eval-call-test e/*target-opts*
  ["#js [1 2]"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Reading #js literal should succeed")
    (is (valid-eval-result? out) "Reading #js literal should have a valid result")
    (is (= "#js [1 2]" out) "Reading #js literal should return the object")))

(h/read-eval-call-test e/*target-opts*
  ["#queue [1 2]"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Reading #queue should succeed")
    (is (valid-eval-result? out) "Reading #queue should have a valid result")
    (is (= "#queue [1 2]" out) "Reading #queue should return the object")))

(h/read-eval-call-test e/*target-opts*
  ["#inst \"2010-11-12T13:14:15.666-05:00\""]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Reading #inst should succeed")
    (is (valid-eval-result? out) "Reading #inst should have a valid result")
    (is (= "#inst \"2010-11-12T18:14:15.666-00:00\"" out) "Reading #inst should return the object")))

(h/read-eval-call-test e/*target-opts*
  ["#uuid \"550e8400-e29b-41d4-a716-446655440000\""]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) "Reading #uuid should succeed")
    (is (valid-eval-result? out) "Reading #uuid should have a valid result")
    (is (= "#uuid \"550e8400-e29b-41d4-a716-446655440000\"" out) "Reading #uuid should return the object")))

(let [custom-opts (assoc e/*target-opts* :no-pr-str-on-value true)]
  (h/read-eval-call-test custom-opts
    ["(js-obj :foo :bar)"]
    (let [out (unwrap-result @_res_)]
      (is (success? @_res_) "Executing (js-obj :foo :bar) and :no-pr-str-on-value true should succeed")
      (is (valid-eval-result? custom-opts out) "Executing (js-obj :foo :bar) and :no-pr-str-on-value true should have a valid result")
      (is (object? out) "Executing (js-obj :foo :bar) and :no-pr-str-on-value true should return a JS object")))

  (h/read-eval-call-test custom-opts
    ["#js [:foo :bar]"]
    (let [out (unwrap-result @_res_)]
      (is (success? @_res_) "Executing #js [:foo :bar] and :no-pr-str-on-value true should succeed")
      (is (valid-eval-result? custom-opts out) "Executing #js [:foo :bar]) and :no-pr-str-on-value true should have a valid result")
      (is (array? out) "Executing #js [:foo :bar] and :no-pr-str-on-value true should return a JS object"))))

(h/read-eval-call-test e/*target-opts*
  ["2 3"]
  (let [error (unwrap-result @_res_)]
    (is (not (success? @_res_)) "\"2 3\" with :context set to :expr should not succeed")
    (is (valid-eval-error? error) "\"2 3\" with :context set to :expr should be an instance of jsError")
    (is (re-find #"ERROR - .* is not a function" (extract-message error)) "\"2 3\" with :context set to :expr should have a valid error message.")))

(let [custom-opts (assoc e/*target-opts* :context :statement)]
  (h/read-eval-call-test custom-opts
    ["2 3"]
    (let [out (unwrap-result @_res_)]
      (is (success? @_res_) "\"2 3\" with :context set to :statement should succeed")
      (is (valid-eval-result? custom-opts out) "\"2 3\" with :context set to :statement should have a valid result")
      (is (= "nil" out) "\"2 3\" with :context set to :statement should yield nil."))))
