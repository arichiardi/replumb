(ns replumb-repl.console
  (:require [clojure.string :as s :refer [join]]
            [cljsjs.jqconsole]
            [replumb.core :as replumb]))

(defn new-jqconsole
  "Creates a new instance of JQConsole which loads on the input
  selector (any jQuery selector will work) and configuration. The
  options are passed as named parameters and follow the jq-console ones.

  * :welcome-string is the string to be shown when the terminal is first
    rendered. Defaults to nil.
  * :prompt-label is the label to be shown before the input when using
    Prompt(). Defaults to \"$ \".
  * :continue-label is the label to be shown before the continued lines
    of the input when using Prompt(). Defaults to nil.
  * :disable-auto-focus is a boolean indicating whether we should disable
    the default auto-focus behavior. Defaults to false, the console always
    focus.

  See https://github.com/replit/jq-console#instantiating"
  [selector {:keys [welcome-string prompt-label continue-label disable-auto-focus]
             :or {welcome-string nil
                  prompt-label "$ "
                  continue-label nil
                  disable-auto-focus false}}]
  (-> (js/$ selector) (.jqconsole welcome-string prompt-label continue-label disable-auto-focus)))

(defn write!
  "Writes a message to the input console. Type is used as class inside
  the rendered message and should be either a string or a keyword. It
  automatically separates messages with \n. Purely side effecting,
  returns nil."
  [console type & messages]
  (when-let [msgs (filter (complement nil?) messages)]
    (.Write console (str (s/join "\n" msgs) "\n") (name type))))

(defn write-error!
  "Writes a jqconsole-error message to the input console. It
  automatically separates messages with \n. Purely side effecting,
  returns nil."
  [console & messages]
  (apply write! console :jqconsole-error messages))

(defn write-return!
  "Writes a jqconsole-return message to the input console. It
  automatically separates messages with \n. Purely side effecting,
  returns nil."
  [console & messages]
  (apply write! console :jqconsole-return messages))

(defn write-output!
  "Writes a jqconsole-return message to the input console. It
  automatically separates messages with \n. Purely side effecting,
  returns nil."
  [console & messages]
  (apply write! console :jqconsole-output messages))

(defn write-exception!
  ([console ex] (write-exception! console ex false))
  ([console ex print-stack-trace?]
   (write-error! console (replumb/error->str print-stack-trace? ex))))
