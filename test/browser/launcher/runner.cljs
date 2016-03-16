(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-tests]]
            replumb.core-test
            replumb.repl-test
            replumb.common-test
            replumb.load-test
            replumb.options-test
            replumb.macro-test
            #_replumb.require-test      ;; TODO browser test
            #_replumb.source-test       ;; TODO browser test
            #_replumb.cache-node-test)) ;; TODO port it to the new test way

(enable-console-print!)

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))

(set! goog.DEBUG false)

(doo-tests 'replumb.core-test
           'replumb.repl-test
           'replumb.common-test
           'replumb.load-test
           'replumb.options-test
           'replumb.macro-test
           #_'replumb.require-test
           #_'replumb.source-test
           #_'replumb.cache-node-test)
