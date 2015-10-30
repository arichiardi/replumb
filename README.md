# replumb 


Replum (*'rɛplʌm*) is a plumbing library for your [bootstrapped](https://en.m.wikipedia.org/wiki/Bootstrapping_%28compilers%29) ClojureScript Read-Eval-Print-Loops.

This library tries to ease the burden of talking direcly to ClojureScript ```cljs.js``` and puts together [pieces](https://github.com/kanaka/cljs-bootstrap) [of](https://github.com/mfikes/planck) [work](https://github.com/mfikes/replete) contributed by the ClojureScript community at disparate points in time. It aspires to be a common starting point for  custom REPLs and/or REPL-ish apps (educational interactive environments for instance).

## Usage

__Leiningen__ ([via Clojars](https://clojars.org/replumb))

Put the following into the `:dependencies` vector.

[![Clojars Project](http://clojars.org/replumb/latest-version.svg)](http://clojars.org/replumb)

Then in your code, call directly ```replumb.core```:

``` clojure
(defn handle-result!
  [console result]
  (let [write-fn (if (replumb/success? result) console/write-return! console/write-exception!)]
    (write-fn console (replumb/unwrap-result result))))

(defn cljs-read-eval-print!
  [console user-input]
  (replumb/read-eval-call (partial handle-result! console) user-input))
```

See ```repl-demo``` for an actual implementation using [```jq-console```](https://github.com/replit/jq-console).

## Design

The implementation was designed not to conceal ClojureScript's ```cljs.js/eval``` quirks and idiosyncrasies. Therefore tricks like Mike Fikes' ["Messing with macros at the REPL"](http://blog.fikesfarm.com/posts/2015-09-07-messing-with-macros-at-the-repl.html) are still part of the game and actually implemented as tests.

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
