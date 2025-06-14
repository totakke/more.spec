(ns more.spec.alpha.specs-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [are deftest is]]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [more.spec.alpha.specs :as specs]
            [more.spec.alpha.test]))

(deftest email-test
  (are [email] (valid? ::specs/email email)
    "test@example.com"
    "test.dots@example.com"
    "TEST@EXAMPLE.COM"
    "test_sym-bol@example.com")

  (are [email] (false? (s/valid? ::specs/email email))
    "test+symbol@example.com"
    "test@example")

  (is (:result (tc/quick-check 100
                               (prop/for-all [x (s/gen ::specs/email)]
                                 (string? x))))))

(deftest iso-local-date-test
  (are [date] (valid? ::specs/iso-local-date date)
    "1970-01-01"
    "2025-06-10"
    "2025-12-31")

  (are [date] (false? (s/valid? ::specs/iso-local-date date))
    "1970-01-1"
    "1970-1-01"
    "1970-00-01"
    "2025-00-10"
    "2025-12-33"
    "2025-13-31")

  (is (:result (tc/quick-check 100
                               (prop/for-all [x (s/gen ::specs/iso-local-date)]
                                 (string? x))))))
