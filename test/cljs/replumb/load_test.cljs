(ns replumb.load-test
  (:require [cljs.test :refer-macros [deftest is]]
            [replumb.load :refer [file-paths-for-load-fn file-paths]]))

(deftest paths-without-ext
  (is (empty? (file-paths-for-load-fn [] true "temp")) "If empty src-paths and :macros true, file-paths-for-load-fn should return empty vector")
  (is (empty? (file-paths-for-load-fn [] false "temp")) "If empty src-paths and :macros false, file-paths-for-load-fn should return empty vector")

  (let [src-paths ["src1/foo" "src2/" "src3/foo/bar"]
        file "core"
        cljs-files (map #(str % (when-not (= "/" (last %)) "/") file ".cljs") src-paths)
        cljc-files (map #(str % (when-not (= "/" (last %)) "/") file ".cljc") src-paths)
        clj-files (map #(str % (when-not (= "/" (last %)) "/") file ".clj") src-paths)
        js-files (map #(str % (when-not (= "/" (last %)) "/") file ".js") src-paths)]
    (let [files (into [] (file-paths-for-load-fn src-paths false file))]
      (is (= 9 (count files)) "With 3 paths and :macros false, file-paths-for-load-fn should try 9 files")
      (is (= cljs-files (subvec files 0 3)) "When :macros false, file-paths-for-load-fn should try .cljs files first")
      (is (= cljc-files (subvec files 3 6)) "When :macros false, file-paths-for-load-fn should try .cljc files second")
      (is (= js-files (subvec files 6)) "When :macros false, file-paths-for-load-fn should try .js files third"))

    (let [files (into [] (file-paths-for-load-fn src-paths true file))]
      (is (= 6 (count files)) "With 3 paths and :macros true, file-paths-for-load-fn should try 6 files")
      (is (= clj-files (subvec files 0 3)) "When :macros true, file-paths-for-load-fn should try .clj files first")
      (is (= cljc-files (subvec files 3)) "When :macros true, file-paths-for-load-fn should try .cljc files second"))))

(deftest paths-with-ext
  (is (empty? (file-paths [] "temp.clj")) "If src-paths is empty, file-paths-for-load-fn should return empty vector")

  (let [src-paths ["src1/foo" "src2/" "src3/foo/bar"]
        file "baz.clj"
        expected-path (map #(str % (when-not (= "/" (last %)) "/") file) src-paths)
        files (into [] (file-paths src-paths file))]
    (is (= expected-path files) "The fn file-paths (remember it does not add extension), should return the expected 3 files")))
