(ns more.spec.alpha.test
  "Provides an assert expression, valid?, to validate a value with a spec in
  clojure.test, which outputs an explanation upon test failure.

    (s/def :int/pos (s/and integer? pos?))

    (is (valid? :int/pos -1))
    ;; FAIL in () (NO_SOURCE_FILE:xx)
    ;; expected: (valid? :int/pos -1)
    ;;   actual: -1 - failed: pos? spec: :int/pos
    ;;=> nil"
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
