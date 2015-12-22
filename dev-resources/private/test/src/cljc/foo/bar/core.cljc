(ns foo.bar.core
  (#?(:clj  :require
      :cljs :require-macros) [foo.bar.macros]))

(defmacro mul-core
  [a b]
  `(* ~a ~b))

(defn str->int [s]
  #?(:clj  (Integer/parseInt s)
     :cljs (js/parseInt s)))

(defn add-five [s]
  (+ (str->int s)
     (foo.bar.macros/str->int "5")))
