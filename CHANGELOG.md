# Changelog

## 0.2.0

- _\*API breaking\*_ Change signature of `result->string`, `error->str` and `unwrap-result`
- _\*API breaking\*_ Merge `nodejs-options` and `browser-options` in `replumb.core/options` 
- Clearing the repl is first class citizen, introducing `replumb.core/repl-reset!`
- Brand new helper functions for querying and dissoc-ing symbols from the compiler AST in `replumb.ast`
- The project now follows the new figwheel way to launch a Clojurescript repl through `lein repl`
- Add (Collapsing Macro Tower)[http://blog.fikesfarm.com/posts/2016-03-04-collapsing-macro-tower.html] tests
- The function replumb.ast/known-namespaces now filters out spurious nils
- Fix for [issue #66](https://github.com/Lambda-X/replumb/issues/66) - referring a wrong symbol wreaks havoc

## 0.1.5
- Expose replumb equivalent of Clojurescript compiler option `:foreign-libs`
- Expose the Clojurescript compiler option `:context`
- Evaluation cache
- Clojurescript 1.7.228 support
- Load-file support (whole file sent to `cljs.js.eval-str`, whicn can be)
- The new option `:no-pr-str-on-value` avoids converting the result value to string
- `replumb.common/debug-prn` does not print to error anymore
- Fix for [issue #117](https://github.com/Lambda-X/replumb/issues/117) - Syntax-quote doesn't auto-qualify symbols - [TRDR-33](http://dev.clojure.org/jira/browse/TRDR-33)
- Fix for [issue #119](https://github.com/Lambda-X/replumb/issues/119) - Cljs.js/eval cb argument - [CLJS-1425](http://dev.clojure.org/jira/browse/CLJS-1425)
- Upstream fix for [issue #91](https://github.com/ScalaConsultants/replumb/issues/91) - :refer-macros fails when implicit
- Fix for [issue #85](https://github.com/ScalaConsultants/replumb/issues/85) - source does not work with macros
- Fix for [issue #81](https://github.com/ScalaConsultants/replumb/issues/81) - missing doc for ns-interns

## 0.1.4
- Dir support
- Apropos support
- Find-doc support
- Initialization is now also triggered when an option in `#{:src-paths :init-fn!}` changes from the previous `read-eval-call`
- Fix for [issue #86](https://github.com/ScalaConsultants/replumb/issues/86) - source does not work with aliased namespaces
- Fix for [issue #100](https://github.com/ScalaConsultants/replumb/issues/100) - make sure every `:src-paths` is string

## 0.1.3
- Require support through custom `:load-fn!` or `read-file-fn!` plus `:src-paths` options, see [wiki](https://github.com/ScalaConsultants/replumb/wiki/Require-and-providing-source-files)
- Source support
- New `replumb.browser.io` and `replumb.node.io` namespaces with some `read-file-fn!` sample implementation
- Support for importing `goog` namespaces (if present in `:src-paths`), solves [issue #59](https://github.com/ScalaConsultants/replumb/issues/59) and [issue #63](https://github.com/ScalaConsultants/replumb/issues/63)
- Add `:warning-as-error` option for treating analyzer warnings as errors
- The result map now contains a `:warning` key when `:warning-as-error` is false
- Fix for [issue #49](https://github.com/ScalaConsultants/replumb/issues/49) - `(require a.namespace)` generates an unwanted error/warning
- `(doc a.namespace)` correctly prints the docstring
- Fix for [issue #36](https://github.com/ScalaConsultants/replumb/issues/36) - missing doc for special symbols
- Fix for [issue #35](https://github.com/ScalaConsultants/replumb/issues/35) - no `*load-fn*` when requiring a new namespace

## 0.1.2
- Add Node.js support and repl demo
- Fix for [issue #20](https://github.com/ScalaConsultants/replumb/issues/20) - `ERROR: JSC_NON_GLOBAL_DEFINE_INIT_ERROR. @define variable cljs.core._STAR_target_STAR_ assignment must be global`

## 0.1.1
- Tagged literals error fix

## 0.1.0
- Initial release
