(ns foo.load)

(require 'foo.bar.baz)
(require 'clojure.string)

(def a true)

(defn b [] (+ 10 20 20))

(defn my-trim
  []
  (clojure.string/trim (str "    " foo.bar.baz/a "    ")))

(defn c [number] (inc number))
