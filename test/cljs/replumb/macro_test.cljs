(ns replumb.macro-test
  (:require[cljs.test :refer-macros [deftest testing is async]]
           [replumb.core :as core :refer [success? unwrap-result]]
           [replumb.common :as common :refer [valid-eval-result? extract-message valid-eval-error?]]
           [replumb.test-env :as e]
           [replumb.test-helpers :as h :refer-macros [read-eval-call-test]]))

;; Implementing examples from Mike Fikes work at:
;; http://blog.fikesfarm.com/posts/2015-09-07-messing-with-macros-at-the-repl.html
;; (it's not that I don't trust Mike, you know)
(h/read-eval-call-test e/*target-opts*
  ["(defmacro hello [x] `(inc ~x))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "true" out) (str _msg_ "should return true")))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(defmacro hello [x] `(inc ~x))"
   "(hello nil nil 13)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "(cljs.core/inc 13)" out) (str _msg_ "should return (cljs.core/inc 13)")))
  (_reset!_))

(h/read-eval-call-test e/*target-opts*
  ["(ns foo.core$macros)"
   "(defmacro hello [x] (prn &form) `(inc ~x))"
   "(foo.core/hello (+ 2 3))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "6" out) (str _msg_ "should return 6")))
  (_reset!_ '[foo.core]))

(h/read-eval-call-test e/*target-opts*
  ["(ns foo.core$macros)"
   "(defmacro hello [x] (prn &form) `(inc ~x))"
   "(ns another.ns)"
   "(require-macros '[foo.core :refer [hello]])"
   "(hello (+ 2 3))"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed."))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "6" out) (str _msg_ "should return 6")))
  (_reset!_ '[foo.core another.ns]))

;; From http://blog.fikesfarm.com/posts/2016-03-04-collapsing-macro-tower.html
;; AR - No need for towering when a macro expands a macro
(h/read-eval-call-test
  (assoc e/*target-opts*
         :load-fn! (fn [m cb]
                     (cb {:lang   :clj
                          :source "(ns bar.core)
                                   (defmacro unless [pred a b]
                                     `(if (not ~pred)
                                       ~a
                                       ~b))
                                   (defmacro abs [x]
                                     `(unless (neg? ~x)
                                       ~x
                                       (- ~x)))"})))
  ["(require-macros 'bar.core)"
   "(bar.core/abs -17)"]
  (let [out (unwrap-result @_res_)]
    (is (success? @_res_) (str _msg_ "should succeed"))
    (is (valid-eval-result? out) (str _msg_ "should be a valid result"))
    (is (= "17" out ) (str _msg_ "should result in 17")))
  (_reset!_ '[bar.core]))
