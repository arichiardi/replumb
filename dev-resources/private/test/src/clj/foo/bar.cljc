(ns foo.bar)

(defmacro add
  [a b]
  `(+ ~a ~b))
