(ns replumb.options
  (:require [replumb.common :as common]
            [replumb.load :as load]
            [replumb.browser :as browser]
            [replumb.nodejs :as nodejs]))

(def valid-opts-set
  "Set of valid option used for external input validation."
  #{:verbose :warning-as-error :target :init-fn!
    :load-fn! :read-file-fn! :src-paths})

(defn valid-opts
  "Validate the input user options. Returns a new map without invalid
  ones according to valid-opts-set."
  [user-opts]
  (into {} (filter (comp valid-opts-set first) user-opts)))

(defn add-default-opts
  "Given user provided options, conjoins the default option map for
  its :target (string or keyword). Defaults to conjoining :default (browser,
  aka :js target)."
  [opts user-opts]
  (merge opts (condp = (keyword (:target user-opts))
                :nodejs nodejs/default-opts
                browser/default-opts)))

(defn add-load-fn
  "Given current and user options, if :load-fn! is present in user-opts,
  conjoins it. Try to create and conjoin one from :src-paths
  and :read-file-fn! otherwise. Conjoins nil if it cannot."
  [opts user-opts]
  (-> opts
      (dissoc :read-file-fn!)
      (assoc :load-fn!
             (or (:load-fn! user-opts)
                 (let [read-file-fn (:read-file-fn! user-opts)
                       src-paths (:src-paths user-opts)]
                   (if (and read-file-fn (sequential? src-paths))
                     (load/make-load-fn (:verbose user-opts)
                                        (into [] src-paths)
                                        read-file-fn)
                     (when (:verbose user-opts)
                       (common/debug-prn "Invalid :read-file-fn! or :src-paths (is it a valid sequence?). Cannot create *load-fn*."))))))))

(defn add-init-fns
  "Given current and user options, returns a map containing a
  valid :init-fns,conjoining with the one in current if necessary."
  [opts user-opts]
  (update-in opts [:init-fns] (fn [init-fns]
                                (if-let [fn (:init-fn! user-opts)]
                                  (conj init-fns fn)
                                  init-fns))))

(defn normalize-opts
  "Process the user options. Returns the map that can be fed to
  read-eval-call."
  [user-opts]
  ;; AR - I would need an applicative functor here but let's keep it simple
  (let [vld-opts (valid-opts user-opts)]
    ;; AR - note the order here, the last always overrides
    (-> vld-opts
        (add-default-opts vld-opts)
        (add-load-fn vld-opts)
        (add-init-fns vld-opts))))
