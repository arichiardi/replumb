(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-all-tests]]
            [replumb.core-test]
            [replumb.repl-test]
            [replumb.common-test]
            [replumb.load-test]))

(enable-console-print!)

(doo-all-tests #"^replumb.*-test")
