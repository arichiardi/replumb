# Changelog

## 0.1.4
- Dir support
- Apropos support
- Find-doc support
- Fix for [issue #86](https://github.com/ScalaConsultants/replumb/issues/86) - source does not work with aliased namespaces

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
