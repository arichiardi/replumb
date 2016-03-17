(ns replumb.ast-test
  (:require [cljs.test :refer-macros [deftest async is]]
            [replumb.core :as core :refer [success? unwrap-result]]
            [replumb.repl :as repl]
            [replumb.ast :as ast :refer [imports symbols macros requires macro-requires
                                         dissoc-macro dissoc-import dissoc-symbol dissoc-all
                                         dissoc-macro-require dissoc-require]]
            [replumb.test-env :as e]
            [replumb.test-helpers :as h :include-macros true]))

(def bar-core-load-fn (fn [m cb]
                        (cb {:lang :clj
                             :source "(ns bar.core)
                                      (defrecord User [name email])
                                      (defrecord Order [id])
                                      (defrecord Object [uuid])
                                      (def foo 42)
                                      (def bar 42)
                                      (def baz 42)
                                      (defmacro unless [pred a b]
                                        `(if (not ~pred)
                                          ~a
                                          ~b))
                                      (defmacro abs [x]
                                        `(unless (neg? ~x)
                                          ~x
                                          (- ~x)))
                                      (defmacro sqrt [x]
                                        `(unless (neg? x)
                                          (Math/sqrt x)
                                          (throw
                                            (ex-info \"Real plz\" {}))))"})))

(h/read-eval-call-test (assoc e/*target-opts* :load-fn! bar-core-load-fn)
  ["(require '[bar.core :refer [foo bar]])"
   "(require-macros '[bar.core :refer [abs sqrt]])"
   "(import '[bar.core User Order])"]

  (is (every? '#{foo bar} (keys (symbols @repl/st 'cljs.user))) "The function symbols should include only referred symbols")
  (is (not-any? '#{baz} (keys (symbols @repl/st 'cljs.user))) "The function symbols should NOT include not referred symbols")

  (is (every? '#{abs sqrt} (keys (macros @repl/st 'cljs.user))) "The function macros should return all the macro symbols")
  (is (not-any? '#{unless} (keys (macros @repl/st 'cljs.user))) "The function macros should NOT return not referred macro symbols")

  (is (every? '#{User Order} (keys (imports @repl/st 'cljs.user))) "The function imports should include only imported defrecords")
  (is (not-any? '#{Object} (keys (imports @repl/st 'cljs.user))) "The function imports should NOT include not imported defrecords")

  (is (every? '#{User Order bar.core} (keys (requires @repl/st 'cljs.user))) "The function requires should include only imported and required symbols")
  (is (not-any? '#{Object} (keys (requires @repl/st 'cljs.user))) "The function requires should NOT include not imported and required symbols")

  (let [st (dissoc-symbol @repl/st 'cljs.user 'foo)]
    (is (some '#{bar} (keys (symbols st 'cljs.user))) "Only foo should be present as symbol after (dissoc-symbol 'cljs.user 'foo)")
    (is (not-any? '#{foo} (keys (symbols st 'cljs.user))) "No trace of foo should be present as symbol after (dissoc-macro 'cljs.user 'foo)"))

  (let [st (dissoc-macro @repl/st 'cljs.user 'sqrt)]
    (is (some '#{abs} (keys (macros st 'cljs.user))) "Only abs should be present as macro after (dissoc-macro 'cljs.user 'sqrt)")
    (is (not-any? '#{sqrt} (keys (macros st 'cljs.user))) "No trace of sqrt should be present as macro after (dissoc-macro 'cljs.user 'sqrt)"))

  (let [st (dissoc-macro-require @repl/st 'cljs.user 'bar.core)]
    (is (not-any? '#{bar.core} (keys (macro-requires st 'cljs.user))) "No bar.core should be in macro-requires after (dissoc-macro-require 'cljs.user 'bar.core)"))

  (let [st (dissoc-require @repl/st 'cljs.user 'bar.core)]
    (is (not-any? '#{bar.core} (keys (requires st 'cljs.user))) "No bar.core should be in requires after (dissoc-require 'cljs.user 'bar.core)"))

  (let [st (dissoc-import @repl/st 'cljs.user 'User)]
    (is (not-any? '#{User} (keys (imports st 'cljs.user))) "No User should be in the imports after (dissoc-import 'cljs.user 'User)")
    (is (every? '#{Order} (keys (imports st 'cljs.user))) "Only Order should be in the imports after (dissoc-import 'cljs.user 'User)")
    (is (not-any? '#{User} (keys (requires st 'cljs.user))) "No User should be in the requires after (dissoc-import 'cljs.user 'User)")
    (is (every? '#{Order bar.core} (keys (requires st 'cljs.user))) "Only Order and bar.core should be in the requires after (dissoc-import 'cljs.user 'User)"))

  (let [st (dissoc-all @repl/st 'cljs.user 'bar.core :macro)]
    (is (not-any? '#{abs sqrt} (keys (macros st 'cljs.user))) "No trace of abs & sqrt should be visible as macro after (dissoc-all 'cljs.user 'bar.core :macro)"))

  (let [st (dissoc-all @repl/st 'cljs.user 'bar.core :symbol)]
    (is (not-any? '#{foo bar} (keys (symbols st 'cljs.user))) "No trace of abs & sqrt should be visible as macro after (dissoc-all 'cljs.user 'bar.core :symbol)"))

  (let [st (dissoc-all @repl/st 'cljs.user 'bar.core :import)]
    (is (not-any? '#{User Order} (keys (imports st 'cljs.user))) "No trace of abs & sqrt should be visible as macro after (dissoc-all 'cljs.user 'bar.core :import)"))

  (let [st (dissoc-all @repl/st 'cljs.user 'bar.core)]
    (is (not-any? '#{foo bar} (keys (symbols st 'cljs.user))) "No trace of abs & sqrt should be visible as macro after (dissoc-all 'cljs.user 'bar.core), defaulting to :symbol")))

(h/read-eval-call-test (assoc e/*target-opts* :load-fn! bar-core-load-fn)
  ["(require '[bar.core :as bc])"
   "(require-macros '[bar.core :as bc])"]

  (let [st (dissoc-require @repl/st 'cljs.user 'bar.core)]
    (is (not-any? '#{bar.core} (keys (requires st 'cljs.user))) "No bar.core should be in requires after (dissoc-require 'cljs.user 'bar.core)")
    (is (some '#{bc} (keys (requires st 'cljs.user))) "The bc alias should be in requires after (dissoc-require 'cljs.user 'bar.core), which does not remove aliases"))

  (let [st (dissoc-macro-require @repl/st 'cljs.user 'bar.core)]
    (is (not-any? '#{bar.core} (keys (macro-requires st 'cljs.user))) "No bar.core should be in macro-requires after (dissoc-macro-require 'cljs.user 'bar.core)")
    (is (some '#{bc} (keys (macro-requires st 'cljs.user))) "The bc alias should be in macro-requires after (dissoc-require 'cljs.user 'bar.core), which does not remove aliases"))

  (let [st (dissoc-all @repl/st 'cljs.user 'bar.core :require)]
    (is (not-any? '#{bc bar.core} (keys (requires st 'cljs.user))) "No bc alias and bar.core should be in requires after (dissoc-all 'cljs.user 'bar.core :require)"))

  (let [st (dissoc-all @repl/st 'cljs.user 'bar.core :macro-require)]
    (is (not-any? '#{bc bar.core} (keys (macro-requires st 'cljs.user))) "No bc alias and bar.core should be in macro-requires after (dissoc-all 'cljs.user 'bar.core :macro-require)")))

;; AR - the following implicitly tests replumb.repl/reset-env!. The compiler
;; state must be clean from the previous test or this won't pass
(h/read-eval-call-test e/*target-opts*
  ["(require 'clojure.string)"]
  (is (every? '#{clojure.string} (keys (requires @repl/st 'cljs.user))) "Should not return requires because no symbol was :refer-ed")
  (is (empty? (imports @repl/st 'cljs.user)) "Should not return imports because no symbol was :refer-ed")
  (is (empty? (macros @repl/st 'cljs.user)) "Should not return macros because no symbol was :refer-ed")

  (let [st (dissoc-require @repl/st 'cljs.user 'clojure.string)]
    (is (not-any? '#{clojure.string} (keys (requires st 'cljs.user))) "No bar.core should be in macro-ns (:require-macros ast key) after (dissoc-require 'cljs.user 'clojure.string)")))
