(ns ^:figwheel-load replumb.repl-test
  (:require [cljs.test :refer-macros [deftest is]]
            [doo.runner :as doo]
            [replumb.repl :as repl]
            [replumb.load :as load]
            [replumb.core :as core :refer [success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?
                                               has-valid-warning?]]))

(deftest current-ns
  (is (symbol? (repl/current-ns)) "The current ns should be a symbol"))

(let [src-paths ["dev-resources/private/test/node/compiled/out"
                 "dev-resources/private/test/src/cljs"
                 "dev-resources/private/test/src/clj"]
      target-opts (if (doo/node?)
                    (core/nodejs-options load/fake-load-fn!)
                    (core/browser-options load/fake-load-fn!))
      validated-echo-cb (partial repl/validated-call-back! target-opts echo-callback)
      reset-env! (partial repl/reset-env! target-opts)
      read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)]

  (deftest init
    ;; The init test heavily relies on repl execution order. If the repl is already
    ;; initialized before this point this test will fail. It is a good idea not
    ;; to put repl tests in other places other then this file or force test execution
    ;; order if this happens. For some we use force-init! in order to reset the
    ;; state.
    (let [_ (repl/force-init!)]
      (is (not (:initializing? @repl/app-env)) "After force-init! :initializing? should be false before init")
      (is (:needs-init? @repl/app-env) "After force-init!, :needs-init? should be true before init")
      (reset-env!))

    (let [_ (repl/persist-init-opts! {:verbose true :src-paths ["src/a" "src/b"] :init-fn! #()})]
      (is (every? repl/init-option-set (keys (:previous-init-opts @repl/app-env))) "After persist-init-opts!, the app-env should contain the right initoptions")
      (reset-env!))

    (let [_ (repl/persist-init-opts! {:verbose true :load-fn! #() :custom :opts})]
      (is (not-any? repl/init-option-set (:previous-init-opts @repl/app-env)) "After persist-init-opts! but no option to persist, the app-env should not contain init options")
      (reset-env!))

    (let [init-map-atom (atom {})
          custom-init-fn (fn [init-map] (reset! init-map-atom init-map))
          _ (swap! repl/app-env merge {:initializing? false
                                       :needs-init? true})
          res (repl/read-eval-call (merge target-opts {:init-fn! custom-init-fn}) validated-echo-cb "(def c 4)")]
      (is (success? res) "Init should return successfully")
      (is (not (:initializing? @repl/app-env)) "Flag :initializing? should be false when the init exits")
      (is (not (:needs-init? @repl/app-env)) "Flag :needs-init? should be false when the init exits")
      (is (= '(def c 4) (:form @init-map-atom)) "Init map should have correct :form")
      (is (not (string? (:form @init-map-atom))) "Init map :form should not be a string")
      (is (= (repl/current-ns) (:ns @init-map-atom)) "Init map should have correct :ns")
      (is (symbol? (:ns @init-map-atom)) "Init map :ns should be a symbol")
      (is (= (:target target-opts) (:target @init-map-atom)) "Init map with custom init-fn! should have correct :target")
      (reset-env!))

    (let [res (repl/read-eval-call (merge target-opts {:src-paths ["my/custom/path"]}) validated-echo-cb "(def c 4)")
          previous-init-opts (:previous-init-opts @repl/app-env)]
      (is (some #{"my/custom/path"} (:src-paths previous-init-opts)) "After changing :src-paths the new app-env should contain the new path")
      (reset-env!)))

  (deftest process-pst
    (let [res (do (read-eval-call "(throw (ex-info \"This is my custom error message %#FT%\" {:tag :exception}))")
                  (read-eval-call "*e"))
          error (unwrap-result res)]
      (is (success? res) "Eval of *e with error should return successfully")
      (is (valid-eval-result? error) "Eval of *e with error should be a valid error")
      (is (re-find #"This is my custom error message %#FT%" error) "Eval of *e with error should return the correct message")
      (reset-env!))

    (let [res (do (read-eval-call "(throw (ex-info \"This is my custom error message %#FT%\" {:tag :exception}))")
                  (read-eval-call "(pst)"))
          trace (unwrap-result res)]
      (is (success? res) "(pst) with previous error should return successfully")
      (is (valid-eval-result? trace) "(pst) with previous error should be a valid result")
      (is (re-find #"This is my custom error message %#FT%" trace) "(pst) with previous error should return the correct message")
      (reset-env!))

    (let [res (read-eval-call "(pst)")
          trace (unwrap-result res)]
      (is (success? res) "(pst) with no error should return successfully")
      (is (valid-eval-result? trace) "(pst) with no error should be a valid result")
      (is (= "nil" trace) "(pst) with no error should return nil")
      (reset-env!)))

  (deftest process-doc
    (let [res (read-eval-call "(doc 'println)")
          error (unwrap-result res)]
      (is (not (success? res)) "(doc 'symbol) should have correct error")
      (is (valid-eval-error? error) "(doc 'symbol) should result in an js/Error")
      (reset-env!))

    (let [res (read-eval-call "(doc println)")
          docstring (unwrap-result res)]
      (is (success? res) "(doc symbol) should succeed")
      (is (valid-eval-result? docstring) "(doc symbol) should be a valid result")
      ;; Cannot test #"cljs.core\/println" because of a compilation bug?
      (is (re-find #"cljs\.core.{1}println" docstring) "(doc symbol) should return valid docstring")
      (reset-env!))

    (let [res (do (read-eval-call "(defn my-function \"This is my documentation\" [param] (param))")
                  (read-eval-call "(doc my-function)"))
          docstring (unwrap-result res)]
      (is (success? res) "(doc my-function) should succeed")
      (is (valid-eval-result? docstring) "(doc my-function) should be a valid result")
      (is (re-find #"This is my documentation" docstring) "(doc my-function) should return valid docstring")
      (reset-env!))

    (let [res (do (read-eval-call "(ns myns.testns \"Docstring for namespace\")")
                  (read-eval-call "(doc myns.testns)"))
          docstring (unwrap-result res)]
      (is (success? res) "(doc myns.testns) should succeed.")
      (is (valid-eval-result? docstring) "(doc myns.testns) should be a valid result")
      (is (re-find #"Docstring for namespace" docstring) "(doc myns.testns) should return valid docstring")
      (reset-env! '[myns.testns]))

    (let [res (read-eval-call "(doc ns-interns)")
          docstring (unwrap-result res)]
      (is (success? res) "(doc ns-interns), for issue #81, should succeed")
      (is (valid-eval-result? docstring) "(doc ns-interns), for issue #81, should be a valid result")
      (is (re-find #"Returns a map of the intern mappings for the namespace" docstring) "(doc ns-interns), for issue #81, should return the correct docstring")
      (reset-env!)))

  (deftest process-dir
    ;; note that we don't require first
    (let [res (read-eval-call "(dir clojure.string)")
          dirstring (unwrap-result res)]
      (is (success? res) "(dir clojure.string) should succeed")
      (is (valid-eval-result? dirstring) "(dir clojure.string) should be a valid result")
      (is (= "nil" dirstring) "(dir clojure.string) should be \"nil\" because clojure.string has not been required first")
      (reset-env!)))

  (deftest process-apropos
    ;; test with string "tim"
    (let [res (read-eval-call "(apropos \"tim\")")
          result (unwrap-result res)
          expected "(cljs.core/-deref-with-timeout cljs.core/dotimes cljs.core/system-time cljs.core/time)"]
      (is (success? res) "(apropos \"tim\") should succeed")
      (is (valid-eval-result? result) "(apropos \"tim\") should be a valid result")
      (is (= expected result) "(apropos \"tim\") should return valid docstring")
      (reset-env!))

    ;; test with regular expression #"tim"
    (let [res (read-eval-call "(apropos #\"tim\")")
          result (unwrap-result res)
          expected "(cljs.core/-deref-with-timeout cljs.core/dotimes cljs.core/system-time cljs.core/time)"]
      (is (success? res) "(apropos #\"tim\") should succeed")
      (is (valid-eval-result? result) "(apropos #\"tim\") should be a valid result")
      (is (= expected result) "(apropos #\"tim\") should return valid docstring")
      (reset-env!))

    ;; test with regular expression #"t[i]me, containing metacharacters
    (let [res (read-eval-call "(apropos #\"t[i]me\")")
          result (unwrap-result res)
          expected "(cljs.core/-deref-with-timeout cljs.core/dotimes cljs.core/system-time cljs.core/time)"]
      (is (success? res) "(apropos  #\"t[i]me\") should succeed")
      (is (valid-eval-result? result) "(apropos  #\"t[i]me\") should be a valid result")
      (is (= expected result) "(apropos #\"t[i]me\") should return valid docstring")
      (reset-env!))

    ;; test with string "t[i]me"
    ;; the metacharacters in this case will be interpreted just as characteres
    (let [res (read-eval-call "(apropos \"t[i]me\")")
          result (unwrap-result res)]
      (is (success? res) "(apropos \"t[i]me\") should succeed")
      (is (valid-eval-result? result) "(apropos  #\"t[i]me\") should be a valid result")
      (is (= "nil" result) "(apropos \"t[i]me\") should return nil.")
      (repl/reset-env!)))

  (deftest process-find-doc
    (let [res (read-eval-call "(find-doc \"unguessable-string\")")
          result (unwrap-result res)]
      (is (success? res) "(find-doc \"unguessable-string\") should succeed")
      (is (valid-eval-result? result) "(find-doc \"unguessable-string\") should be a valid result")
      (is (= "nil" result) "(find-doc \"unguessable-string\") should return nil")
      (reset-env!))

    (let [res (read-eval-call "(find-doc \"^(di|a)ssoc.*!\")")
          result (unwrap-result res)
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
      (is (success? res) "(find-doc \"^(di|a)ssoc.*!\") should succeed")
      (is (valid-eval-result? result) "(find-doc \"^(di|a)ssoc.*!\") should be a valid result")
      (is (= expected result) "(find-doc \"^(di|a)ssoc.*!\") should return valid docstrings")
      (reset-env!))

    (let [res (read-eval-call "(find-doc \"select-keys\")")
          result (unwrap-result res)
          expected "-------------------------
select-keys
([map keyseq])
  Returns a map containing only those entries in map whose key is in keys
"]
      (is (success? res) "(find-doc \"select-keys\") should succeed")
      (is (valid-eval-result? result) "(find-doc \"select-keys\") should be a valid result")
      (is (= expected result) "(find-doc \"select-keys\") should return valid docstring")
      (reset-env!)))

  (deftest process-in-ns
    ;; Damian - Add COMPILED flag to cljs eval to turn off namespace already declared errors
    ;; AR - COMPILED goes here not in the runner otherwise node does not execute doo tests
    ;; AR - js/COMPILED is not needed after having correctly bootstrapped the
    ;; browser environment, see PR #57
    (let [res (read-eval-call "(in-ns \"first.namespace\")")
          error (unwrap-result res)]
      (is (not (success? res)) "(in-ns \"string\") should NOT succeed")
      (is (valid-eval-error? error) "(in-ns \"string\")  should result in an js/Error")
      (is (= "Argument to in-ns must be a symbol" (extract-message error)) "(in-ns \"string\") should have correct error")
      (reset-env!))

    (let [res (read-eval-call "(in-ns first.namespace)")
          error (unwrap-result res)]
      (is (not (success? res)) "(in-ns symbol) should NOT succeed")
      (is (valid-eval-error? error) "(in-ns symbol)  should result in an js/Error")
      ;; Weird behavior of various runtime, each generating a different message
      (is (or (re-find #"is not defined" (extract-message error))
              (re-find #"Can't find variable" (extract-message error))
              (re-find #"Argument to in-ns must be a symbol" (extract-message error))) "(in-ns symbol) should have correct error")
      (reset-env!))

    (let [res (read-eval-call "(in-ns 'first.namespace)")
          out (unwrap-result res)]
      (is (success? res) "(in-ns 'symbol) should succeed")
      (is (valid-eval-result? out) "(in-ns 'symbol) should be a valid result")
      (is (= "nil" out) "(in-ns 'symbol) should return nil")
      (reset-env! '[first.namespace]))

    ;; Note that (do (in-ns 'my.namespace) (def a 3) (in-ns 'cljs) my.namespace/a)
    ;; Does not work in ClojureScript!
    (let [res (do (read-eval-call "(in-ns 'first.namespace)")
                  (read-eval-call "(def a 3)")
                  (read-eval-call "(in-ns 'second.namespace)")
                  (read-eval-call "(require 'first.namespace)")
                  (read-eval-call "first.namespace/a"))
          out (unwrap-result res)]
      (is (success? res) "Deffing a var in an in-ns namespace and querying it should succeed")
      (is (valid-eval-result? out) "Deffing a var in an in-ns namespace and querying it should be a valid result")
      (is (= "3" out) "Deffing a var in an in-ns namespace and querying it should retrieve the interned var value")
      (reset-env! '[first.namespace second.namespace])))

  (deftest process-ns
    (let [res (read-eval-call "(ns 'something.ns)")
          error (unwrap-result res)]
      (is (not (success? res)) "(ns 'something.ns) should NOT succeed")
      (is (valid-eval-error? error) "(ns 'something.ns) should result in an js/Error")
      (is (re-find #"Namespaces must be named by a symbol" (extract-message error)) "(ns 'something.ns) should have correct error")
      (reset-env!))

    (let [res (read-eval-call "(ns my.namespace)")
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace) should be a valid result")
      (is (= "nil" out) "(ns my.namespace) should return \"nil\"")
      (reset-env! '[my.namespace])))

  ;; AR - with fake load, we want to test functionality that don't depend on
  ;; source reading. This will stay until we will provide a mechanism to inject
  ;; *load-fn* that actuall read files.
  (deftest process-require-fake-load
    ;; Damian - Add js/COMPILED flag to cljs eval to turn off namespace already declared errors
    ;; AR - js/COMPILED goes here not in the runner otherwise node does not execute doo tests
    ;; AR - js/COMPILED is not needed after having correctly bootstrapped the
    ;; browser environment, see PR #57
    (let [res (read-eval-call "(require something)")
          error (unwrap-result res)]
      (is (not (success? res)) "(require something) should NOT succeed")
      (is (valid-eval-error? error) "(require something) should result in an js/Error")
      (is (re-find #"is not ISeqable" (extract-message error)) "(require something) should have correct error")
      (reset-env!))

    (let [res (read-eval-call "(require \"something\")")
          error (unwrap-result res)]
      (is (not (success? res)) "(require \"something\") should NOT succeed")
      (is (valid-eval-error? error) "(require \"something\") should result in an js/Error")
      (is (re-find #"Argument to require must be a symbol" (extract-message error)) "(require \"something\") should have correct error")
      (reset-env!))

    (let [res (read-eval-call "(require 'something.ns)")
          out (unwrap-result res)]
      (is (success? res) "(require 'something.ns) should succeed")
      (is (valid-eval-result? out) "(require 'something.ns) should be a valid result")
      (is (= "nil" out) "(require 'something.ns) should return nil")
      (reset-env! '[something.ns]))

    (let [res (do (read-eval-call "(ns a.ns)")
                  (read-eval-call "(def a 3)")
                  (read-eval-call "(ns b.ns)")
                  (read-eval-call "(require 'a.ns)"))
          out (unwrap-result res)]
      (is (success? res) "(require 'a.ns) from b.ns should succeed")
      (is (valid-eval-result? out) "(require 'a.ns) from b.ns should be a valid result")
      (is (= 'b.ns (repl/current-ns)) "(require 'a.ns) from b.ns should not change namespace")
      (reset-env! '[a.ns b.ns]))

    (let [res (do (read-eval-call "(ns c.ns)")
                  (read-eval-call "(def referred-a 3)")
                  (read-eval-call "(ns d.ns)")
                  (read-eval-call "(require '[c.ns :refer [referred-a]])")
                  (read-eval-call "referred-a"))
          out (unwrap-result res)]
      (is (success? res) )
      (is (valid-eval-result? out) )
      (is (= 'd.ns (repl/current-ns)) "(require '[c.ns :refer [referred-a]]) should not change namespace")
      (is (= "3" out) "(require '[c.ns :refer [referred-a]]) should retrieve the interned var value")
      (reset-env! '[c.ns d.ns])))

  (deftest warnings
    ;; AR - The only missing is because you can't have an error and a warning at the same time.
    ;; Response is :error and warning-as-error is true
    (let [res (repl/read-eval-call (merge target-opts {:warning-as-error true}) validated-echo-cb
                                   "(def a \"6\"")
          out (unwrap-result res)]
      (is (not (success? res)) "Response is :error and warning-as-error is true should not succeed")
      (is (not (has-valid-warning? res)) "Response is :error and warning-as-error is true should not contain :warning")
      (is (valid-eval-error? out) "Response is :error and warning-as-error is true should result in an js/Error")
      (is (re-find #"EOF" (extract-message out)) "Response is :error and warning-as-error is true should return EOF, the original error")
      (reset-env!))

    ;; Response is :error and warning-as-error is false
    (let [res (repl/read-eval-call target-opts validated-echo-cb
                                   "(def a \"6\"")
          out (unwrap-result res)]
      (is (not (success? res)) "Response is :error and warning-as-error is false should not succeed")
      (is (not (has-valid-warning? res)) "Response is :error and warning-as-error is false should not contain :warning")
      (is (valid-eval-error? out) "Response is :error and warning-as-error is false should result in an js/Error")
      (is (re-find #"EOF" (extract-message out)) "Response is :error and warning-as-error is false should return EOF")
      (reset-env!))

    ;; Response is :value but warning was raised and warning-as-error is true
    (let [res (repl/read-eval-call (merge target-opts {:warning-as-error true}) validated-echo-cb
                                   "_arsenununpa42")
          out (unwrap-result res)]
      (is (not (success? res)) "Response is :value but warning was raised and warning-as-error is true should not succeed")
      (is (not (has-valid-warning? res)) "Response is :value but warning was raised and warning-as-error is true should not contain warning")
      (is (valid-eval-error? out) "Response is :value but warning was raised and warning-as-error is true should result in an js/Error")
      (is (re-find #"undeclared.*_arsenununpa42" (extract-message out)) "Response is :value but warning was raised and warning-as-error is true should have the right error msg")
      (reset-env!))

    ;; Response is :value but warning was raised and warning-as-error is false
    (let [res (repl/read-eval-call target-opts validated-echo-cb
                                   "_arsenununpa42")
          out (unwrap-result res)]
      (is (success? res) "Response is :value but warning was raised and warning-as-error is false should succeed")
      (is (has-valid-warning? res) "Response is :value but warning was raised and warning-as-error is false should contain warning")
      (is (valid-eval-result? out) "Response is :value but warning was raised and warning-as-error is false should be a valid result")
      (is (= "nil" out) "Response is :value but warning was raised and warning-as-error is false should return nil")
      (reset-env!))

    ;; Response is :value, no warning and warning-as-error is false
    (let [res (do (repl/read-eval-call target-opts validated-echo-cb "(def a 2)")
                  (repl/read-eval-call target-opts validated-echo-cb "a"))
          out (unwrap-result res)]
      (is (success? res) "Response is :value, no warning and warning-as-error is false should succeed")
      (is (not (has-valid-warning? res)) "Response is :value, no warning was raised and warning-as-error is false should not contain warning")
      (is (valid-eval-result? out) "Response is :value, no warning and warning-as-error is false should be a valid result")
      (is (= "2" out) "Response is :value, no warning and warning-as-error is false symbol should return 2")
      (reset-env!))

    ;; Response is :value, no warning and warning-as-error is true
    (let [opts (merge target-opts {:warning-as-error true})
          res (do (repl/read-eval-call opts validated-echo-cb "(def a 2)")
                  (repl/read-eval-call opts validated-echo-cb "a"))
          out (unwrap-result res)]
      (is (success? res) "Response is :value, no warning and warning-as-error is true should succeed")
      (is (not (has-valid-warning? res)) "Response is :value, no warning was raised and warning-as-error is true should not contain warning")
      (is (valid-eval-result? out) "Response is :value, no warning and warning-as-error is true should be a valid result")
      (is (= "2" out) "Response is :value, no warning and warning-as-error is true symbol should return 2")
      (reset-env!)))

  (deftest macros
    ;;;;;;;;;;;;;;;;
    ;; Implementing examples from Mike Fikes work at:
    ;; http://blog.fikesfarm.com/posts/2015-09-07-messing-with-macros-at-the-repl.html
    ;; (it's not that I don't trust Mike, you know)
    ;;;;;;;;;;;;;;;;
    (let [res (read-eval-call "(defmacro hello [x] `(inc ~x))")
          out (unwrap-result res)]
      (is (success? res) "(defmacro hello ..) should succeed")
      (is (valid-eval-result? out) "(defmacro hello ..) should have a valid result")
      (is (= "true" out) "(defmacro hello ..) should return true")
      (reset-env!))

    (let [res (do (read-eval-call "(defmacro hello [x] `(inc ~x))")
                  (read-eval-call "(hello nil nil 13)"))
          out (unwrap-result res)]
      (is (success? res) "Executing (defmacro hello ..) as function should succeed")
      (is (valid-eval-result? out) "Executing (defmacro hello ..) as function should have a valid result")
      (is (= "(inc 13)" out) "Executing (defmacro hello ..) as function should return (inc 13)")
      (reset-env!))

    (let [res (do (read-eval-call "(ns foo.core$macros)")
                  (read-eval-call "(defmacro hello [x] (prn &form) `(inc ~x))")
                  (read-eval-call "(foo.core/hello (+ 2 3))"))
          out (unwrap-result res)]
      (is (success? res) "Executing (foo.core/hello ..) as function should succeed")
      (is (valid-eval-result? out) "Executing (foo.core/hello ..) hello ..) as function should have a valid result")
      (is (= "6" out) "Executing (foo.core/hello ..) hello ..) as function should return 6")
      (reset-env! '[foo.core]))

    (let [res (do (read-eval-call "(ns foo.core$macros)")
                  (read-eval-call "(defmacro hello [x] (prn &form) `(inc ~x))")
                  (read-eval-call "(ns another.ns)")
                  (read-eval-call "(require-macros '[foo.core :refer [hello]])")
                  (read-eval-call "(hello (+ 2 3))"))
          out (unwrap-result res)]
      (is (success? res) "Executing (foo.core/hello ..) as function should succeed")
      (is (valid-eval-result? out) "Executing (foo.core/hello ..) hello ..) as function should have a valid result")
      (is (= "6" out) "Executing (foo.core/hello ..) hello ..) as function should return 6")
      (reset-env! '[foo.core another.ns])))

  (deftest tagged-literals
    ;; AR - Don't need to test more as ClojureScript already has extensive tests on this
    (let [res (read-eval-call "#js [1 2]")
          out (unwrap-result res)]
      (is (success? res) "Reading #js literal should succeed")
      (is (valid-eval-result? out) "Reading #js literal should have a valid result")
      (is (= "#js [1 2]" out) "Reading #js literal should return the object"))

    (let [res (read-eval-call "#queue [1 2]")
          out (unwrap-result res)]
      (is (success? res) "Reading #queue should succeed")
      (is (valid-eval-result? out) "Reading #queue should have a valid result")
      (is (= "#queue [1 2]" out) "Reading #queue should return the object"))

    (let [res (read-eval-call "#inst \"2010-11-12T13:14:15.666-05:00\"")
          out (unwrap-result res)]
      (is (success? res) "Reading #inst should succeed")
      (is (valid-eval-result? out) "Reading #inst should have a valid result")
      (is (= "#inst \"2010-11-12T18:14:15.666-00:00\"" out) "Reading #inst should return the object"))

    (let [res (read-eval-call "#uuid \"550e8400-e29b-41d4-a716-446655440000\"")
          out (unwrap-result res)]
      (is (success? res) "Reading #uuid should succeed")
      (is (valid-eval-result? out) "Reading #uuid should have a valid result")
      (is (= "#uuid \"550e8400-e29b-41d4-a716-446655440000\"" out) "Reading #uuid should return the object")))

  (deftest load-fn
    (let [load-map-atom (atom {})
          custom-load-fn (fn [load-map cb] (reset! load-map-atom load-map) (cb nil))
          custom-opts (assoc target-opts :load-fn! custom-load-fn)]
      (let [rs (repl/read-eval-call custom-opts validated-echo-cb "(require 'bar.core)")]
        (is (= 'bar.core (:name @load-map-atom)) "Loading map with custom load-fn should have correct :name")
        (is (not (:macros @load-map-atom)) "Loading map with custom load-fn should have correct :macros")
        (is (= "bar/core" (:path @load-map-atom)) "Loading map with custom load-fn should have correct :path")
        (reset! load-map-atom {})
        (reset-env! '[bar.core]))))

  (deftest no-pr-str-on-value
    (let [custom-opts (assoc target-opts :no-pr-str-on-value true)
          validated-echo-cb (partial repl/validated-call-back! custom-opts echo-callback)
          res (repl/read-eval-call custom-opts validated-echo-cb "(js-obj :foo :bar)")
          out (unwrap-result res)]
      (is (success? res) "Executing (js-obj :foo :bar) and :no-pr-str-on-value true should succeed")
      (is (valid-eval-result? custom-opts out) "Executing (js-obj :foo :bar) and :no-pr-str-on-value true should have a valid result")
      (is (object? out) "Executing (js-obj :foo :bar) and :no-pr-str-on-value true should return a JS object")
      (reset-env!))

    (let [custom-opts (assoc target-opts :no-pr-str-on-value true)
          validated-echo-cb (partial repl/validated-call-back! custom-opts echo-callback)
          res (repl/read-eval-call custom-opts validated-echo-cb "#js [:foo :bar]")
          out (unwrap-result res)]
      (is (success? res) "Executing #js [:foo :bar] and :no-pr-str-on-value true should succeed")
      (is (valid-eval-result? custom-opts out) "Executing #js [:foo :bar]) and :no-pr-str-on-value true should have a valid result")
      (is (array? out) "Executing #js [:foo :bar] and :no-pr-str-on-value true should return a JS object")
      (reset-env!))))
