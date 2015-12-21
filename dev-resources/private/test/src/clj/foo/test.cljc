(ns foo.test)

(defmacro add
  [a b]
  `(+ ~a ~b))
