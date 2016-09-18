(ns ^{:doc "Thin layer of helpers over the ClojureScript Abstract syntax tree (AST)"}
    replumb.ast
  (:refer-clojure :exclude [namespace ns-publics ns-interns])
  (:require goog.string))

(defn known-namespaces
  "Given a compiler state, return the seq of namespace symbols currently
  present in the AST."
  [state]
  (remove nil? (keys (:cljs.analyzer/namespaces state))))

(defn ns-publics
  "Given compiler state and namespace symbol return all the public vars
  in the AST.

  Analogous to `cljs.analyzer/ns-publics`, but it returns analysis maps
  not vars.

  Beware, there can be a lot of data."
  ([state ns]
   {:pre [(symbol? ns)]}
   (->> (merge
         (get-in state [:cljs.analyzer/namespaces ns :macros])
         (get-in state [:cljs.analyzer/namespaces ns :defs]))
        (remove (fn [[k v]] (:private v)))
        (into {}))))

(defn ns-interns
  "Given compiler state and namespace symbol return all the vars in the
  AST.

  Analogous to `cljs.analyzer/ns-interns`, but it returns analysis maps
  not vars.

  Beware, there can be a lot of data."
  [state ns]
  {:pre [(symbol? ns)]}
  (merge
   (get-in state [:cljs.analyzer/namespaces ns :macros])
   (get-in state [:cljs.analyzer/namespaces ns :defs])))

(defn ns-defs
  "Given compiler state and namespace symbol, returns its AST's `:defs`
  key content.

  Beware, there can be a lot of data."
  [state ns]
  {:pre [(symbol? ns)]}
  (get-in state [:cljs.analyzer/namespaces ns :defs]))

(defn ns-macros
  "Given compiler state and namespace symbol, returns its AST's `:macros`
  key content.

  Beware, there can be a lot of data."
  [state ns]
  {:pre [(symbol? ns)]}
  (get-in state [:cljs.analyzer/namespaces ns :macros]))

(defn namespace
  "Given compiler state and namespace symbol, returns its whole AST
  content.

  Beware, there can be a lot of data."
  [state ns]
  {:pre [(symbol? ns)]}
  (get-in state [:cljs.analyzer/namespaces ns]))

(defn requires
  "Return required symbols given compiler state and namespace: a map of
  `{ns ns, another-ns another-ns, ...}`.

  Note that `import` also adds something to the AST's `:requires` key of
  the requirer-ns, see `replumb.ast/dissoc-import`.

  You need a `require` in the requirer-ns namespace for this to return
  something."
  [state requirer-ns]
  {:pre [(symbol? requirer-ns)]}
  (get-in state [:cljs.analyzer/namespaces requirer-ns :requires]))

(defn imports
  "Return imported symbols given compiler state and a namespace: a map
  of `{symbol1 ns, symbol2 ns, ...}`.

  Note that an `import` symbol is the final segment only, so `User` in
  the `foo.bar.User`

  You need a `import` in the requirer-ns namespace for this to return
  something."
  [state requirer-ns]
  {:pre [(symbol? requirer-ns)]}
  (get-in state [:cljs.analyzer/namespaces requirer-ns :imports]))

(defn symbols
  "Return referred/used symbols given compiler state and a namespace: a
  map of `{symbol1 ns, symbol2 ns, ...}`.

  You need a `:refer` in the requirer-ns namespace for this to return
  something."
  [state requirer-ns]
  {:pre [(symbol? requirer-ns)]}
  (get-in state [:cljs.analyzer/namespaces requirer-ns :uses]))

(defn macros
  "Return all the macro symbols given compiler state and namespace: a
  map of `{macro-symbol1 macro-ns, macro-symbol2 macro-ns}`.

  You need a `:refer` in the requirer-ns namespace for this to return
  something."
  [state requirer-ns]
  {:pre [(symbol? requirer-ns)]}
  (get-in state [:cljs.analyzer/namespaces requirer-ns :use-macros]))

(defn macro-requires
  "Return all the required macro namespaces given compiler state and a
  namespace: a map of `{macro-ns1 macro-ns1, macro-ns2 macro-ns2, ...}`.

  You need a `require-macros` in the requirer-ns namespace for this to
  return something."
  [state requirer-ns]
  {:pre [(symbol? requirer-ns)]}
  (get-in state [:cljs.analyzer/namespaces requirer-ns :require-macros]))

(defn require-of-ns?
  "Yields true when the symbol belongs to ns.

  This typically works on the output of either `replumb.ast/requires` or
  `replumb.ast/macro-requires`."
  [ns sym]
  (= ns sym))

(defn symbol-of-ns?
  "Yields true when the symbol belongs to ns.

  This typically works on the output of `replumb.ast/symbols`."
  [ns sym]
  (= ns sym))

(defn import-of-ns?
  "Yields true when sym belongs to ns.

  This typically works on the output of `replumb.ast/imports`."
  [ns sym]
  (goog.string.caseInsensitiveContains (str sym) (str ns)))

(defn macro-of-ns?
  "Yields true when the sym belongs to ns.

  This typically works on the output of `replumb.ast/macros`."
  [ns sym]
  (= ns sym))

;; from https://github.com/mfikes/planck/commit/fe9e7b3ee055930523af1ea3ec9b53407ed2b8c8
(defn dissoc-ns
  "Dissoc the namespace symbol from the compiler state."
  [state ns]
  {:pre [(symbol? ns)]}
  (update-in state [:cljs.analyzer/namespaces] dissoc ns))

(defn dissoc-symbol
  "Dissoc symbol from the compiler state given the symbol of the
  namespace where `require` (or `use`) was called from."
  [state requirer-ns sym]
  {:pre [(symbol? requirer-ns) (symbol? sym)]}
  (update-in state [:cljs.analyzer/namespaces requirer-ns :uses] dissoc sym))

(defn dissoc-import
  "Dissoc the imported symbol from the compiler state."
  [state requirer-ns sym]
  {:pre [(symbol? requirer-ns) (symbol? sym)]}
  (-> state
      (update-in [:cljs.analyzer/namespaces requirer-ns :requires] dissoc sym)
      (update-in [:cljs.analyzer/namespaces requirer-ns :imports] dissoc sym)))

(defn dissoc-macro
  "Dissoc a macro symbol from the compiler state given the symbol of the
  namespace where `require-macros` (or `use-macros`) was called from."
  [state requirer-ns sym]
  {:pre [(symbol? requirer-ns) (symbol? sym)]}
  (update-in state [:cljs.analyzer/namespaces requirer-ns :use-macros] dissoc sym))

(defn dissoc-require
  "Dissoc the required-ns from requirer-ns.

  For instance after:

  ```
  (in-ns 'cljs.user)        ;; requirer-ns
  (require 'clojure.string) ;; required-ns
  ```

  You can use the following to clean the compiler state:

  ```
  (dissoc-require repl/st 'cljs.user 'clojure.string)
  ```

  This util function does not remove aliases. See
  `replumb.ast/dissoc-all`."
  [state requirer-ns required-ns]
  {:pre [(symbol? requirer-ns) (symbol? required-ns)]}
  (update-in state [:cljs.analyzer/namespaces requirer-ns :requires] dissoc required-ns))

(defn dissoc-macro-require
  "Dissoc the macro required-ns from requirer-ns.

  For instance after:

  ```
  (in-ns 'cljs.user)          ;; requirer-ns
  (require-macros 'cljs.test) ;; required-ns
  ```

  You can use the following to clean the compiler state:

  ```
  (dissoc-macro-require repl/st 'cljs.user 'cljs.test)
  ```

  This util function does not remove aliases. See
  `replumb.ast/dissoc-all`."
  [state requirer-ns required-ns]
  {:pre [(symbol? requirer-ns) (symbol? required-ns)]}
  (update-in state [:cljs.analyzer/namespaces requirer-ns :require-macros] dissoc required-ns))

(defn dissoc-all
  "Dissoc all the required-ns symbols from requirer-ns.

  There are five types of symbol in replumb jargon, which loosely map to
  `cljs.js` ones. These optionally go in the type parameter as keyword:

  - `:symbol`, the default, for instance my-sym in `(def my-sym 3)`
  - `:macro`, which comes from a `(defmacro ...)`
  - `:import`, for instance User in `(import 'foo.bar.User)`
  - `:require`, which is the namespace symbol in a `(require ...)`
  - `:macro-require`, which is the namespace symbol in a `(require-macros ...)`

  This is the only function in the lot that also reliably clears
  namespace aliases."
  ([state requirer-ns required-ns]
   (dissoc-all state requirer-ns required-ns :symbol))
  ([state requirer-ns required-ns type]
   (let [[get-fn pred dissoc-fn] (case type
                                   :require [requires #(require-of-ns? required-ns (second %)) dissoc-require]
                                   :macro-require [macro-requires #(require-of-ns? required-ns (second %)) dissoc-macro-require]
                                   :symbol [symbols #(symbol-of-ns? required-ns (second %)) dissoc-symbol]
                                   :macro [macros #(macro-of-ns? required-ns (second %)) dissoc-macro]
                                   :import [imports #(import-of-ns? required-ns (second %)) dissoc-import])]
     (let [syms (get-fn state requirer-ns)
           required-syms (map first (filter pred syms))]
       (reduce #(dissoc-fn %1 requirer-ns %2) state required-syms)))))

(defn get-state
  "Retrieves a map with the state details for the input requirer-ns.

  In replumb jargon:

  ```
  (in-ns 'cljs.user)          ;; requirer-ns
  (require-macros 'cljs.test) ;; required-ns
  ```

  And the returned map is (on the right, the compiler AST real key):

  ```
  {:requires (replumb.ast/requires ...)             ;; :requires
   :macro-requires (replumb.ast/macro-requires ...) ;; :require-macros
   :symbols (replumb.ast/symbols ...)               ;; :uses
   :macros (replumb.ast/macros ...)                 ;; :use-macros
   :imports (replumb.ast/imports ...)}              ;; :imports
  ```"
  [state requirer-ns]
  {:requires (requires state requirer-ns)
   :macro-requires (macro-requires state requirer-ns)
   :symbols (symbols state requirer-ns)
   :macros (macros state requirer-ns)
   :imports (imports state requirer-ns)})

(defn empty-state?
  "Return true if the compiler state is empty.

  In other words if `(every? empty? (vals (replumb.ast/get-state ...)`
  returns true for the input requirer-ns.

  In replumb jargon:

  ```
  (in-ns 'cljs.user)          ;; requirer-ns
  (require-macros 'cljs.test) ;; required-ns
  ```"
  [state requirer-ns]
  (every? empty? (vals (get-state state requirer-ns))))
