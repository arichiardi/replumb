(defproject replumb/replumb "0.2.5-SNAPSHOT"
  :description "ClojureScript plumbing for your bootstrapped REPLs."
  :url "https://github.com/Lambda-X/replumb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.145" :scope "test"]
                 [org.clojure/tools.reader "1.2.2" :scope "test"]
                 [com.cognitect/transit-clj "0.8.300" :scope "test"]
                 [com.cognitect/transit-cljs "0.8.243" :scope "test"]
                 [org.clojure/spec.alpha "0.1.143"]
                 [org.clojure/test.check "0.10.0-alpha2"]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-codox "0.9.0"]]

  :clean-targets ^{:protect false} ["dev-resources/public/js/compiled" ;; dev
                                    "dev-resources/private/browser/js/compiled" ;; browser-repl
                                    "dev-resources/private/browser/js/simple/compiled" ;; browser-repl-simple
                                    "dev-resources/private/node/js/compiled" ;; node-repl
                                    "dev-resources/private/node/js/simple/compiled" ;; node-repl-simple
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
                       {:id "node-repl-simple"
                        :source-paths ["src/cljs" "src/node" "repl-demo/node/cljs"]
                        :compiler {:target :nodejs
                                   :optimizations :simple
                                   :main nodejs-repl.core
                                   :output-to "dev-resources/private/node/js/simple/compiled/nodejs-repl.js"
                                   :output-dir "dev-resources/private/node/js/simple/compiled/out"
                                   :asset-path "js/simple/compiled/out"
                                   :source-map "dev-resources/private/node/js/simple/compiled/replumb-repl.js.map"
                                   :source-map-timestamp false
                                   :cache-analysis true
                                   :aot-cache true
                                   :closure-defines {'cljs.core/*target* "nodejs"}
                                   :static-fns true
                                   :compiler-stats true
                                   :process-shim false
                                   :fn-invoke-direct true
                                   :parallel-build true}}
                       {:id "browser-test"
                        :source-paths ["src/cljs" "test/common" "test/browser"]
                        :compiler {:optimizations :none
                                   :main test.runner
                                   :output-to "dev-resources/private/test/browser/compiled/browser-test.js"
                                   :output-dir "dev-resources/private/test/browser/compiled/out"
                                   :asset-path "dev-resources/private/test/browser/compiled/out"
                                   :static-fns true
                                   :parallel-build true
                                   :closure-defines {"goog.DEBUG" false}}}
                       #_{:id "browser-test-simple"
                          :source-paths ["src/cljs" "test/common" "test/browser"]
                          :compiler {:optimizations :simple
                                     :main test.runner
                                     :output-to "dev-resources/private/test/browser/compiled/browser-test.js"
                                     :output-dir "dev-resources/private/test/browser/compiled/out"
                                     :asset-path "dev-resources/private/test/browser/compiled/out"
                                     :static-fns true
                                     :parallel-build true
                                     :closure-defines {"goog.DEBUG" false}}}
                       {:id "node-test"
                        :source-paths ["src/cljs" "src/node" "test/common" "test/node"]
                        :compiler {:target :nodejs
                                   :optimizations :none
                                   :main test.runner
                                   :output-to "dev-resources/private/test/node/compiled/nodejs-test.js"
                                   :output-dir "dev-resources/private/test/node/compiled/out"
                                   :asset-path "dev-resources/private/test/node/compiled/out"
                                   :static-fns true
                                   :parallel-build true
                                   :closure-defines {"goog.DEBUG" false}}}
                       {:id "node-test-simple"
                        :source-paths ["src/cljs" "src/node" "test/common" "test/node"]
                        :compiler {:target :nodejs
                                   :optimizations :simple
                                   :main test.runner
                                   :output-to "dev-resources/private/test/node/compiled/nodejs-test.js"
                                   :output-dir "dev-resources/private/test/node/compiled/simple/out"
                                   ;; AR we trick the runtime to look at out from :none
                                   :asset-path "dev-resources/private/test/node/compiled/out"
                                   :static-fns true
                                   :parallel-build true
                                   :closure-defines {"goog.DEBUG" false}}}
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
          :namespaces [replumb.core
                       replumb.ast]
          :output-path "doc"
          :metadata {:doc/format :markdown}}

  :aliases {"repl-node" ^{:doc "Clean, build and launch the node demo repl. Node.js must be installed."} ["do" "clean" ["cljsbuild" "once" "node-repl"] ["shell" "scripts/node-repl.sh"]]
            "repl-node-simple" ^{:doc "Clean, build and launch the node demo repl. Node.js must be installed."} ["do" "clean" ["cljsbuild" "once" "node-repl-simple"] ["shell" "scripts/node-repl.sh" "--simple"]]
            "repl-browser" ^{:doc "Clean, build and launch the browser demo repl."} ["do" "clean" ["cljsbuild" "once" "browser-repl"] ["shell" "scripts/browser-repl.sh"]]
            "repl-browser-simple" ^{:doc "Clean, build and launch the browser demo repl."} ["do" "clean" ["cljsbuild" "once" "browser-repl-simple"] ["shell" "scripts/browser-repl.sh"]]

            "minify" ^{:doc "Compile sources minified for production."} ["cljsbuild" "once" "min"]

            "test-phantom" ^{:doc "Execute once unit tests with PhantomJS (must be installed)."} ["doo" "phantom" "browser-test" "once"]
            "test-slimer" ^{:doc "Execute once unit tests with SlimerJS (must be installed)."} ["doo" "slimer" "browser-test" "once"]
            "test-node" ^{:doc "Execute once unit tests with Node.js (must be installed)."} ["doo" "node" "node-test" "once"]
            "test-node-simple" ^{:doc "Clean and execute once unit tests with Node.js (must be installed)."} ["doo" "node" "node-test-simple" "once"]

            "auto-phantom" ^{:doc "Clean and execute automatic unit tests with PhantomJS (must be installed)."} ["do" "clean" ["doo" "phantom" "browser-test" "auto"]]
            "auto-slimer" ^{:doc "Clean and execute automatic unit tests with SlimerJS (must be installed)."} ["do" "clean" ["doo" "slimer" "browser-test" "auto"]]
            "auto-node" ^{:doc "Clean and execute automatic unit tests with Node.js (must be installed)."} ["do" "clean" ["doo" "node" "node-test" "auto"]]
            "auto-node-simple" ^{:doc "Clean and execute once unit tests with Node.js (must be installed)."} ["do" "clean" ["doo" "node" "node-test-simple" "auto"]]

            "tests" ^{:doc "Clean and execute all the unit tests (PhantomJS must be installed)."} ["do" "clean"
                                                                                                   ["doo" "phantom" "browser-test" "once"]
                                                                                                   ;; AR
                                                                                                   ;; In leiningen we don't have a way to generate the source files necessary for node-test-simple to work by itself
                                                                                                   ;; This feat is done by https://github.com/Lambda-X/boot-pack-source but it is boot only.
                                                                                                   ;; What happens here is that node-test generates the source for the following node-test-simple files
                                                                                                   ;;
                                                                                                   ;; WARNING: they both need to generate code in the same folder or they will break
                                                                                                   ["doo" "node" "node-test" "once"]
                                                                                                   ["doo" "node" "node-test-simple" "once"]]}

  :deploy-repositories [["releases" {:url "https://clojars.org/repo"
                                     :signing {:gpg-key "clojars@scalac.io"}
                                     :creds :gpg}]]

  :profiles {:dev {:resource-paths ["dev-resources"]
                   :source-paths ["src/cljs" "src/clj" "test/common" "test/browser" "src/browser" "repl-demo/browser/cljs" "dev"]
                   :dependencies [[cider/piggieback "0.3.5"]
                                  [figwheel-sidecar "0.5.16"]
                                  [cljsjs/jqconsole "2.13.2-0"]
                                  [reagent "0.6.0-rc"]
                                  [binaryage/devtools "0.8.1"]]
                   :plugins [[lein-doo "0.1.7"]
                             [lein-figwheel "0.5.7" :exclusions [cider/cider-nrepl]]
                             [lein-shell "0.4.2"]]}
             :repl {:repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
