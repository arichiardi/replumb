(ns ^:figwheel-load replumb.load-test
  (:require [cljs.test :refer-macros [deftest is]]
            [clojure.string :as string]
            [replumb.load :refer [filenames-to-try]]))

(deftest files
  (is (empty? (filenames-to-try [] true "temp")) "No files should be tried with empty source path and if :macros true")
  (is (empty? (filenames-to-try [] false "temp")) "No files should be tried with empty source path and if :macros false")
  (let [src-paths ["src1/foo" "src2/" "src3/foo/bar"]
        file "core"
        cljs-files (map #(str % (when-not (= "/" (last %)) "/") file ".cljs") src-paths)
        cljc-files (map #(str % (when-not (= "/" (last %)) "/") file ".cljc") src-paths)
        clj-files (map #(str % (when-not (= "/" (last %)) "/") file ".clj") src-paths)
        js-files (map #(str % (when-not (= "/" (last %)) "/") file ".js") src-paths)]
    (let [files (into [] (filenames-to-try src-paths false file))]
      (is (= 9 (count files)) "With 3 paths 9 files should be tried if :macros false")
      (is (= cljs-files (subvec files 0 3)) "Try .cljs files first if :macros false")
      (is (= cljc-files (subvec files 3 6)) "Try .cljc files second if :macros false")
      (is (= js-files (subvec files 6)) "Try .js files third if :macros false"))
    (let [files (into [] (filenames-to-try src-paths true file))]
      (is (= 6 (count files)) "With 3 paths 6 files should be tried if :macros true")
      (is (= clj-files (subvec files 0 3)) "Try .clj files first if :macros true")
      (is (= cljc-files (subvec files 3)) "Try .cljc files second if :macros true"))))
