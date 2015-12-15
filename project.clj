(defproject replumb/replumb "0.1.3-SNAPSHOT"
  :description "ClojureScript plumbing for your bootstrapped REPLs."
  :url "https://github.com/clojurex/replumb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/tools.reader "1.0.0-alpha1"]]

  :plugins [[lein-cljsbuild "1.1.1"]
            [lein-codox "0.9.0"]]

  :clean-targets ^{:protect false} ["dev-resources/public/js/compiled" "dev-resources/private/test/browser/compiled"
                                    "dev-resources/private/test/node/compiled" "dev-resources/private/node"
                                    "out" :target-path]
  :source-paths ["src/cljs"]
  :hooks [leiningen.cljsbuild]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "src/browser" "repl-demo/browser/cljs"]
                        :figwheel {:on-jsload "replumb-repl.core/main"
                                   :css-dirs ["dev-resources/public/styles"]}
                        :compiler {:main replumb-repl.core
                                   :optimizations :none
                                   :output-to "dev-resources/public/js/compiled/replumb-repl.js"
                                   :output-dir "dev-resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true
                                   :static-fns true}}
                       {:id "browser-test"
                        :source-paths ["src/cljs" "test/cljs" "test/browser" "test/doo"]
                        :compiler {:optimizations :none
                                   :main launcher.runner
                                   :output-to "dev-resources/private/test/browser/compiled/browser-test.js"
                                   :output-dir "dev-resources/private/test/browser/compiled/out"
                                   :asset-path "dev-resources/private/test/browser/compiled/out"
                                   :static-fns true}}
                       {:id "node-test"
                        :source-paths ["src/cljs" "src/node" "test/cljs" "test/node" "test/doo"]
                        :compiler {:target :nodejs
                                   :optimizations :none
                                   :main launcher.runner
                                   :output-to "dev-resources/private/test/node/compiled/nodejs-test.js"
                                   :output-dir "dev-resources/private/test/node/compiled/out"
                                   :asset-path "dev-resources/private/test/node/compiled/out"
                                   :static-fns true}}
                       {:id "node-repl"
                        :source-paths ["src/cljs" "src/node" "repl-demo/node/cljs"]
                        :compiler {:target :nodejs
                                   :optimizations :none
                                   :main nodejs-repl.core
                                   :output-to "dev-resources/private/node/compiled/nodejs-repl.js"
                                   :output-dir "dev-resources/private/node/compiled/out"
                                   :asset-path "dev-resources/private/node/compiled/out"
                                   :static-fns true}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler { ;; :main cljs-browser-repl.core ;; https://github.com/emezeske/lein-cljsbuild/issues/420
                                   :output-to "dev-resources/public/js/compiled/replumb-repl.js"
                                   :optimizations :simple
                                   :pretty-print false
                                   :elide-asserts true
                                   :static-fns true}}]}
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
            "node-repl" ^{:doc "Clean, build and launch the node demo repl. Node.js (must be installed)."} ["do" "clean" ["cljsbuild" "once" "node-repl"] ["shell" "scripts/node-repl.sh"]]
            "test-phantom" ^{:doc "Execute once unit tests with PhantomJS (must be installed)."} ["doo" "phantom" "browser-test" "once"]
            "test-phantom*" ^{:doc "Clean and execute once unit tests with PhantomJS (must be installed)."} ["do" "clean" ["doo" "phantom" "browser-test" "once"]]
            "auto-phantom" ^{:doc "Clean and execute automatic unit tests with PhantomJS (must be installed)."} ["do" "clean" ["doo" "phantom" "browser-test" "auto"]]
            "test-slimer" ^{:doc "Execute once unit tests with SlimerJS (must be installed)."} ["doo" "slimer" "browser-test" "once"]
            "test-slimer*" ^{:doc "Clean and execute once unit tests with SlimerJS (must be installed)."} ["do" "clean" ["doo" "slimer" "browser-test" "once"]]
            "auto-slimer" ^{:doc "Clean and execute automatic unit tests with SlimerJS (must be installed)."} ["do" "clean" ["doo" "slimer" "browser-test" "auto"]]
            "test-node" ^{:doc "Execute once unit tests with Node.js (must be installed)."} ["doo" "node" "node-test" "once"]
            "test-node*" ^{:doc "Clean and execute once unit tests with Node.js (must be installed)."} ["do" "clean" ["doo" "node" "node-test" "once"]]
            "auto-node" ^{:doc "Clean and execute automatic unit tests with Node.js (must be installed)."} ["do" "clean" ["doo" "node" "node-test" "auto"]]
            "tests" ^{:doc "Execute once unit tests with PhantomJS and SlimerJS (must be installed)."} ["doo" "headless" "browser-test" "once"]
            "tests*" ^{:doc "Clean and execute once unit tests with PhantomJS and SlimerJS (must be installed)."} ["do" "clean" ["doo" "headless" "browser-test" "once"]]}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :signing {:gpg-key "clojars@scalac.io"}
                                     :creds :gpg}]]

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.5"]
                                  [org.clojure/tools.nrepl "0.2.11"]
                                  [cljsjs/jqconsole "2.13.1-0"]
                                  [reagent "0.5.1"]]
                   :plugins [[lein-doo "0.1.7-SNAPSHOT"]
                             [lein-figwheel "0.5.0-2" :exclusions [cider/cider-nrepl]]
                             [lein-shell "0.4.2"]]}})
