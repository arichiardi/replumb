(ns replumb.ast
  (:refer-clojure :exclude [namespace ns-publics ns-interns]))

(defn known-namespaces
  "Given a compiler state, return the seq of namespace symbols currently
  present in the AST."
  [state]
  (keys (:cljs.analyzer/namespaces state)))

(defn ns-publics
  "Given compiler state and namespace symbol return all the public vars
  in the AST. Analagous to clojure.core/ns-publics but returns var
  analysis maps not vars (beware, it can be a lot of data)."
  ([state ns]
   {:pre [(symbol? ns)]}
   (->> (merge
         (get-in state [:cljs.analyzer/namespaces ns :macros])
         (get-in state [:cljs.analyzer/namespaces ns :defs]))
        (remove (fn [[k v]] (:private v)))
        (into {}))))

(defn ns-interns
  "Given compiler state and namespace symbol return all the vars in the
  AST. Analagous to clojure.core/ns-interns but returns var analysis
  maps not vars (beware, it can be a lot of data)."
  [state ns]
  {:pre [(symbol? ns)]}
  (merge
   (get-in state [:cljs.analyzer/namespaces ns :macros])
   (get-in state [:cljs.analyzer/namespaces ns :defs])))

(defn ns-defs
  "Given compiler state and namespace symbol, returns its AST :defs
  content (beware, it can be a lot of data)"
  [state ns]
  {:pre [(symbol? ns)]}
  (get-in state [:cljs.analyzer/namespaces ns :defs]))

(defn namespace
  "Given compiler state and namespace symbol, returns its whole AST
  content (beware, it can be a lot of data)."
  [state ns]
  {:pre [(symbol? ns)]}
  (get-in state [:cljs.analyzer/namespaces ns]))
