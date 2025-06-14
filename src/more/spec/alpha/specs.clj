(ns more.spec.alpha.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as string]
            [more.spec.alpha :as ms]))

;; Email, e.g., test@example.com

(def email-regex #"[\w-\.]+@([\w-]+\.)+[\w-]{2,12}")

(def ^:private email-char-gen
  (gen/fmap char (gen/frequency [[1 (gen/return 45)]
                                 [3 (gen/choose 48 57)]
                                 [1 (gen/return 95)]
                                 [20 (gen/choose 97 122)]])))

(def ^:private email-string-gen
  (gen/such-that #(re-matches #"[a-z].*[0-9a-z]" %)
                 (gen/fmap string/join (gen/vector email-char-gen 2 10))))

(def ^:private sample-tlds
  ["biz" "ca" "cn" "com" "de" "dev" "edu" "fr" "gov" "info" "international" "jp"
   "kr" "museum" "net" "org" "site" "uk" "us"])

(def email-gen
  (gen/fmap
   (fn [[user sd tld]]
     (string/lower-case (format "%s@%s.%s" user sd tld)))
   (gen/tuple (gen/fmap #(string/join "." %) (gen/vector email-string-gen 1 2))
              (gen/fmap #(string/join "." %) (gen/vector email-string-gen 1 3))
              (gen/elements sample-tlds))))

(s/def ::email
  (s/with-gen (ms/re-matches email-regex)
    (constantly email-gen)))

;; ISO-8601 local date, e.g., 2025-06-11

(def iso-local-date-regex #"\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])")

(def iso-local-date-gen
  (gen/fmap
   (fn [[year month day]]
     (format "%04d-%02d-%02d" year month day))
   (gen/tuple (gen/choose 1970 2100)
              (gen/choose 1 12)
              (gen/choose 1 28))))

(s/def ::iso-local-date
  (s/with-gen (ms/re-matches iso-local-date-regex)
    (constantly iso-local-date-gen)))
