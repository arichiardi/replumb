(ns replumb.source-node-test
  (:require [cljs.test :refer-macros [deftest is]]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.load :as load]
            [replumb.nodejs.io :as io]
            [replumb.source-test :as stest]))

(stest/make-tests (core/options :nodejs
                                ["dev-resources/private/test/node/compiled/out"
                                 "dev-resources/private/test/src/cljs"
                                 "dev-resources/private/test/src/clj"]
                                io/read-file!)
                  io/read-file!
                  io/write-file!
                  io/delete-file!)
