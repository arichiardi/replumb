(defproject replumb "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [cljsjs/jqconsole "2.12.0-0"]
                 [reagent "0.5.1"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-codox "0.9.0"]]

  ;; :figwheel {:repl false}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "resources/private/test/compiled" "target"]
  :source-paths ["src/clj"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "repl-demo/cljs" "test/cljs"]
                        :figwheel {:on-jsload "launcher.test/run"
                                   :css-dirs ["resources/public/styles"]}
                        :compiler {:main replumb-repl.core
                                   :output-to "resources/public/js/compiled/replumb-repl.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :optimizations :none
                                   :source-map-timestamp true}}
                       {:id "test"
                        :source-paths ["src/cljs" "test/cljs" "test/doo"]
                        :compiler {:main launcher.runner
                                   :output-to "resources/private/test/compiled/replumb-repl.js"
                                   :pretty-print false}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler { ;; :main cljs-browser-repl.core ;; https://github.com/emezeske/lein-cljsbuild/issues/420
                                   :output-to "resources/public/js/compiled/replumb-repl.js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :externs ["resources/replumb.ext.js"]}}]}

  :codox {:language :clojurescript
          :source-paths ["src/cljs"]
          :namespaces [replumb.core]
          :output-path "doc"
          :metadata {:doc/format :markdown}}

  :aliases {"fig-dev" ^{:doc "Start figwheel with dev profile."} ["figwheel" "dev"]
            "fig-dev*" ^{:doc "Clean and start figwheel with dev profile"} ["do" "clean" ["figwheel" "dev"]]
            "minify" ^{:doc "Clean and compile sources minified for production."} ["do" "clean" ["cljsbuild" "once" "min"]]
            "deploy" ^{:doc "Clean, compile (minified) sources, test and then deploy."} ["do" "clean" ["test" ":integration"] ["deploy" "clojars"]]
            "test-phantom" ^{:doc "Execute once unit tests with PhantomJS (must be installed)."} ["doo" "phantom" "test" "once"]
            "test-phantom*" ^{:doc "Clean and execute once unit tests with PhantomJS (must be installed)."} ["do" "clean" ["doo" "phantom" "test" "once"]]
            "auto-phantom" ^{:doc "Clean and execute automatic unit tests with PhantomJS (must be installed)."} ["do" "clean" ["doo" "phantom" "test" "auto"]]
            "test-slimer" ^{:doc "Execute once unit tests with SlimerJS (must be installed)."} ["doo" "slimer" "test" "once"]
            "test-slimer*" ^{:doc "Clean and execute once unit tests with SlimerJS (must be installed)."} ["do" "clean" ["doo" "slimer" "test" "once"]]
            "auto-slimer" ^{:doc "Clean and execute automatic unit tests with SlimerJS (must be installed)."} ["do" "clean" ["doo" "slimer" "test" "auto"]]
            "tests" ^{:doc "Execute once unit tests with PhantomJS and SlimerJS (must be installed)."} ["doo" "headless" "test" "once"]
            "tests*" ^{:doc "Clean and execute once unit tests with PhantomJS and SlimerJS (must be installed)."} ["do" "clean" ["doo" "headless" "test" "once"]]}

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.5"]
                                  [org.clojure/tools.nrepl "0.2.11"]]
                   :plugins [[lein-doo "0.1.6-SNAPSHOT"]
                             [lein-figwheel "0.4.1" :exclusions [cider/cider-nrepl]]]}})
