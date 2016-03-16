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
            #_replumb.cache-node-test)) ;; TODO port it to the new test way

(enable-console-print!)

(set! goog.DEBUG false)

(doo-tests 'replumb.core-test
           'replumb.repl-test
           'replumb.common-test
           'replumb.load-test
           'replumb.options-test
           'replumb.macro-test
           'replumb.require-test
           'replumb.source-test
           #_'replumb.cache-node-test)
