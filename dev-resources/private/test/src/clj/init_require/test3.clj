(ns init-require.test3)

(defmacro fun3
  [p]
  `(* ~init-require.test3/var3 ~p))
