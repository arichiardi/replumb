#!/bin/bash

# For convenience you can pass --verbose here and it will be forwarded to the repl
node dev-resources/private/node/compiled/nodejs-repl.js $1 \
     dev-resources/private/node/compiled/out:dev-resources/private/test/src/cljs:dev-resources/private/test/src/clj:dev-resources/private/test/src/cljc
