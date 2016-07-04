(ns ^{:doc "Replumb core functions for self-hosted ClojureScript REPL implementations"}
    replumb.core
  (:require-macros [cljs.env.macros :refer [with-compiler-env]])
  (:require [cljs.js :as cljs]
            [replumb.repl :as repl]
            [replumb.common :as common]))

(defn ^:export read-eval-call
  "Reads, evaluates and calls back with the evaluation result.

  The first parameter is a map of configuration options, currently
  supporting:

  * `:verbose` will enable the the evaluation logging, defaults to false.
  To customize how to print, use `(set! *print-fn* (fn [& args] ...)`

  * `:warning-as-error` will consider a compiler warning as error.

  * `:target` `:nodejs` and `:browser` supported, the latter is used if
  missing.

  * `:init-fn!` user provided initialization function, it will be passed a
  map:

          :form   ;; the form to evaluate, as data
          :ns     ;; the current namespace, as symbol
          :target ;; the current target

  * `:load-fn!` will override replumb's default `cljs.js/*load-fn*`.
  It rules out `:read-file-fn!`, losing any perk of using `replumb.load`
  helpers. Trickily enough, `:load-fn!` is never used with `load-file`. It is the
  only case where it does not take precedence over `:read-file-fn!`. Use it if
  you know what you are doing and follow this protocol:

      ```
      Each runtime environment provides a different way to load a library.
      Whatever function `*load-fn*` is bound to will be passed two arguments
      - a map and a callback function: The map will have the following keys:

          :name   - the name of the library (a symbol)
          :macros - modifier signaling a macros namespace load
          :path   - munged relative library path (a string)

      The callback cb, upon resolution, will need to pass the same map:

          :lang       - the language, :clj or :js
          :source     - the source of the library (a string)
          :cache      - optional, if a :clj namespace has been precompiled to
                        :js, can give an analysis cache for faster loads.
          :source-map - optional, if a :clj namespace has been precompiled
                        to :js, can give a V3 source map JSON

      If the resource could not be resolved, the callback should be invoked with
      nil.
      ```

  * `:read-file-fn!` an asynchronous 2-arity function with signature
  `[file-path src-cb]` where src-cb is itself a function `(fn [source]
  ...)` that needs to be called with the file content as string (`nil`
  if no file is found). It is mutually exclusive with `:load-fn!` and
  will be ignored in case both are present.

  * `:write-file-fn!` a synchronous 2-arity function with signature
  `[file-path data]` that accepts a file-path and data to write.

  * `:src-paths` - a vector of paths containing source files.

  * `:cache` - a map containing two optional values: the first, `:path`,
  indicates the path of the cached files. The second, `:src-paths-lookup?`,
  indicates whether search the cached files in `:src-paths`. If both present,
  `:path` will have the priority but both will be inspected.

  * `:no-pr-str-on-value`  in case of `:success?` avoid converting the
  result map `:value` to string.

  * `:context` - indicates the evaluation context that will be passed to
  `cljs/eval-str`. One in `:expr`, `:statement`, `:return`. Defaults to `:expr`.
  If you really feel adventurous check [David Nolen's dev notes](https://github.com/clojure/clojurescript/blob/r1.7.228/devnotes/day1.org#tricky-bit---context).

  * `:foreign-libs` - a way to include foreign libraries. The format is analogous
  to the compiler option. For more info visit the [compiler options page](https://github.com/clojure/clojurescript/wiki/Compiler-Options#foreign-libs).

  * `:static-fns` - static dispatch in generated JavaScript. See the
  [compiler option page](https://github.com/clojure/clojurescript/wiki/Compiler-Options#static-fns).

  * `:preloads` - accepts either a sequence of symbols, akin to the core feature,
  or a map containing keys to specs, analogous to the `:ns` form syntax:
  ```
  {:preloads {:require '#{[my-ns.core :refer [init]] your-ns.core}
                  :use '#{their-ns}
                  :cb #(println \"Result:\" %)}}
  ```
  (Note the set, order does not matter)

  The second parameter, `callback`, should be a 1-arity function which receives
  the result map, whose result keys will be:

  ```
  :success?  a boolean indicating if everything went alright
  :value     (if (:success? result)), this key contains the yielded value as
             string, unless :no-pr-str-on-value is true, in which case it
             returns the bare value.
  :error     (if-not (:success? result)) will contain a js/Error
  :warning   in case a warning was thrown and :warning-as-error is falsey
  :form      the evaluated form as data structure (not string)}
  ```

  The third parameter is the source string to be read and evaluated."
  ([callback source] (repl/read-eval-call {} callback source))
  ([opts callback source] (repl/read-eval-call opts callback source)))

(defn ^:export get-prompt
  "Retrieves the REPL prompt to display, according to the current
  namespace. Returns a string."
  []
  (str (repl/current-ns) "=> "))

(defn ^:export error->str
  "Return the message string of the input `js/Error`."
  ([error] (common/extract-message error))
  ([print-stack? error] (common/extract-message print-stack? error)))

(defn ^:export unwrap-result
  "Unwraps the result of an evaluation.

  It returns the content of `:value` in case of success and the content
  of `:error` (a `js/Error`) in case of failure.

  When `include-warning?` is true, then the string returned is, in
  order, from the `:error`, `:warning` and eventually `:value` key in
  the result map."
  ([result-map]
   (unwrap-result false result-map))
  ([include-warning? result-map]
   (let [{:keys [error value warning]} result-map]
     (if error
       error
       (if (and include-warning? warning)
         warning
         value)))))

(defn ^:export success?
  "Given a `result-map`, tells whether the evaluation was successful."
  [result-map]
  (:success? result-map))

(defn ^:export result->string
  "Given a `result-map`, returns the result of the evaluation as string.

  - When `include-warning?` is true, then the string returned is, in
  order, from the `:error`, `:warning` and eventually `:value` key in
  the result map.

  - When `print-stack?` is true, the error string will include the stack
  trace."
  ([result-map]
   (result->string false false result-map))
  ([print-stack? result-map]
   (result->string print-stack? false result-map))
  ([print-stack? include-warning? result-map]
   {:pre [(not (map? print-stack?)) (not (map? include-warning?)) (map? result-map)]}
   (let [{:keys [error value warning]} result-map]
     (if error
       (common/extract-message print-stack? false error)
       (if (and include-warning? warning)
         warning
         value)))))

(defn ^:export options
  "Creates the right option map for read-eval-call.

  Supported targets: `:nodejs` or `:node`, `:browser`. It throws if not
  supported.

  The 2-arity function requires a `load-fn!` compatible with
  ClojureScript `cljs.js/*load-fn*`. Use it if you know what you are
  doing and follow this protocol:

      Each runtime environment provides a different way to load a library.
      Whatever function `*load-fn*` is bound to will be passed two arguments
      - a map and a callback function: The map will have the following keys:

          :name   - the name of the library (a symbol)
          :macros - modifier signaling a macros namespace load
          :path   - munged relative library path (a string)

      The callback cb, upon resolution, will need to pass the same map:

          :lang       - the language, :clj or :js
          :source     - the source of the library (a string)
          :cache      - optional, if a :clj namespace has been precompiled to
                        :js, can give an analysis cache for faster loads.
          :source-map - optional, if a :clj namespace has been precompiled
                        to :js, can give a V3 source map JSON

      If the resource could not be resolved, the callback should be invoked with
      nil.

  The 3-arity function accepts a sequence of source path strings and
  `read-file-fn!`, an asynchronous 2-arity function with signature
  `[file-path src-cb]` where src-cb is itself a function `(fn [source]
  ...)` that needs to be called with the file content as string (`nil`
  if no file is found).

  The 4-arity function receives additionally a fourth parameter `write-file-fn!`,
  a synchronous 2-arity function with signature `[file-path data]` that accepts
  a file-path and data to write."
  ([target load-fn!]
   (case target
     (:nodejs :node) {:target :nodejs
                      :load-fn! load-fn!}
     :browser {:target :default
               :load-fn! load-fn!}))
  ([target src-paths read-file-fn!]
   (options target src-paths read-file-fn! nil))
  ([target src-paths read-file-fn! write-file-fn!]
   (case target
     (:nodejs :node) {:target :nodejs
                      :read-file-fn! read-file-fn!
                      :src-paths src-paths
                      :write-file-fn! write-file-fn!}
     :browser {:target :default
               :read-file-fn! read-file-fn!
               :src-paths src-paths
               :write-file-fn! write-file-fn!})))

(defn repl-reset!
  "Reset the repl and the current compiler state.

  It performs the following (in order):

  1. removes `cljs.js/*loaded*` namespaces from the compiler environment
  2. calls `(read-eval-call (in-ns 'cljs.user))`
  3. resets the last warning
  4. sets `*e` to nil
  5. resets the init options (the next eval will trigger an init)"
  [opts]
  ;; Clean cljs.user
  (when-not (repl/empty-cljs-user?)
    (repl/purge-cljs-user!))
  ;; Back to cljs.user, has to be first
  (repl/read-eval-call opts identity "(in-ns 'cljs.user)")
  ;; Other side effects
  (repl/reset-last-warning!)
  (repl/read-eval-call opts identity "(set! *e nil)"))
