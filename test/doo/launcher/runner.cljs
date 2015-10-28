(ns  ^:figwheel-always launcher.runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [replumb.core-test]
            [replumb.repl-test]
            [replumb.common-test]))

(enable-console-print!)

(doo-all-tests #"^replumb.*-test")
