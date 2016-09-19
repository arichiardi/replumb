(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-tests]]
            replumb.core-test
            replumb.repl-test
            replumb.common-test
            replumb.load-test
            replumb.options-test
            replumb.macro-test
            replumb.require-test
            replumb.source-test
            replumb.ast-test
            replumb.cache-test))

(enable-console-print!)

(doo-tests
 'replumb.core-test
 'replumb.repl-test
 'replumb.common-test
 'replumb.load-test
 'replumb.options-test
 'replumb.macro-test
 'replumb.require-test
 'replumb.source-test
 'replumb.ast-test
 'replumb.cache-test)
