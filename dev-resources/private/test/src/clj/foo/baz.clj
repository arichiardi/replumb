(ns foo.baz)

(defmacro mul
  [a b]
  `(* ~a ~b))
