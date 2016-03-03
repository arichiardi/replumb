(ns replumb.test-helpers
  (:require replumb.core
            replumb.repl
            goog.Promise)
  (:require-macros cljs.test))

(defn read-eval-call-promise
  "Return a promise that resolves with the result of accumulating the
  actual call to replumb.repl/read-eval-call on the source with results."
  [opts source results]
  {:pre [(vector? results)]}
  (goog.Promise.
   (fn [resolve, reject]
     (replumb.repl/read-eval-call
      opts
      (partial replumb.repl/validated-call-back!
               opts
               #(let [resolver (if (replumb.core/success? %) resolve reject)]
                  (resolver (conj results %))))
      source))))
