(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-all-tests]]
            replumb.core-test
            replumb.repl-test
            replumb.common-test
            replumb.load-test
            replumb.options-test
            replumb.require-node-test
            replumb.source-node-test
            replumb.cache-node-test))

(doo-all-tests #"^replumb.*-test")
