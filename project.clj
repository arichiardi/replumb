(defproject replumb/replumb "0.2.0-SNAPSHOT"
  :description "ClojureScript plumbing for your bootstrapped REPLs."
  :url "https://github.com/Lambda-X/replumb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/tools.reader "1.0.0-alpha3"]
                 [com.cognitect/transit-cljs "0.8.220"]]

  :plugins [[lein-cljsbuild "1.1.2"]
            [lein-codox "0.9.0"]]

  :clean-targets ^{:protect false} ["dev-resources/public/js/compiled" ;; dev
                                    "dev-resources/private/browser/js/compiled" ;; browser-repl
                                    "dev-resources/private/browser/js/simple/compiled" ;; browser-repl-simple
                                    "dev-resources/private/node/js/compiled" ;; node-repl
                                    "dev-resources/private/test/browser/compiled" ;; browser-test, browser-simple-test
                                    "dev-resources/private/test/node/compiled" ;; node-test, node-simple-test
                                    "dev-resources/public/js/compiled" ;; min
                                    "out" :target-path]
  :hooks [leiningen.cljsbuild]
  :min-lein-version "2.0.0"
  :jvm-opts ^:replace ["-XX:+TieredCompilation" "-XX:TieredStopAtLevel=1" "-Xverify:none"]
  :source-paths ["src/cljs"]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "src/browser" "repl-demo/browser/cljs"]
                        :figwheel {:on-jsload "replumb-repl.core/main"}
                        :compiler {:main replumb-repl.core
                                   :optimizations :none
                                   :output-to "dev-resources/public/js/compiled/replumb-repl.js"
                                   :output-dir "dev-resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true
                                   :static-fns true}}
                       {:id "browser-repl"
                        :source-paths ["src/cljs" "src/browser" "repl-demo/browser/cljs"]
                        :compiler {:optimizations :none
                                   :main replumb-repl.core
                                   :output-to "dev-resources/private/browser/js/compiled/replumb-repl.js"
                                   :output-dir "dev-resources/private/browser/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true
                                   :static-fns true
                                   :parallel-build true}}
                       {:id "browser-repl-simple"
                        :source-paths ["src/cljs" "src/browser" "repl-demo/browser/cljs"]
                        :compiler {:optimizations :simple
                                   :main replumb-repl.core
                                   :output-to "dev-resources/private/browser/js/simple/compiled/replumb-repl.js"
                                   :output-dir "dev-resources/private/browser/js/simple/compiled/out"
                                   :asset-path "js/simple/compiled/out"
                                   :source-map "dev-resources/private/browser/js/simple/compiled/replumb-repl.js.map"
                                   :source-map-timestamp true
                                   :static-fns true
                                   :parallel-build true}}
                       {:id "node-repl"
                        :source-paths ["src/cljs" "src/node" "repl-demo/node/cljs"]
                        :compiler {:target :nodejs
                                   :optimizations :none
                                   :main nodejs-repl.core
                                   :output-to "dev-resources/private/node/js/compiled/nodejs-repl.js"
                                   :output-dir "dev-resources/private/node/js/compiled/out"
                                   :asset-path "dev-resources/private/node/js/compiled/out"
                                   :static-fns true
                                   :parallel-build true}}
                       {:id "browser-test"
                        :source-paths ["src/cljs" "test/cljs" "test/browser"]
                        :compiler {:optimizations :none
                                   :main launcher.runner
                                   :output-to "dev-resources/private/test/browser/compiled/browser-test.js"
                                   :output-dir "dev-resources/private/test/browser/compiled/out"
                                   :asset-path "dev-resources/private/test/browser/compiled/out"
                                   :static-fns true
                                   :parallel-build true}}
                       #_{:id "browser-test-simple"
                          :source-paths ["src/cljs" "test/cljs" "test/browser"]
                          :compiler {:optimizations :simple
                                     :main launcher.runner
                                     :output-to "dev-resources/private/test/browser/compiled/browser-test.js"
                                     :output-dir "dev-resources/private/test/browser/compiled/out"
                                     :asset-path "dev-resources/private/test/browser/compiled/out"
                                     :static-fns true
                                     :parallel-build true}}
                       {:id "node-test"
                        :source-paths ["src/cljs" "src/node" "test/cljs" "test/clj" "test/node"]
                        :compiler {:target :nodejs
                                   :optimizations :none
                                   :main launcher.runner
                                   :output-to "dev-resources/private/test/node/compiled/nodejs-test.js"
                                   :output-dir "dev-resources/private/test/node/compiled/out"
                                   :asset-path "dev-resources/private/test/node/compiled/out"
                                   :static-fns true
                                   :parallel-build true}}
                       #_{:id "node-test-simple"
                          :source-paths ["src/cljs" "src/node" "test/cljs" "test/node"]
                          :compiler {:target :nodejs
                                     :optimizations :simple
                                     :main launcher.runner
                                     :output-to "dev-resources/private/test/node/compiled/nodejs-test.js"
                                     :output-dir "dev-resources/private/test/node/compiled/out"
                                     :asset-path "dev-resources/private/test/node/compiled/out"
                                     :static-fns true
                                     :parallel-build true}}
                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler { ;; :main cljs-browser-repl.core ;; https://github.com/emezeske/lein-cljsbuild/issues/420
                                   :output-to "dev-resources/public/js/compiled/replumb-repl.js"
                                   :optimizations :simple
                                   :pretty-print false
                                   :elide-asserts true
                                   :static-fns true
                                   :parallel-build true}}]}

  :figwheel {:css-dirs ["dev-resources/public/styles"]
             :open-file-command "open-emacs"
             :nrepl-port 5088}

  :prep-tasks [] ;; or cljsbuild will start compiling everything
  :codox {:language :clojurescript
          :source-paths ["src/cljs"]
          :namespaces [replumb.core]
          :output-path "doc"
          :metadata {:doc/format :markdown}}

  :aliases {"fig-dev" ^{:doc "Start figwheel with dev profile."} ["figwheel" "dev"]
            "fig-dev*" ^{:doc "Clean and start figwheel with dev profile"} ["do" "clean" ["figwheel" "dev"]]

            "node-repl" ^{:doc "Clean, build and launch the node demo repl. Node.js must be installed."} ["do" "clean" ["cljsbuild" "once" "node-repl"] ["shell" "scripts/node-repl.sh"]]
            "browser-repl" ^{:doc "Clean, build and launch the browser demo repl."} ["do" "clean" ["cljsbuild" "once" "browser-repl"] ["shell" "scripts/browser-repl.sh"]]
            "browser-repl-simple" ^{:doc "Clean, build and launch the browser demo repl."} ["do" "clean" ["cljsbuild" "once" "browser-repl-simple"] ["shell" "scripts/browser-repl.sh"]]

            "minify" ^{:doc "Compile sources minified for production."} ["cljsbuild" "once" "min"]
            "minify*" ^{:doc "Clean and compile sources minified for production."} ["do" "clean" ["cljsbuild" "once" "min"]]

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

  :profiles {:dev {:resource-paths ["dev-resources"]
                   :source-paths ["src/cljs" "src/clj" "test/clj" "test/cljs" "test/browser" "src/browser" "repl-demo/browser/cljs" "dev"]
                   :dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.12"]
                                  [figwheel-sidecar "0.5.0-6"]
                                  [cljsjs/jqconsole "2.13.2-0"]
                                  [reagent "0.5.1"]
                                  [binaryage/devtools "0.5.2"]
                                  [spellhouse/clairvoyant "0.0-72-g15e1e44"]]
                   :plugins [[lein-doo "0.1.7-SNAPSHOT"]
                             [lein-figwheel "0.5.0-6" :exclusions [cider/cider-nrepl]]
                             [lein-shell "0.4.2"]]}
             :repl {:plugins [[cider/cider-nrepl "0.11.0-SNAPSHOT"]]
                    :repl-options {:nrepl-middleware [#_cider.nrepl/cider-middleware
                                                      cemerick.piggieback/wrap-cljs-repl]}}})
