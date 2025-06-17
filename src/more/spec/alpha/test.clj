(ns more.spec.alpha.test
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.test :as test]))

(defmethod test/assert-expr 'valid?
  [msg form]
  (let [args (rest form)]
    `(if (s/valid? ~@args)
       (test/do-report {:type :pass
                        :message ~msg
                        :expected '~form
                        :actual '~form})
       (test/do-report {:type :fail
                        :message ~msg
                        :expected '~form
                        :actual (symbol (string/trim-newline
                                         (s/explain-str ~@args)))}))))
