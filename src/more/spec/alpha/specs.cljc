(ns more.spec.alpha.specs
  "Predefined specs and generators which are popular in real production."
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.string :as string]
            #?@(:cljs [[goog.string :refer [format]]
                       [goog.string.format]])
            [more.spec.alpha :as ms])
  #?(:cljs (:import [goog Uri])))

;; Email, e.g., test@example.com

(def email-regex #"^[\w-\.]+@([\w-]+\.)+[\w-]{2,12}$")

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
  (s/with-gen (ms/string :re email-regex)
    (constantly email-gen)))

;; ISO-8601 local date, e.g., 2025-06-11

(def iso-local-date-regex #"^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$")

(def iso-local-date-gen
  (gen/fmap
   (fn [[year month day]]
     (format "%04d-%02d-%02d" year month day))
   (gen/tuple (gen/choose 1970 2100)
              (gen/choose 1 12)
              (gen/choose 1 28))))

(s/def ::iso-local-date
  (s/with-gen (ms/string :re iso-local-date-regex)
    (constantly iso-local-date-gen)))

;; HTTP status code (RFC 9110), e.g., 404

(s/def ::http-status
  (s/int-in 100 600))

;; URI reference (RFC 3986), e.g., https://example.com/path, /path, about:blank
;;
;; The regex checks scheme syntax, allowed characters, and percent-encoding, but
;; does not strictly validate the authority component.

(def uri-reference-regex
  #"^(?:[A-Za-z][A-Za-z0-9+.\-]*:|(?![^/?#]*:))(?:[A-Za-z0-9\-._~!$&'()*+,;=:@/\[\]]|%[0-9A-Fa-f]{2})*(?:\?(?:[A-Za-z0-9\-._~!$&'()*+,;=:@/?]|%[0-9A-Fa-f]{2})*)?(?:#(?:[A-Za-z0-9\-._~!$&'()*+,;=:@/?]|%[0-9A-Fa-f]{2})*)?$")

;; Generators are built lazily so that test.check is required only when
;; generating values.

;; Host label (RFC 1123), e.g., example
(defn- uri-host-label-char-gen []
  (gen/fmap char (gen/frequency [[1 (gen/return 45)]
                                 [3 (gen/choose 48 57)]
                                 [20 (gen/choose 97 122)]])))

(defn- uri-host-label-gen []
  (gen/such-that #(re-matches #"[0-9a-z](.*[0-9a-z])?" %)
                 (gen/fmap string/join (gen/vector (uri-host-label-char-gen) 1 10))))

;; Path segment or query component consisting of unreserved characters
(defn- uri-segment-char-gen []
  (gen/fmap char (gen/frequency [[3 (gen/elements [45 46 95 126])]
                                 [3 (gen/choose 48 57)]
                                 [20 (gen/choose 97 122)]])))

(defn- uri-segment-gen []
  (gen/fmap string/join (gen/vector (uri-segment-char-gen) 1 10)))

(defn- uri-path-gen []
  (gen/fmap #(str "/" (string/join "/" %))
            (gen/vector (uri-segment-gen) 0 3)))

(defn- absolute-uri-gen []
  (gen/fmap
   (fn [[scheme host tld path query]]
     (str scheme "://" host "." tld path (when query (str "?" query))))
   (gen/tuple (gen/elements ["http" "https"])
              (gen/fmap #(string/join "." %) (gen/vector (uri-host-label-gen) 1 3))
              (gen/elements sample-tlds)
              (uri-path-gen)
              (gen/one-of [(gen/return nil)
                           (gen/fmap (fn [[k v]] (str k "=" v))
                                     (gen/tuple (uri-segment-gen)
                                                (uri-segment-gen)))]))))

(defn uri-reference-string-gen
  "Returns a generator of URI reference strings."
  []
  (gen/frequency [[8 (absolute-uri-gen)]
                  [3 (uri-path-gen)]
                  [1 (gen/return "about:blank")]]))

(defn uri-object?
  "Returns true if x is a java.net.URI (clj) or a goog.Uri (cljs)."
  [x]
  #?(:clj (instance? java.net.URI x)
     :cljs (instance? Uri x)))

(defn- ->uri-object
  [s]
  #?(:clj (java.net.URI. s)
     :cljs (Uri/parse s)))

(s/def ::uri-reference-string
  (s/with-gen (ms/string :re uri-reference-regex)
    uri-reference-string-gen))

(s/def ::uri-object
  (s/with-gen uri-object?
    #(gen/fmap ->uri-object (uri-reference-string-gen))))

(s/def ::uri-reference
  (s/or :object ::uri-object
        :string ::uri-reference-string))

;; Problem Details for HTTP APIs (RFC 9457), e.g.,
;; {:type "https://example.com/probs/out-of-credit" :status 403}
;;
;; All members are optional and extension members are allowed. The spec
;; validates maps with unqualified keyword keys.

(s/def :more.spec.alpha.specs.problem-details/type ::uri-reference)
(s/def :more.spec.alpha.specs.problem-details/title string?)
(s/def :more.spec.alpha.specs.problem-details/status ::http-status)
(s/def :more.spec.alpha.specs.problem-details/detail string?)
(s/def :more.spec.alpha.specs.problem-details/instance ::uri-reference)

(s/def ::problem-details
  (s/keys :opt-un [:more.spec.alpha.specs.problem-details/type
                   :more.spec.alpha.specs.problem-details/title
                   :more.spec.alpha.specs.problem-details/status
                   :more.spec.alpha.specs.problem-details/detail
                   :more.spec.alpha.specs.problem-details/instance]))
