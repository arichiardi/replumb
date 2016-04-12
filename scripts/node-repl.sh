#!/bin/bash

JS=dev-resources/private/node/js/compiled/nodejs-repl.js
VERBOSE=

if [ "$1" == "--simple" ]; then
    JS=dev-resources/private/node/js/simple/compiled/nodejs-repl.js
fi

if [[ "$1" == "--verbose" ]] || [[ $2 == "--verbose" ]]
then
    VERBOSE=--verbose
fi

# For convenience you can pass --verbose here and it will be forwarded to the repl
node $JS $VERBOSE \
     dev-resources/private/node/js/compiled/out:dev-resources/private/test/src/cljs:dev-resources/private/test/src/clj:dev-resources/private/test/src/cljc:dev-resources/private/test/src/js
