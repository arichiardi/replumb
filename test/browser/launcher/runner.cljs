(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-all-tests]]
            replumb.core-test
            replumb.repl-test
            replumb.common-test
            replumb.load-test
            replumb.options-test
            replumb.require-browser-test
            replumb.source-browser-test))

(doo-all-tests #"^replumb.*-test")
