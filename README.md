[![Build Status](https://travis-ci.org/ScalaConsultants/replumb.svg?branch=travis)](https://travis-ci.org/ScalaConsultants/replumb)

<p>
  <img src="https://raw.githubusercontent.com/ScalaConsultants/replumb/master/images/replumb_logo_bg.jpg" alt="Replumb Logo"/>
</p>

Replum (*'rɛplʌm*) is a plumbing library for your [bootstrapped](https://en.m.wikipedia.org/wiki/Bootstrapping_%28compilers%29) ClojureScript Read-Eval-Print-Loops.

This library tries to ease the burden of talking direcly to ClojureScript ```cljs.js``` and puts together [pieces](https://github.com/kanaka/cljs-bootstrap) [of](https://github.com/mfikes/planck) [work](https://github.com/mfikes/replete) contributed by the ClojureScript community at disparate points in time on the [`cljs-in-cljs`](https://github.com/clojure/clojurescript/wiki/Bootstrapping-the-Compiler#cljs-in-cljs-from-2012) subject. It aspires to be a common starting point for custom REPLs and/or REPL-ish apps (educational interactive environments for instance).

## Usage

__Leiningen__ ([via Clojars](https://clojars.org/replumb))

Put the following into the `:dependencies` vector.

[![Clojars Project](http://clojars.org/replumb/latest-version.svg)](http://clojars.org/replumb)

Then in your code, directly call ```replumb.core``` functions:

``` clojure
(defn handle-result!
  [console result]
  (let [write-fn (if (replumb/success? result) console/write-return! console/write-exception!)]
    (write-fn console (replumb/unwrap-result result))))

(defn cljs-read-eval-print!
  [console user-input]
  (replumb/read-eval-call (partial handle-result! console) user-input))
```

The core of it all is `read-eval-call`, which reads, evaluates and calls back
with the evaluation result.

The first parameter is a map of configuration options, currently
supporting:

* `:verbose`  will enable the the evaluation logging, defaults to false

* `:target`  `:nodejs` and `:default` supported, the latter used if missing
* `:init-fn!`  user provided initialization function, it will be passed a map:

            :form   ;; the form to evaluate, as data
            :ns     ;; the current namespace, as symbol
            :target ;; the current target
        
* `:read-file-fn!`  an asyncronous 2-arity function `(fn [filename source-cb]
...)` where source-cb is itself a function `(fn [source] ...)` that needs to be
called when ready with the found file source as string (`nil` if no file is
found).

* `:src-paths`  a vector of paths containing source files.

The second parameter, `callback`, should be a 1-arity function which receives
the result map, whose result keys will be:

```
:success?  ;; a boolean indicating if everything went right
:value     ;; (if (success? result)) will contain the actual yield of the evaluation
:error     ;; (if (not (success? result)) will contain a js/Error
:form      ;; the evaluated form as data structure (not a string)
```

It initializes the repl harness on the first execution if necessary. See
[```repl-demo```](https://github.com/ScalaConsultants/replumb/blob/master/repl-demo/browser/cljs/replumb_repl/console.cljs)
for an actual implementation using
[```jq-console```](https://github.com/replit/jq-console).


## Node.js

Support is provided, but only `:optimizations :none` works fine at the moment:

```clojure
(replumb/read-eval-call
  (replumb/nodejs-options src-paths node-read-file)
  (fn [res]
    (-> res
        replumb/result->string true
        println)
    (.setPrompt rl (replumb/get-prompt))
    (.prompt rl))
  cmd)
```

Where `node-read-file` is typically a client-provided file-system reading
function. See `nodejs-options` documentation.

The folder `repl-demo/node` contains a working example that can be built with
```lein node-repl*``` and launched with:

```
node dev-resources/private/node/compiled/nodejs-repl.js <src-path1:src-path2:src-path3>
```

You can also watch [Mike Fikes](https://www.youtube.com/watch?v=VwARsqTRw7s)'
demo or peek under the crook of his [elbow](https://github.com/mfikes/elbow).

## Design

The implementation was designed not to conceal ClojureScript's ```cljs.js/eval``` quirks and idiosyncrasies. Therefore tricks like Mike Fikes' ["Messing with macros at the REPL"](http://blog.fikesfarm.com/posts/2015-09-07-messing-with-macros-at-the-repl.html) are still part of the game and actually [implemented as tests](https://github.com/ScalaConsultants/replumb/blob/master/test/cljs/replumb/repl_test.cljs#L187).

This will in the long run be useful for both ```replumb``` and the ```ClojureScript``` community as it will help in finding and fix issues.

Another challenge faced during the development was about the asynchronous evaluation response: we were torn between keeping the callback model as per ```cljs.js``` or adopting the novel and cutting-edge channel model of ```core.async```. We did not see any strong evidence for adding channels here, therefore avoiding inflating the artifact size and delegating the choice to client code.

This project adheres to the [SemVer](http://semver.org/) specification.

### Documentation

The documentation referring to the latest version can always be found in the ```doc``` folder.

### Contributions

Contributions are welcome, any of them. Have a look at ```CONTRIBUTING.md``` for details.

### Thanks

As [current maintainer](https://github.com/arichiardi) of the project I would like to thank each and every present and future contributor, [Scalac](https://scalac.io) for actively supporting the (Clojure) open source community and [Mikes Fikes](https://github.com/mfikes) for having answered to my incessant questions on Slack and aided in brainstorming the project name. 

### License

Copyright (C) 2015 Scalac Sp. z o.o.

Distributed under the Eclipse Public License, the same as Clojure.
