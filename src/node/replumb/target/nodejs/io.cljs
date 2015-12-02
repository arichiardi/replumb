(ns replumb.target.nodejs.io
  (:require [cljs.nodejs :as nodejs]
            [replumb.load :as load]))

(def require-fs
  "Delay containing the call to \"require fs\". It returns the Node.js
  module object."
  (delay (nodejs/require "fs")))

(defn read-file!
  "Accepts the fs module object, encoding or file options, file name to
  read and a callback. Upon success, invokes the callback with the
  source of the file. Otherwise invokes the callback with nil.

  The arity without explicit module will call require the first time
  only. The arity without encoding-or-opts will default to no file
  options and encoding \"UTF-8\".

  For encoding-or-opts, see
  https://nodejs.org/api/fs.html#fs_fs_readfile_file_options_callback."
  ([file-name src-cb]
   (read-file! (force require-fs) "utf-8" file-name src-cb))
  ([encoding-or-opts file-name src-cb]
   (read-file! (force require-fs) encoding-or-opts file-name src-cb))
  ([fs-module encoding-or-opts file-name src-cb]
   (.readFile fs-module file-name encoding-or-opts
              (fn [err source]
                (src-cb (when-not err
                          source))))))
