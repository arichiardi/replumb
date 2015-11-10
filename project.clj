(defproject replumb/replumb "0.1.1-SNAPSHOT"
  :description "ClojureScript plumbing for your bootstrapped REPLs."
  :url "https://github.com/clojurex/replumb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]]

  :plugins [[lein-cljsbuild "1.1.0"]
            [lein-codox "0.9.0"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "resources/private/test/compiled" :target-path]
  :source-paths ["src/cljs"]
  :hooks [leiningen.cljsbuild]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "repl-demo/cljs" "test/cljs"]
                        :figwheel {:on-jsload "launcher.test/run"
                                   :css-dirs ["resources/public/styles"]}
                        :compiler {:main replumb-repl.core
                                   :output-to "dev-resources/public/js/compiled/replumb-repl.js"
                                   :output-dir "dev-resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :optimizations :none
                                   :source-map-timestamp true}}
                       {:id "test"
                        :source-paths ["src/cljs" "repl-demo/cljs" "test/cljs"]
                        :compiler {:output-to "dev-resources/private/test/compiled/replumb-repl.js"
                                   :optimizations :whitespace
                                   :pretty-print false}}
                       {:id "doo-test"
                        :source-paths ["src/cljs" "test/cljs" "test/doo"]
                        :compiler {:main launcher.runner
                                   :output-to "dev-resources/private/test/compiled/replumb-repl.js"
                                   :pretty-print false}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler { ;; :main cljs-browser-repl.core ;; https://github.com/emezeske/lein-cljsbuild/issues/420
                                   :output-to "dev-resources/public/js/compiled/replumb-repl.js"
                                   :optimizations :advanced
                                   :pretty-print false
                                   :elide-asserts true
                                   :externs ["resources/replumb.ext.js"]}}]}
  ;; :figwheel {:repl false}

  :prep-tasks ["codox"]
  :codox {:language :clojurescript
          :source-paths ["src/cljs"]
          :namespaces [replumb.core]
          :output-path "doc"
          :metadata {:doc/format :markdown}}

  :aliases {"fig-dev" ^{:doc "Start figwheel with dev profile."} ["figwheel" "dev"]
            "fig-dev*" ^{:doc "Clean and start figwheel with dev profile"} ["do" "clean" ["figwheel" "dev"]]
            "minify" ^{:doc "Compile sources minified for production."} ["cljsbuild" "once" "min"]
            "minify*" ^{:doc "Clean and compile sources minified for production."} ["do" "clean" ["cljsbuild" "once" "min"]]
            "test-phantom" ^{:doc "Execute once unit tests with PhantomJS (must be installed)."} ["doo" "phantom" "doo-test" "once"]
            "test-phantom*" ^{:doc "Clean and execute once unit tests with PhantomJS (must be installed)."} ["do" "clean" ["doo" "phantom" "doo-test" "once"]]
            "auto-phantom" ^{:doc "Clean and execute automatic unit tests with PhantomJS (must be installed)."} ["do" "clean" ["doo" "phantom" "doo-test" "auto"]]
            "test-slimer" ^{:doc "Execute once unit tests with SlimerJS (must be installed)."} ["doo" "slimer" "doo-test" "once"]
            "test-slimer*" ^{:doc "Clean and execute once unit tests with SlimerJS (must be installed)."} ["do" "clean" ["doo" "slimer" "doo-test" "once"]]
            "auto-slimer" ^{:doc "Clean and execute automatic unit tests with SlimerJS (must be installed)."} ["do" "clean" ["doo" "slimer" "doo-test" "auto"]]
            "tests" ^{:doc "Execute once unit tests with PhantomJS and SlimerJS (must be installed)."} ["doo" "headless" "doo-test" "once"]
            "tests*" ^{:doc "Clean and execute once unit tests with PhantomJS and SlimerJS (must be installed)."} ["do" "clean" ["doo" "headless" "doo-test" "once"]]}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :signing {:gpg-key "clojars@scalac.io"}
                                     :creds :gpg}]]

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.5"]
                                  [org.clojure/tools.nrepl "0.2.11"]
                                  [cljsjs/jqconsole "2.13.1-0"]
                                  [reagent "0.5.1"]]
                   :plugins [[lein-doo "0.1.6-SNAPSHOT"]
                             [lein-figwheel "0.4.1" :exclusions [cider/cider-nrepl]]]}})
