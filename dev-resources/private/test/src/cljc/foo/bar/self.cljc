(ns foo.bar.self
  #?(:cljs (:require-macros [foo.bar.self :refer [add]])))

#?(:clj 
  (defmacro add
    [a b]
    `(+ ~a ~b)))

#?(:cljs
  (defn sum
    [a b]
    (add a b)))
