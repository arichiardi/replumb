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
      validated-echo-cb (partial repl/validated-call-back! echo-callback)
      target-opts (if (doo/node?)
                    (core/nodejs-options load/fake-load-fn!)
                    (core/browser-options load/fake-load-fn!))
      read-eval-call (partial repl/read-eval-call target-opts validated-echo-cb)]

  (deftest init
    ;; This test heavily relies on repl execution order. If the repl is already
    ;; initialized before this point this test will fail. It is a good idea not
    ;; to put repl tests in other places other then this file or force test execution
    ;; order if this happens. At the moment it is disabled.
    ;; (is (not (:initializing? @repl/app-env)) "Flag :initializing? should be false before init")
    ;; (is (:needs-init? @repl/app-env) "Flag :needs-init? should be true before init")
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
      (repl/reset-env!)))

  (deftest process-pst
    (let [res (do (read-eval-call "(throw (ex-info \"This is my custom error message %#FT%\" {:tag :exception}))")
                  (read-eval-call "*e"))
          error (unwrap-result res)]
      (is (success? res) "Eval of *e with error should return successfully")
      (is (valid-eval-result? error) "Eval of *e with error should be a valid error")
      (is (re-find #"This is my custom error message %#FT%" error) "Eval of *e with error should return the correct message")
      (repl/reset-env!))

    (let [res (do (read-eval-call "(throw (ex-info \"This is my custom error message %#FT%\" {:tag :exception}))")
                  (read-eval-call "(pst)"))
          trace (unwrap-result res)]
      (is (success? res) "(pst) with previous error should return successfully")
      (is (valid-eval-result? trace) "(pst) with previous error should be a valid result")
      (is (re-find #"This is my custom error message %#FT%" trace) "(pst) with previous error should return the correct message")
      (repl/reset-env!))

    (let [res (read-eval-call "(pst)")
          trace (unwrap-result res)]
      (is (success? res) "(pst) with no error should return successfully")
      (is (valid-eval-result? trace) "(pst) with no error should be a valid result")
      (is (= "nil" trace) "(pst) with no error should return nil")
      (repl/reset-env!)))

  (deftest process-doc
    (let [res (read-eval-call "(doc 'println)")
          error (unwrap-result res)]
      (is (not (success? res)) "(doc 'symbol) should have correct error")
      (is (valid-eval-error? error) "(doc 'symbol) should result in an js/Error")
      (repl/reset-env!))

    (let [res (read-eval-call "(doc println)")
          docstring (unwrap-result res)]
      (is (success? res) "(doc symbol) should succeed")
      (is (valid-eval-result? docstring) "(doc symbol) should be a valid result")
      ;; Cannot test #"cljs.core\/println" because of a compilation bug?
      (is (re-find #"cljs\.core.{1}println" docstring) "(doc symbol) should return valid docstring")
      (repl/reset-env!))

    (let [res (do (read-eval-call "(defn my-function \"This is my documentation\" [param] (param))")
                  (read-eval-call "(doc my-function)"))
          docstring (unwrap-result res)]
      (is (success? res) "(doc my-function) should succeed")
      (is (valid-eval-result? docstring) "(doc my-function) should be a valid result")
      (is (re-find #"This is my documentation" docstring) "(doc my-function) should return valid docstring")
      (repl/reset-env!))

    (let [res (do (read-eval-call "(ns myns.testns \"Docstring for namespace\")")
                  (read-eval-call "(doc myns.testns)"))
          docstring (unwrap-result res)]
      (is (success? res) "(doc myns.testns) should succeed.")
      (is (valid-eval-result? docstring) "(doc myns.testns) should be a valid result")
      (is (re-find #"Docstring for namespace" docstring) "(doc myns.testns) should return valid docstring")
      (repl/reset-env! ["myns.testns"])))

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
      (repl/reset-env!))

    (let [res (read-eval-call "(in-ns first.namespace)")
          error (unwrap-result res)]
      (is (not (success? res)) "(in-ns symbol) should NOT succeed")
      (is (valid-eval-error? error) "(in-ns symbol)  should result in an js/Error")
      ;; Weird behavior of various runtime, each generating a different message
      (is (or (re-find #"is not defined" (extract-message error))
              (re-find #"Can't find variable" (extract-message error))
              (re-find #"Argument to in-ns must be a symbol" (extract-message error))) "(in-ns symbol) should have correct error")
      (repl/reset-env!))

    (let [res (read-eval-call "(in-ns 'first.namespace)")
          out (unwrap-result res)]
      (is (success? res) "(in-ns 'symbol) should succeed")
      (is (valid-eval-result? out) "(in-ns 'symbol) should be a valid result")
      (is (= "nil" out) "(in-ns 'symbol) should return nil")
      (repl/reset-env! ['first.namespace]))
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
      (repl/reset-env! ['first.namespace 'second.namespace])))

  (deftest process-ns
    (let [res (read-eval-call "(ns 'something.ns)")
          error (unwrap-result res)]
      (is (not (success? res)) "(ns 'something.ns) should NOT succeed")
      (is (valid-eval-error? error) "(ns 'something.ns) should result in an js/Error")
      (is (re-find #"Namespaces must be named by a symbol" (extract-message error)) "(ns 'something.ns) should have correct error")
      (repl/reset-env!))

    (let [res (read-eval-call "(ns my.namespace)")
          out (unwrap-result res)]
      (is (success? res) "(ns my.namespace) should succeed")
      (is (valid-eval-result? out) "(ns my.namespace) should be a valid result")
      (is (= "nil" out) "(ns my.namespace) should return \"nil\"")
      (repl/reset-env! ['my.namespace])))

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
      (repl/reset-env!))

    (let [res (read-eval-call "(require \"something\")")
          error (unwrap-result res)]
      (is (not (success? res)) "(require \"something\") should NOT succeed")
      (is (valid-eval-error? error) "(require \"something\") should result in an js/Error")
      (is (re-find #"Argument to require must be a symbol" (extract-message error)) "(require \"something\") should have correct error")
      (repl/reset-env!))

    (let [res (read-eval-call "(require 'something.ns)")
          out (unwrap-result res)]
      (is (success? res) "(require 'something.ns) should succeed")
      (is (valid-eval-result? out) "(require 'something.ns) should be a valid result")
      (is (= "nil" out) "(require 'something.ns) should return nil")
      (repl/reset-env! ['something.ns]))

    (let [res (do (read-eval-call "(ns a.ns)")
                  (read-eval-call "(def a 3)")
                  (read-eval-call "(ns b.ns)")
                  (read-eval-call "(require 'a.ns)"))
          out (unwrap-result res)]
      (is (success? res) "(require 'a.ns) from b.ns should succeed")
      (is (valid-eval-result? out) "(require 'a.ns) from b.ns should be a valid result")
      (is (= 'b.ns (repl/current-ns)) "(require 'a.ns) from b.ns should not change namespace")
      (repl/reset-env! ['a.ns 'b.ns]))

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
      (repl/reset-env! ['c.ns 'd.ns])))

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
      (repl/reset-env!))

    ;; Response is :error and warning-as-error is false
    (let [res (repl/read-eval-call target-opts validated-echo-cb
                                   "(def a \"6\"")
          out (unwrap-result res)]
      (is (not (success? res)) "Response is :error and warning-as-error is false should not succeed")
      (is (not (has-valid-warning? res)) "Response is :error and warning-as-error is false should not contain :warning")
      (is (valid-eval-error? out) "Response is :error and warning-as-error is false should result in an js/Error")
      (is (re-find #"EOF" (extract-message out)) "Response is :error and warning-as-error is false should return EOF")
      (repl/reset-env!))

    ;; Response is :value but warning was raised and warning-as-error is true
    (let [res (repl/read-eval-call (merge target-opts {:warning-as-error true}) validated-echo-cb
                                   "_arsenununpa42")
          out (unwrap-result res)]
      (is (not (success? res)) "Response is :value but warning was raised and warning-as-error is true should not succeed")
      (is (not (has-valid-warning? res)) "Response is :value but warning was raised and warning-as-error is true should not contain warning")
      (is (valid-eval-error? out) "Response is :value but warning was raised and warning-as-error is true should result in an js/Error")
      (is (re-find #"undeclared.*_arsenununpa42" (extract-message out)) "Response is :value but warning was raised and warning-as-error is true should have the right error msg")
      (repl/reset-env!))

    ;; Response is :value but warning was raised and warning-as-error is false
    (let [res (repl/read-eval-call target-opts validated-echo-cb
                                   "_arsenununpa42")
          out (unwrap-result res)]
      (is (success? res) "Response is :value but warning was raised and warning-as-error is false should succeed")
      (is (has-valid-warning? res) "Response is :value but warning was raised and warning-as-error is false should contain warning")
      (is (valid-eval-result? out) "Response is :value but warning was raised and warning-as-error is false should be a valid result")
      (is (= "nil" out) "Response is :value but warning was raised and warning-as-error is false should return nil")
      (repl/reset-env!))

    ;; Response is :value, no warning and warning-as-error is false
    (let [res (do (repl/read-eval-call target-opts validated-echo-cb "(def a 2)")
                  (repl/read-eval-call target-opts validated-echo-cb "a"))
          out (unwrap-result res)]
      (is (success? res) "Response is :value, no warning and warning-as-error is false should succeed")
      (is (not (has-valid-warning? res)) "Response is :value, no warning was raised and warning-as-error is false should not contain warning")
      (is (valid-eval-result? out) "Response is :value, no warning and warning-as-error is false should be a valid result")
      (is (= "2" out) "Response is :value, no warning and warning-as-error is false symbol should return 2")
      (repl/reset-env!))

    ;; Response is :value, no warning and warning-as-error is true
    (let [opts (merge target-opts {:warning-as-error true})
          res (do (repl/read-eval-call opts validated-echo-cb "(def a 2)")
                  (repl/read-eval-call opts validated-echo-cb "a"))
          out (unwrap-result res)]
      (is (success? res) "Response is :value, no warning and warning-as-error is true should succeed")
      (is (not (has-valid-warning? res)) "Response is :value, no warning was raised and warning-as-error is true should not contain warning")
      (is (valid-eval-result? out) "Response is :value, no warning and warning-as-error is true should be a valid result")
      (is (= "2" out) "Response is :value, no warning and warning-as-error is true symbol should return 2")
      (repl/reset-env!)))

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
      (is (= "true" out) "(defmacro hello ..) shoud return true")
      (repl/reset-env!))
    (let [res (do (read-eval-call "(defmacro hello [x] `(inc ~x))")
                  (read-eval-call "(hello nil nil 13)"))
          out (unwrap-result res)]
      (is (success? res) "Executing (defmacro hello ..) as function should succeed")
      (is (valid-eval-result? out) "Executing (defmacro hello ..) as function should have a valid result")
      (is (= "(inc 13)" out) "Executing (defmacro hello ..) as function shoud return (inc 13)")
      (repl/reset-env!))
    (let [res (do (read-eval-call "(ns foo.core$macros)")
                  (read-eval-call "(defmacro hello [x] (prn &form) `(inc ~x))")
                  (read-eval-call "(foo.core/hello (+ 2 3))"))
          out (unwrap-result res)]
      (is (success? res) "Executing (foo.core/hello ..) as function should succeed")
      (is (valid-eval-result? out) "Executing (foo.core/hello ..) hello ..) as function should have a valid result")
      (is (= "6" out) "Executing (foo.core/hello ..) hello ..) as function shoud return 6")
      (repl/reset-env! ["foo.core$macros"]))
    (let [res (do (read-eval-call "(ns foo.core$macros)")
                  (read-eval-call "(defmacro hello [x] (prn &form) `(inc ~x))")
                  (read-eval-call "(ns another.ns)")
                  (read-eval-call "(require-macros '[foo.core :refer [hello]])")
                  (read-eval-call "(hello (+ 2 3))"))
          out (unwrap-result res)]
      (is (success? res) "Executing (foo.core/hello ..) as function should succeed")
      (is (valid-eval-result? out) "Executing (foo.core/hello ..) hello ..) as function should have a valid result")
      (is (= "6" out) "Executing (foo.core/hello ..) hello ..) as function shoud return 6")
      (repl/reset-env! ["foo.core$macros"])))

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
      (is (= "#inst \"2010-11-12T18:14:15.666-00:00\"" out) "Reading #inst shoud return the object"))

    (let [res (read-eval-call "#uuid \"550e8400-e29b-41d4-a716-446655440000\"")
          out (unwrap-result res)]
      (is (success? res) "Reading #uuid should succeed")
      (is (valid-eval-result? out) "Reading #uuid should have a valid result")
      (is (= "#uuid \"550e8400-e29b-41d4-a716-446655440000\"" out) "Reading #uuid should return the object")))

  (deftest load-fn
    (let [load-map-atom (atom {})
          custom-load-fn (fn [load-map cb] (reset! load-map-atom load-map) (cb nil))
          target-opts (if (doo/node?)
                        (core/nodejs-options custom-load-fn)
                        (core/browser-options custom-load-fn))]
      (let [rs (repl/read-eval-call target-opts validated-echo-cb "(require 'bar.core)")]
        (is (= 'bar.core (:name @load-map-atom)) "Loading map with custom load-fn should have correct :name")
        (is (not (:macros @load-map-atom)) "Loading map with custom load-fn should have correct :macros")
        (is (= "bar/core" (:path @load-map-atom)) "Loading map with custom load-fn should have correct :path")
        (reset! load-map-atom {})
        (repl/reset-env! ["bar.core"])))))
