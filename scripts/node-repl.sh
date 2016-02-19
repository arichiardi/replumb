#!/bin/bash

# For convenience you can pass --verbose here and it will be forwarded to the repl
node dev-resources/private/node/js/compiled/nodejs-repl.js $1 \
     dev-resources/private/node/js/compiled/out:dev-resources/private/test/src/cljs:dev-resources/private/test/src/clj:dev-resources/private/test/src/cljc:dev-resources/private/test/src/js
