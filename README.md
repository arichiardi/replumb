[![Build Status](https://travis-ci.org/Lambda-X/replumb.svg?branch=travis)](https://travis-ci.org/Lambda-X/replumb)

<p>
  <img src="https://raw.githubusercontent.com/ScalaConsultants/replumb/master/images/replumb_logo_bg.jpg" alt="Replumb Logo"/>
</p>

Replumb (*'rɛplʌm*) is a plumbing library for your [self-hosted](https://en.m.wikipedia.org/wiki/Self-hosting_compiler) ClojureScript Read-Eval-Print-Loops. Live demo available at [clojurescript.io](http://clojurescript.io).

This library tries to ease the burden of talking directly to ClojureScript ```cljs.js``` and puts together [pieces](https://github.com/kanaka/cljs-bootstrap) [of](https://github.com/mfikes/planck) [work](https://github.com/mfikes/replete) contributed by the ClojureScript community at disparate points in time on the [`cljs-in-cljs`](https://github.com/clojure/clojurescript/wiki/Bootstrapping-the-Compiler#cljs-in-cljs-from-2012) subject. It aspires to be a common starting point for custom REPLs and/or REPL-ish apps (educational interactive environments for instance).

### Thanks

As [current maintainer](https://github.com/arichiardi) of the project I would like to thank each and every present and future contributor, [Scalac](https://scalac.io) for actively supporting the (Clojure) open source community and [Mikes Fikes](https://github.com/mfikes) for having answered to my incessant questions on Slack, for having aided in brainstorming the project name and of course for the symbiosis with [planck](https://github.com/mfikes/planck).

## Usage

Put the following into the `:dependencies` vector.

[![via Clojars](http://clojars.org/replumb/latest-version.svg)](http://clojars.org/replumb)

After that directly call ```replumb.core``` functions:

``` clojure
(ns ...
  (:require ...
            [replumb.core :as replumb]
            [your.io.impl :as io]))

(defn handle-result!
  [console result]
  (let [write-fn (if (replumb/success? result) console/write-return! console/write-exception!)]
    (write-fn console (replumb/unwrap-result result))))

(defn cljs-read-eval-print!
  [console repl-opts user-input]
  (replumb/read-eval-call repl-opts
                          (partial handle-result! console)
                          user-input))

(defn cljs-console-did-mount
  [console-opts]
  (js/$
   (fn []
     (let [repl-opts (merge (replumb/browser-options ["/src/cljs" "/js/compiled/out"]
                                                     io/fetch-file!)
                            {:warning-as-error true
                             :verbose true})
           jqconsole (console/new-jqconsole "#cljs-console" console-opts)]
       (cljs-console-prompt! jqconsole repl-opts)))))
```

Note that `replumb`, like ClojureScript, abstracts over `IO` details. You will
need to provide your own read/write functions for it to work. There are examples of 
this in both `replumb.browser.io` and `replumb.nodejs.io` that you can freely copy over.

## Read-eval-call options

The core of it all is `read-eval-call`, which reads, evaluates and calls back
with the evaluation result.

The first parameter is a map of configuration options, currently
supporting:

* `:verbose` will enable the evaluation logging, defaults to false.
To customize how to print, use `(set! *print-fn* (fn [& args] ...)`

* `:warning-as-error` will consider a compiler warning as error
* `:target` `:nodejs` and `:browser` supported, the latter is used if
missing
* `:init-fn!` user provided initialization function, it will be passed a
map:

        :form   ;; the form to evaluate, as data
        :ns     ;; the current namespace, as symbol
        :target ;; the current target

* `:load-fn!` will override replumb's default `cljs.js/*load-fn*`.
It rules out `:read-file-fn!`, losing any perk of using `replumb.load`
helpers. Use it if you know what you are doing and follow this
protocol:

    > Each runtime environment provides a different way to load a library.
    > Whatever function `*load-fn*` is bound to will be passed two arguments,
    > a map and a callback function: The map will have the following keys:
    >
    >     :name   - the name of the library (a symbol)
    >     :macros - modifier signaling a macros namespace load
    >     :path   - munged relative library path (a string)
    >
    > The callback cb, upon resolution, will need to pass the same map:
    >
    >     :lang       - the language, :clj or :js
    >     :source     - the source of the library (a string)
    >     :cache      - optional, if a :clj namespace has been precompiled to
    >                   :js, can give an analysis cache for faster loads.
    >     :source-map - optional, if a :clj namespace has been precompiled
    >                   to :js, can give a V3 source map JSON
    >
    > If the resource could not be resolved, the callback should be invoked with
    > nil.

* `:read-file-fn!` an asynchronous 2-arity function with signature
`[file-path src-cb]` where src-cb is itself a function `(fn [source]
...)` that needs to be called with the file content as string (`nil`
if no file is found). It is mutually exclusive with `:load-fn!` and
will be ignored in case both are present

* `:write-file-fn!` a synchronous 2-arity function with signature
`[file-path data]` that accepts a file-path and data to write.

* `:src-paths`  a vector of paths containing source files

* `:cache` - a map containing two optional values: the first, `:path`, indicates
the path of the cached files. The second, `:src-paths-lookup?`, indicates
whether look for cached files in `:src-paths`. If both present, `:path` will
have the priority but both will be inspected.

* `:no-pr-str-on-value`  in case of `:success?` avoid converting the return
 `:value` to string

* `:context` - indicates the evaluation context that will be passed to
`cljs/eval-str`. Defaults to `:expr`.

* `:foreign-libs` - a way to include foreign libraries. The format is analogous
to the compiler option. For more info visit https://github.com/clojure/clojurescript/wiki/Compiler-Options#foreign-libs

The second parameter, `callback`, should be a 1-arity function which receives
the `result` map, whose result keys will be:


    {:success?  a boolean indicating if everything went alright
     :value     (if (:success? result)), this key contains the yielded value as
                string, unless :no-pr-str-on-value is true, in which case it
                returns the bare value.
     :error     (if-not (:success? result)) will contain a js/Error
     :warning   in case a warning was thrown and :warning-as-error is falsey
     :form      the evaluated form as data structure (not string)}

The third parameter is the source string to be read and evaluated.

It initializes the repl harness either on first execution or if an option in
`#{:src-paths :init-fn!}` changes from the previous `read-eval-call`.

See the [```browser-repl```](https://github.com/ScalaConsultants/replumb/blob/master/repl-demo/browser/cljs/replumb_repl/console.cljs)
for an actual implementation using [```jq-console```](https://github.com/replit/jq-console).


## Node.js

Support is provided, but only `:optimizations :none` works fine at the moment:

```clojure
(replumb/read-eval-call
  (replumb/nodejs-options src-paths node-read-file!)
  (fn [res]
    (-> res
        replumb/result->string true
        println)
    (.setPrompt rl (replumb/get-prompt))
    (.prompt rl))
  cmd)
```

Where `node-read-file!` is the user-provided node implementation for
`:read-file-fn!`.

See `replumb.core/nodejs-options` documentation and feel free to reuse code in
`src/node/replumb/nodejs/io.cljs`. Moreover `repl-demo/node` contains a working
example that can be built and executed with ```lein node-repl```.

You can also watch [Mike Fikes](https://www.youtube.com/watch?v=VwARsqTRw7s)'
demo or peek under the crook of his [elbow](https://github.com/mfikes/elbow).

## Design

The implementation was designed not to conceal ClojureScript's ```cljs.js/eval``` quirks and idiosyncrasies. Therefore tricks like Mike Fikes' ["Messing with macros at the REPL"](http://blog.fikesfarm.com/posts/2015-09-07-messing-with-macros-at-the-repl.html) are still part of the game and actually implemented as tests.

This will in the long run be useful for both ```replumb``` and the ```ClojureScript``` community as it will help in finding and fix issues.

Another challenge faced during the development was about the asynchronous evaluation response: we were torn between keeping the callback model as per ```cljs.js``` or adopting the novel and cutting-edge channel model of ```core.async```. We did not see any strong evidence for adding channels here, therefore avoiding inflating the artifact size and delegating the choice to client code.

This project adheres to the [SemVer](http://semver.org/) specification.

### Documentation

The documentation referring to the latest version (`SNAPSHOT` or release) can always be found in the ```doc``` folder of this repo or generated through `lein codox`.

The [wiki](https://github.com/ScalaConsultants/replumb/wiki) is also good (growing) source of info.

### Contributions

Contributions are welcome, any of them. Have a look at ```CONTRIBUTING.md``` for details.

### Community

Tell us about your amazing Replumb project!

| Author               | Link | Repo
|---------------------------|------|------|
| [@joakin](https://github.com/joakin) | [Web repl + notebook](http://chimeces.com/cljs-browser-repl/) | [cljs-browser-repl](https://github.com/joakin/cljs-browser-repl) |
| [@jaredly](https://github.com/jaredly) | [Repl w/ autocomplete, parinfer, and more](http://jaredforsyth.com/reepl/) | [Reepl](https://github.com/jaredly/reepl) |
| [@milt](https://github.com/milt) | [Simple Emacs-flavored web repl](http://yetanalytics.github.io/re-pl/) | [re-pl](https://github.com/yetanalytics/re-pl) |

### License

Distributed under the Eclipse Public License, the same as Clojure.

Copyright (C) 2015-16 Scalac Sp. z o.o.

Scalac [scalac.io](http://scalac.io/?utm_source=scalac_github&utm_campaign=scalac1&utm_medium=web) is a full-stack team of great functional developers focused around Scala/Clojure backed by great frontend and mobile developers.

On our [blog](http://blog.scalac.io/?utm_source=scalac_github&utm_campaign=scalac1&utm_medium=web) we share our knowledge with community on how to write great, clean code, how to work remotely and about our culture.
