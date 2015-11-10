# Contributing

## replumb-repl

A very simple reagent app has been included in ```repl-demo``` and its generated code will reside in ```dev-resources/public```.

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

## Issue reporting

* Check that the issue has not already been reported (check the closed ones as well).
* Open an issue with a descriptive title and a summary in concise and precise terms.
* Include any relevant code to the issue summary (contributors can create a test case from it).

## Pull Requests

* Open an issue for discussing the feature/fix you want to contribute in order to minimize the chances that your effort will be wasted (maybe somebody else is working on the same thing).
* If you are new to GitHub, read [this nice blog post](http://gun.io/blog/how-to-github-fork-branch-and-pull-request) about the common vocabulary of open source projects on Github.
* Fork the repository and create and use a topic branch off of the master branch.
* When the topic is ready, open a [pull request](https://help.github.com/articles/using-pull-requests) that relates to *only* one subject.
* After opening a PR and throughout the review phase, append commits to the topic branch but don't rewrite the history with rebase. It is ok to fixup, but leave there `!fixup` commit message, without squashing. This will ease code reviewing.
* Once the pull request is reviewed and accepted, you can squash related commits together, if necessary.
* If test and continuos integration are in place, wait for completion and check the test results. If all green, the maintainer will be able to merge your work.

You can take a look at [Understanding the GitHub Flow](https://guides.github.com/introduction/flow) for a visual version of the workflow.

### Thanks

Thanks to the domic [Bruce Hauman](https://github.com/bhauman) for Figwheel (and ```devcards```) and to [Sebastian Bensusan](https://github.com/bensu) for doo.
