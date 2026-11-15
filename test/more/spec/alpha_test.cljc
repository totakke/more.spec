(ns more.spec.alpha-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [more.spec.alpha :as ms]))

(s/def ::only-keys-key1 string?)
(s/def ::only-keys-key2 integer?)
(s/def ::only-keys-key3 keyword?)

(s/def ::only-keys-map (ms/only-keys :req [::only-keys-key1]
                                     :opt [::only-keys-key2]))

(deftest only-keys-test
  (testing "valid"
    (is (true? (s/valid? ::only-keys-map {::only-keys-key1 "1"
                                          ::only-keys-key2 2})))
    (is (true? (s/valid? ::only-keys-map {::only-keys-key1 "1"}))))

  (testing "invalid"
    (is (false? (s/valid? ::only-keys-map {::only-keys-key1 "1"
                                           ::only-keys-key2 2
                                           ::only-keys-key3 3})))
    (is (false? (s/valid? ::only-keys-map {::only-keys-key2 2})))
    (is (false? (s/valid? ::only-keys-map {::only-keys-key1 1
                                           ::only-keys-key2 2})))))

(s/def ::only-keys-un-map (ms/only-keys :req-un [::only-keys-key1]
                                        :opt-un [::only-keys-key2]))

(deftest only-keys-un-test
  (testing "valid"
    (is (true? (s/valid? ::only-keys-un-map {:only-keys-key1 "1"
                                             :only-keys-key2 2})))
    (is (true? (s/valid? ::only-keys-un-map {:only-keys-key1 "1"}))))

  (testing "invalid"
    (is (false? (s/valid? ::only-keys-un-map {:only-keys-key1 "1"
                                              :only-keys-key3 3})))
    (is (false? (s/valid? ::only-keys-un-map {::only-keys-key1 "1"})))))

(s/def ::only-keys-or-map
  (ms/only-keys :req [(or ::only-keys-key1
                          (and ::only-keys-key2 ::only-keys-key3))]))

(deftest only-keys-or-test
  (testing "valid"
    (is (true? (s/valid? ::only-keys-or-map {::only-keys-key1 "1"})))
    (is (true? (s/valid? ::only-keys-or-map {::only-keys-key2 2
                                             ::only-keys-key3 :a}))))

  (testing "invalid"
    (is (false? (s/valid? ::only-keys-or-map {::only-keys-key2 2})))
    (is (false? (s/valid? ::only-keys-or-map {::only-keys-key1 "1"
                                              ::only-keys-key4 4})))))

(s/def ::only-keys-or-un-map
  (ms/only-keys :req-un [(or ::only-keys-key1 ::only-keys-key2)]))

(deftest only-keys-or-un-test
  (is (true? (s/valid? ::only-keys-or-un-map {:only-keys-key2 2})))
  (is (false? (s/valid? ::only-keys-or-un-map {:only-keys-key2 2
                                               :only-keys-key3 :a}))))

(deftest only-keys-gen-test
  (is (every? #(s/valid? ::only-keys-map %)
              (map first (s/exercise ::only-keys-map 10))))
  (is (every? #(s/valid? ::only-keys-or-map %)
              (map first (s/exercise ::only-keys-or-map 10)))))

(deftest string-test
  (testing "valid"
    (is (true? (s/valid? (ms/string) "foo")))
    (is (true? (s/valid? (ms/string) "")))
    (is (true? (s/valid? (ms/string) " ")))
    (is (true? (s/valid? (ms/string :not-empty true) "foo")))
    (is (true? (s/valid? (ms/string :not-empty true) " ")))
    (is (true? (s/valid? (ms/string :not-blank true) "foo")))
    (is (true? (s/valid? (ms/string :count 3) "foo")))
    (is (true? (s/valid? (ms/string :min-count 1 :max-count 4) "foo")))
    (is (true? (s/valid? (ms/string :re #"[a-z]+") "foo")))
    (is (true? (s/valid? (ms/string :re #"[a-z]+") "foo 123"))))

  (testing "invalid"
    (is (false? (s/valid? (ms/string) 1)))
    (is (false? (s/valid? (ms/string) nil)))
    (is (false? (s/valid? (ms/string :not-empty true) "")))
    (is (false? (s/valid? (ms/string :not-blank true) "")))
    (is (false? (s/valid? (ms/string :not-blank true) " ")))
    (is (false? (s/valid? (ms/string :count 3) "foobar")))
    (is (false? (s/valid? (ms/string :min-count 1 :max-count 4) "foobar")))
    (is (false? (s/valid? (ms/string :re #"[a-z]+") "123")))
    (is (false? (s/valid? (ms/string :re #"^[a-z]+$") "foo 123")))))

(s/def ::re-find-spec (ms/re-find #"Hello"))

(deftest re-find-test
  (testing "valid"
    (is (true? (s/valid? ::re-find-spec "Hello")))
    (is (true? (s/valid? ::re-find-spec "Hello, world!"))))

  (testing "invalid"
    (is (false? (s/valid? ::re-find-spec "Goodbye, world!")))
    (is (false? (s/valid? ::re-find-spec nil)))))

(s/def ::re-matches-spec (ms/re-matches #"Hello"))

(deftest re-matches-test
  (testing "valid"
    (is (true? (s/valid? ::re-matches-spec "Hello"))))

  (testing "invalid"
    (is (false? (s/valid? ::re-matches-spec "Hello, world!")))
    (is (false? (s/valid? ::re-matches-spec "Goodbye, world!")))
    (is (false? (s/valid? ::re-matches-spec nil)))))
