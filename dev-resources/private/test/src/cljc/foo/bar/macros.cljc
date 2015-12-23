(ns foo.bar.macros)

(defmacro str->int [s]
  #?(:clj  (Integer/parseInt s)
     :cljs (js/parseInt s)))
