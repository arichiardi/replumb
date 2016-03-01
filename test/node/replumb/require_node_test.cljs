(ns replumb.require-node-test
  (:require [cljs.test :refer-macros [deftest is]]
            [clojure.string :as s]
            [cljs.nodejs :as nodejs]
            [doo.runner :as doo]
            [replumb.core :as core :refer [options success? unwrap-result]]
            [replumb.common :as common :refer [echo-callback valid-eval-result?
                                               extract-message valid-eval-error?]]
            [replumb.repl :as repl]
            [replumb.ast :as ast]
            [replumb.load :as load]
            [replumb.nodejs.io :as io]
            [replumb.require-test :as rtest]))

(rtest/make-tests (core/options :nodejs
                                ["dev-resources/private/test/node/compiled/out"
                                 "dev-resources/private/test/src/cljs"
                                 "dev-resources/private/test/src/clj"
                                 "dev-resources/private/test/src/cljc"
                                 "dev-resources/private/test/src/js"]
                                io/read-file!)
                  io/read-file!
                  io/write-file!
                  io/delete-file!)
