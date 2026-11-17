(ns more.spec.alpha.specs-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [are deftest is]]
            [clojure.test.check :as tc]
            [clojure.test.check.properties :as prop]
            [more.spec.alpha.specs :as specs])
  #?(:cljs (:import [goog Uri])))

(deftest email-test
  (are [email] (true? (s/valid? ::specs/email email))
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
  (are [date] (true? (s/valid? ::specs/iso-local-date date))
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

(deftest http-status-test
  (are [status] (true? (s/valid? ::specs/http-status status))
    100
    200
    404
    599)

  (are [status] (false? (s/valid? ::specs/http-status status))
    99
    600
    "404"
    404.5
    nil)

  (is (:result (tc/quick-check 100
                               (prop/for-all [x (s/gen ::specs/http-status)]
                                 (int? x))))))

(deftest uri-reference-string-test
  (are [uri] (true? (s/valid? ::specs/uri-reference-string uri))
    "https://example.com/probs/out-of-credit"
    "http://user@example.com:8080/a/b?c=d&e=f#g"
    "http://[::1]/path"
    "about:blank"
    "urn:isbn:0451450523"
    "mailto:test@example.com"
    "/account/12345/msgs/abc"
    "relative/path"
    "?query"
    "#fragment"
    "/path%20with%20space"
    "")

  (are [uri] (false? (s/valid? ::specs/uri-reference-string uri))
    "https://example.com/has space"
    "https://例え.jp/"
    "/path%zz"
    "/a#b#c"
    "1a:b"
    "<https://example.com>"
    nil)

  (is (:result (tc/quick-check 100
                               (prop/for-all [x (s/gen ::specs/uri-reference-string)]
                                 (string? x))))))

(deftest uri-object-test
  (is (true? (s/valid? ::specs/uri-object
                       #?(:clj (java.net.URI. "https://example.com")
                          :cljs (Uri/parse "https://example.com")))))
  (is (false? (s/valid? ::specs/uri-object "https://example.com")))

  (is (:result (tc/quick-check 100
                               (prop/for-all [x (s/gen ::specs/uri-object)]
                                 (specs/uri-object? x)))))

  ;; Generated absolute URIs must have a valid host
  (is (:result (tc/quick-check 100
                               (prop/for-all [x (s/gen ::specs/uri-object)]
                                 #?(:clj (or (not (#{"http" "https"} (.getScheme x)))
                                             (some? (.getHost x)))
                                    :cljs (or (not (#{"http" "https"} (.getScheme ^js x)))
                                              (seq (.getDomain ^js x)))))))))

(deftest uri-reference-test
  (are [uri] (true? (s/valid? ::specs/uri-reference uri))
    "https://example.com/probs/out-of-credit"
    #?(:clj (java.net.URI. "https://example.com/probs/out-of-credit")
       :cljs (Uri/parse "https://example.com/probs/out-of-credit")))

  (are [uri] (false? (s/valid? ::specs/uri-reference uri))
    "https://example.com/has space"
    404
    nil)

  (let [uri #?(:clj (java.net.URI. "/path") :cljs (Uri/parse "/path"))]
    (is (= [:object uri] (s/conform ::specs/uri-reference uri))))
  (is (= [:string "/path"] (s/conform ::specs/uri-reference "/path")))
  (is (= "/path" (s/unform ::specs/uri-reference [:string "/path"])))

  (is (:result (tc/quick-check 100
                               (prop/for-all [x (s/gen ::specs/uri-reference)]
                                 (or (string? x) (specs/uri-object? x)))))))

(deftest problem-details-test
  (are [m] (true? (s/valid? ::specs/problem-details m))
    {}
    {:type "about:blank"
     :status 404}
    {:type "https://example.com/probs/out-of-credit"
     :title "You do not have enough credit."
     :status 403
     :detail "Your current balance is 30, but that costs 50."
     :instance "/account/12345/msgs/abc"
     :balance 30
     :accounts ["/account/12345" "/account/67890"]}
    {:type #?(:clj (java.net.URI. "https://example.com/probs/out-of-credit")
              :cljs (Uri/parse "https://example.com/probs/out-of-credit"))})

  (are [m] (false? (s/valid? ::specs/problem-details m))
    {:type "https://example.com/has space"}
    {:title 1}
    {:status "403"}
    {:status 700}
    {:detail :detail}
    {:instance "/a#b#c"}
    nil)

  (is (:result (tc/quick-check 100
                               (prop/for-all [x (s/gen ::specs/problem-details)]
                                 (map? x))))))
