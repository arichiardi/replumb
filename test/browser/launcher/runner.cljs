(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-all-tests]]
            replumb.core-test
            replumb.repl-test
            replumb.common-test
            replumb.load-test
            replumb.options-test
            replumb.require-browser-test
            replumb.source-browser-test))

;; Or doo will exit with an error, see:
;; https://github.com/bensu/doo/issues/83#issuecomment-165498172
(set! (.-error js/console) (fn [x] (.log js/console x)))

(doo-all-tests #"^replumb.*-test")
