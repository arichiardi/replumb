(ns replumb.common
  (:require [clojure.string :as string]))

;; js/Error tree representation
(defn error-branch?
  [error]
  (instance? js/Error error))

(defn error-children
  [error]
  [(.-cause error)])

(defn error-seq
  [error]
  (tree-seq error-branch? error-children error))

(defn extract-message
  "Iteratively extracts messages inside (nested #error objects), returns
  a string. If the boolean `exclude-error-msg?` is true, the \"ERROR\"
  only message will be included in the final string. If the boolean
  `print-stack?` is true, the stack will be added to the final
  string. They both default to false.

  ** Be sure to pass a js/Error object here **"
  ([err]
   (extract-message err false false))
  ([err exclude-error-msg?]
   (extract-message err exclude-error-msg? false))
  ([err exclude-error-msg? print-stack?]
   (str (let [strings (cond->> (keep identity (error-seq err))
                           exclude-error-msg? (filter #(not= "ERROR" (.-message %1)))
                           true (map #(.-message %1))
                           true (filter (complement empty?)))]
          (if (seq strings)
            (string/join " - " strings)
            "Error"))
        (when print-stack?
          (str "\n" (string/join "\n" (drop 1 (string/split-lines (.-stack err)))))))))

(defn echo-callback
  "Callback that just echoes the result map. It also asserts the correct
  result format in its post condition. Useful for debugging and
  testing."
  {:post [(map? %) (find % :success?) (or (find % :error) (find % :value))]} ;; TODO, use dire or schema
  [result-map]
  result-map)

(defn wrap-success
  "Wraps the message in a success map."
  [message]
  {:value message})

(defn wrap-error
  "Wraps the message in a error map."
  [message]
  {:error message})

(defn inline-newline?
  "Returns true if the string contains the newline \\\\n or \\\\r as
  characters."
  [s]
  (re-matches #"\\{2,}n|\\{2,}r" s))

(defn valid-eval-result?
  "Is the string returned from an evaluation valid?"
  [result]
  (and (string? result) (not (inline-newline? result))))

(defn valid-eval-error?
  "Is the string returned from an evaluation valid?"
  [error]
  (instance? js/Error error))

(defn error-keyword-not-supported
  "Yields a \"keyword not supported\" error map. Receives the
  symbol/keyword printed in the message and ex-info data."
  [keyword ex-info-data]
  (wrap-error (ex-info (str "The " keyword " keyword is not supported at the moment")
                       ex-info-data)))

(defn error-argument-must-be-symbol
  "Yields a \"Argument must a be a symbol\" error map. Receives the
  symbol/fn name printed in the message and ex-info data."
  [symbol ex-info-data]
  (wrap-error (ex-info (str "Argument to " symbol " must be a symbol") ex-info-data)))
