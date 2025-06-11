(ns more.spec.alpha-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [more.spec.alpha :as ms]
            [more.spec.alpha.test]))

(s/def ::only-keys-map (ms/only-keys :req [::only-keys-key1]
                                     :opt [::only-keys-key2]))

(s/def ::only-keys-key1 string?)
(s/def ::only-keys-key2 integer?)

(deftest only-keys-test
  (testing "valid"
    (is (valid? ::only-keys-map {::only-keys-key1 "1"
                                 ::only-keys-key2 2}))
    (is (valid? ::only-keys-map {::only-keys-key1 "1"})))

  (testing "invalid"
    (is (false? (s/valid? ::only-keys-map {::only-keys-key1 "1"
                                           ::only-keys-key2 2
                                           ::only-keys-key3 3})))
    (is (false? (s/valid? ::only-keys-map {::only-keys-key2 2})))
    (is (false? (s/valid? ::only-keys-map {::only-keys-key1 1
                                           ::only-keys-key2 2})))))

(s/def ::re-find-spec (ms/re-find #"Hello"))

(deftest re-find-test
  (testing "valid"
    (is (valid? ::re-find-spec "Hello"))
    (is (valid? ::re-find-spec "Hello, world!")))

  (testing "invalid"
    (is (false? (s/valid? ::re-find-spec "Goodbye, world!")))
    (is (false? (s/valid? ::re-find-spec nil)))))

(s/def ::re-matches-spec (ms/re-matches #"Hello"))

(deftest re-matches-test
  (testing "valid"
    (is (valid? ::re-matches-spec "Hello")))

  (testing "invalid"
    (is (false? (s/valid? ::re-matches-spec "Hello, world!")))
    (is (false? (s/valid? ::re-matches-spec "Goodbye, world!")))
    (is (false? (s/valid? ::re-matches-spec nil)))))
