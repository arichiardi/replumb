# Contributing

## replumb-repl

A very simple reagent app has been included in ```repl-demo``` and its generated code will reside in ```dev-resources/public```.

In order to contribute and take full advantage of the Figwheel workflow, you first need to build a local copy of ```cljsjs/jqconsole``` (the patch has not been yet merged to ```cljsjs``` master).

The steps to generate it are:

* ```git clone -b cljsjs-jqconsole https://github.com/arichiardi/packages```
* ```cd packages/jqconsole```
* ```boot package build-jar```

[Boot](https://github.com/boot-clj/boot#install) must be installed on the machine.

#### Figwheel:

This project uses Figwheel, as many do. After the step above you can use the classic ```lein figwheel dev``` or use the local alias ```lein fig-dev```

*Note: There are a lot of other alias, ```lein help``` is your friend.*

If you want to clean and then run Figwheel, use ```lein fig-dev*``` (alias with asterisk means that they ```lein clean``` first).

Wait a bit, then browse to [http://localhost:3449](http://localhost:3449). Figwheel will automatically push cljs changes to the browser.


## Production Build

Useful for seeing warnings in ```{:optimizations :advanced}``` mode: ```lein minify```.

## Testing

Tests work in the Firefox/Chrome Developer Console, with PhanthomJS and SlimerJS.

The former is easier to check: after having booted Figwheel you have to open the Developer Console and run ```luncher.test.run()```. Moreover, tests are executed every time Figwheel reloads.

For headless testing, first you need to install [PhantomJS](https://github.com/ariya/phantomjs/) and/or [SlimerJS](http://slimerjs.org/). Afterwards you can: ```lein test-phantom``` and/or ```lein test-slimer``` respectively.
Run ```lein auto-slimer``` or ```lein auto-phantom``` for reloading tests while developing code.

Featuring [doo](https://github.com/bensu/doo).

## Docs

The documentation tool of choice is [Codox](https://github.com/weavejester/codox). You just need to execute `lein codox` and open `doc/index.html` in order to see the result.

### Thanks

Thanks to the domic [Bruce Hauman](https://github.com/bhauman) for Figwheel (and ```devcards```) and to [Sebastian Bensusan](https://github.com/bensu) for doo.
