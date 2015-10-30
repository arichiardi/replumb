(ns replumb.core
  (:require-macros [cljs.env.macros :refer [with-compiler-env]])
  (:require [cljs.js :as cljs]
            [replumb.repl :as repl]
            [replumb.common :as common]))

(defn ^:export read-eval-call
  "Reads, evaluates and calls back with the evaluation result.

  The first parameter is a map of configuration options, currently
  supporting:

  * `:verbose` will enable the the evaluation logging, defaults to false.

  The second parameter `cb`, should be a 1-arity function which receives
  the result map.

  Therefore, given a callback `(fn [result-map] ...)`, the result keys are:

  ```
  :success? ;; a boolean indicating if everything went right
  :value    ;; (if (success? result)) will contain the actual yield of the evaluation
  :error    ;; (if (not (success? result)) will contain a js/Error
  :form     ;; the evaluated form as data structure (not a string)
  ```

  It initializes the repl harness if necessary."
  ([callback source] (repl/read-eval-call {} callback source))
  ([opts callback source] (repl/read-eval-call opts callback source)))

(defn ^:export get-prompt
  "Retrieves the repl prompt to display, according to the current
  namespace. Returns a string."
  []
  (str (repl/current-ns) "=> "))

(defn ^:export error->str
  "Return the message string of the input `js/Error`."
  ([error] (common/extract-message error))
  ([error print-stack?] (common/extract-message error print-stack?)))

(defn ^:export unwrap-result
  "Unwraps the result of an evaluation.

  It returns the content of `:value` in case of success and the content
  of `:error` (a `js/Error`) in case of failure."
  [result-map]
  (if (:success? result-map)
    (:value result-map)
    (:error result-map)))

(defn ^:export success?
  "Given a `result-map`, tells whether the evaluation was successful."
  [result-map]
  (:success? result-map))

(defn ^:export result->string
  "Given a `result-map`, returns the result of an evaluation as string."
  ([result-map]
   (result->string result-map false))
  ([result-map print-stack?]
   (if (:success? result-map)
     (:value result-map)
     (common/extract-message (:error result-map) print-stack?))))
