(ns replumb.nodejs.io
  (:require [cljs.nodejs :as nodejs]))

(def require-fs
  "Delay containing the call to \"require fs\". It returns the File
  System Node.js module object."
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
  ([file-path src-cb]
   (read-file! (force require-fs) "utf-8" file-path src-cb))
  ([encoding-or-opts file-path src-cb]
   (read-file! (force require-fs) encoding-or-opts file-path src-cb))
  ([fs-module encoding-or-opts file-path src-cb]
   (try
     ;; AR the async readFile had weird behavior and was wreaking havoc
     (src-cb (.readFileSync fs-module file-path encoding-or-opts))
     (catch :default e
       (src-cb nil)))))

(defn write-file!
  "Accepts the fs module object, encoding or file options, file name to
  write and a callback. Upon success, invokes the callback with the
  source of the file. Otherwise invokes the callback with nil.

  The arity without explicit module will call require the first time
  only. The arity without encoding-or-opts will default to no file
  options and encoding \"UTF-8\".

  It is synchronous and returns undefined as per Node.js doc.

  For encoding-or-opts, see
  https://nodejs.org/api/fs.html#fs_fs_writefile_file_data_options_callback."
  ([file-path data]
   (write-file! "utf-8" file-path data))
  ([encoding-or-opts file-path data]
   (write-file! (force require-fs) encoding-or-opts file-path data))
  ([fs-module encoding-or-opts file-path data]
   (try
     (.writeFileSync fs-module file-path data encoding-or-opts)
     (catch :default e
       (println (.-stack e))))))

(defn delete-file!
  "Accepts the fs module object, encoding or file options, file name to
  write and a callback. Upon success, invokes the callback with the
  source of the file. Otherwise invokes the callback with nil.

  The arity without explicit module will call require the first time
  only. The arity without encoding-or-opts will default to no file
  options and encoding \"UTF-8\".

  It is synchronous and returns undefined as per Node.js doc.

  For encoding-or-opts, see
  https://nodejs.org/api/fs.html#fs_fs_writefile_file_data_options_callback."
  ([file-path]
   (delete-file! (force require-fs) file-path))
  ([fs-module file-path]
   (try
     (.unlinkSync fs-module file-path)
     (catch :default e
       (println (.-stack e))))))
