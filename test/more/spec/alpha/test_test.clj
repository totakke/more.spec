(ns more.spec.alpha.test-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [*test-out* deftest is testing]]
            [more.spec.alpha.test]))

(defmacro with-test-out-str
  [& body]
  `(with-out-str
     (binding [*test-out* *out*]
       ~@body)))

(s/def ::test-spec (s/and integer? pos?))

(deftest valid?-test
  (testing "pass"
    (is (= "" (with-test-out-str
                (is (s/valid? ::test-spec 1)))))

    (is (= "" (with-test-out-str
                (is (valid? ::test-spec 1))))))

  (testing "fail"
    (is (re-find #"actual: \(not \(s/valid\? :more.spec.alpha.test-test/test-spec -1\)\)"
                 (with-test-out-str
                   (is (s/valid? ::test-spec -1)))))

    (is (re-find #"actual: -1 - failed: pos\? spec: :more\.spec\.alpha\.test-test/test-spec"
                 (with-test-out-str
                   (is (valid? ::test-spec -1)))))))
