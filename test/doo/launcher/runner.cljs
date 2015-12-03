(ns launcher.runner
  (:require [doo.runner :as doo :refer-macros [doo-all-tests]]
            [replumb.core-test]
            [replumb.repl-test]
            [replumb.common-test]
            [replumb.load-test]
            [replumb.options-test]
            [replumb.require-test]))

(enable-console-print!)

(doo-all-tests #"^replumb.*-test")
