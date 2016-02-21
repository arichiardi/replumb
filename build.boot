(set-env!
 :dependencies '[;; Boot deps
                 [adzerk/boot-cljs            "1.7.228-1" :scope "test"]
                 [pandeiro/boot-http          "0.7.2"     :scope "test"]
                 [adzerk/boot-reload          "0.4.4"     :scope "test"]
                 [degree9/boot-semver         "1.2.4"     :scope "test"]

                 ;; Repl
                 [adzerk/boot-cljs-repl       "0.3.0"  :scope "test"]
                 [com.cemerick/piggieback     "0.2.1"  :scope "test"]
                 [weasel                      "0.7.0"  :scope "test"]
                 [org.clojure/tools.nrepl     "0.2.12" :scope "test"]

                 ;; Tests
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]
                 [adzerk/boot-test            "1.0.7"          :scope "test"]
                 [doo                         "0.1.7-SNAPSHOT" :scope "test"]

                 ;; App deps
                 [org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/tools.reader "1.0.0-alpha3"]
                 [com.cognitect/transit-cljs "0.8.220"]])

(require '[adzerk.boot-cljs             :refer [cljs]]
         '[adzerk.boot-reload           :refer [reload]]
         '[pandeiro.boot-http           :refer [serve]]
         '[crisptrutski.boot-cljs-test  :refer [test-cljs]]
         '[adzerk.boot-cljs-repl        :refer [cljs-repl start-repl]]
         '[boot-semver.core :refer :all])

(def +version+ "0.1.5-SNAPSHOT" #_(get-version))

(task-options! pom {:project "replumb"
                    :version +version+}
               test-cljs {:js-env :phantom
                          :out-file "phantom-tests.js"})

;;;;;;;;;;;;;;;;;;;;;;
;;;    Options     ;;;
;;;;;;;;;;;;;;;;;;;;;;

(def dev-compiler-options
  {:source-map-timestamp true
   :static-fns true})

(def prod-compiler-options
  {:closure-defines {"goog.DEBUG" false}
   :optimize-constants true
   :static-fns true
   :elide-asserts true
   :pretty-print false
   :source-map-timestamp true})

(def test-namespaces
  #{"replumb.core-test"
    "replumb.repl-test"
    "replumb.common-test"
    "replumb.load-test"
    "replumb.options-test"
    "replumb.require-browser-test"
    "replumb.source-browser-test"})

(defmulti options
  "Return the correct option map for the build, dispatching on identity"
  identity)

(defmethod options :dev
  [selection]
  {:type :dev
   :env {:source-paths #{"src/clj" "src/cljs"}}
   :cljs {:source-map true
          :optimizations :none
          :compiler-options dev-compiler-options}
   :test-cljs {:optimizations :none
               :cljs-opts dev-compiler-options
               :namespaces test-namespaces}})

(defmethod options :test
  [selection]
  {:type :dev
   :env {:source-paths #{"src/clj" "src/cljs" "src/browser" "test/cljs" "test/browser"}}
   :cljs {:source-map true
          :optimizations :none
          :compiler-options dev-compiler-options}
   :test-cljs {:optimizations :none
               :cljs-opts dev-compiler-options
               :namespaces test-namespaces}})

(defmethod options :prod
  [selection]
  {:type :prod
   :env {:source-paths #{"src/clj" "src/cljs"}
         :resource-paths #{"resources/public/"}}
   :cljs {:source-map true
          :optimizations :simple
          :compiler-options prod-compiler-options}
   :test-cljs {:optimizations :simple
               :cljs-opts prod-compiler-options
               :namespaces test-namespaces}})

(deftask version-file
  "A task that includes the version.properties file in the fileset."
  []
  (with-pre-wrap [fileset]
    (boot.util/info "Add version.properties...\n")
    (-> fileset
        (add-resource (java.io.File. ".") :include #{#"^version\.properties$"})
        commit!)))

(deftask build
  "Build the final artifact, if no type is passed in, it builds production."
  [t type VAL kw "The build type, either prod or dev"]
  (let [options (options (or type :prod))]
    (boot.util/info "Building %s profile...\n" (:type options))
    (apply set-env! (reduce #(into %2 %1) [] (:env options)))
    (comp (version-file)
          (apply cljs (reduce #(into %2 %1) [] (:cljs options)))
          (target))))

(deftask dev
  "Start the dev interactive environment."
  []
  (boot.util/info "Starting interactive dev...\n")
  (let [options (options :dev)]
    (apply set-env! (reduce #(into %2 %1) [] (:env options)))
    (comp (version-file)
          (serve)
          (watch)
          (cljs-repl)
          (reload :on-jsload 'cljs-repl-web.core/main)
          (apply cljs (reduce #(into %2 %1) [] (:cljs options))))))

;; This prevents a name collision WARNING between the test task and
;; clojure.core/test, a function that nobody really uses or cares
;; about.
(ns-unmap 'boot.user 'test)

(deftask test
  "Run tests, if no type is passed in, it tests against the production build."
  [t type VAL kw "The build type, either prod or dev"]
  (let [options (options (or type :test))]
    (boot.util/info "Testing %s profile...\n" (:type options))
    (apply set-env! (reduce #(into %2 %1) [] (update-in (:env options) [:source-paths] conj "test/cljs")))
    (comp (apply test-cljs (reduce #(into %2 %1) [] (:test-cljs options)))
          (target))))

(deftask auto-test
  "Run tests while updating on file change.

  Always runs against :dev.

  It automatically enables test sound notifications, use the -n parameter for
  switching them off."
  [n no-sounds bool "Enable notifications during tests"]
  (comp (watch)
        (if no-sounds identity (speak))
        (test :type :dev)))
