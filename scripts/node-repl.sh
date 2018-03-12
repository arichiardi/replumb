#!/bin/bash

compile_dir="dev-resources/private/node/js/compiled"
test_cp_dir="dev-resources/private/test"

repl_js="$compile_dir/nodejs-repl.js"
verbose=
cache_dir="$test_cp_dir/.replumb-cache"

if [ "$1" == "--simple" ]; then
    repl_js=dev-resources/private/node/js/simple/compiled/nodejs-repl.js
fi

if [[ "$@" =~ "--verbose" ]] || [[ "$@" == "-v" ]]; then
    verbose=true
else
    verbose=false
fi


classpath_dirs="$compile_dir/out:\
$test_cp_dir/src/cljs:\
$test_cp_dir/src/clj:\
$test_cp_dir/src/cljc:\
$test_cp_dir/test/src/js"

# For convenience you can pass --verbose here and it will be forwarded to the repl
node "$repl_js" $verbose "$cache_dir" "$classpath_dirs"
