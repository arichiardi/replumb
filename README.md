# replumb

ClojureScript plumbing for your bootstrapped REPLs.

This library tries to ease the burden of talking direcly to ClojureScript ```cljs.js``` and puts together [pieces](https://github.com/kanaka/cljs-bootstrap) [of](https://github.com/mfikes/planck) [work](https://github.com/mfikes/replete) contributed by the ClojureScript community at this points in time. It aspires to be a common starting point for  custom REPLs and/or REPL-ish apps.

## Contributions

Controbutions are welcome, especially about testing and issue tracking.

#### Figwheel:

This project *TANRTN ```lein fig-dev```  **or** ```lein fig-dev*``` if you want to clean as well.

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449).

## Production Build

```lein minify``` **or** ```lein do clean, cljsbuild once min```

## Testing

Tests at the moment complete smoothly in the Firefox/Chrome Developer Console but not with PhanthomJS or SlimerJS.
The weird returned error form requiring or ns-ing a namespace is:

``` clojure
#error {:message "ERROR", :data {:tag :cljs/analysis-error}, :cause #object[Error Error: Namespace "first.namespace" already declared.]}
```

The former is easier to check: after having booted Figwheel you have to open the Developer Console and run ```luncher.test.run()```. Moreover, tests are executed every time Figwheel reloads.

For improving on the latter, you need first of all [PhantomJS](https://github.com/ariya/phantomjs/) and/or [SlimerJS](http://slimerjs.org/), after which you can: ```lein test-phantom``` and/or ```lein test-slimer``` respectively. Featuring [doo](https://github.com/bensu/doo) here.

## Docs

The documentation tool of choice is [Codox](https://github.com/weavejester/codox). You just need to execute `lein codox` and open `doc/index.html` in order to see the result.

## Resources

 * [JQConsole](https://github.com/replit/jq-console)
 * [CLJSJS](https://github.com/cljsjs/packages)
