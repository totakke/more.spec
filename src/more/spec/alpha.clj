(ns more.spec.alpha
  "Provides just a bit more convenient functionality to clojure.spec."
  (:refer-clojure :exclude [count re-find re-matches])
  (:require [clojure.spec.alpha :as s]
            [clojure.string]))

(alias 'c 'clojure.core)

(defn- unqualify-keyword
  [key]
  (keyword (name key)))

(defmacro only-keys
  "Takes the same arguments as clojure.spec.alpha/keys and creates a map
  validating spec. However, the spec disallows undefined keys."
  [& {:keys [req req-un opt opt-un] :as m}]
  (let [keys-args (apply concat m)
        key-set (set (concat req
                             (map unqualify-keyword req-un)
                             opt
                             (map unqualify-keyword opt-un)))]
    `(s/and (s/keys ~@keys-args)
            (s/map-of ~key-set any?))))

(defmacro string
  "Returns a spec for a string.

  Takes several kwargs options that further constrain the string:

  :not-blank - the string is not blank (default false)
  :count - specifies the string length exactly (default nil)
  :min-count, :max-count - range of the string length (<= min-count count max-count)
                           (defaults nil)
  :match - the string matches a regexp, using clojure.core/re-matches
           (default nil)"
  [& {:keys [not-blank count min-count max-count match]}]
  (let [xs (cond-> []
             not-blank (conj `(complement clojure.string/blank?))
             count (conj `#(= ~count (c/count %)))
             min-count (conj `#(<= ~min-count (c/count %)))
             max-count (conj `#(>= ~max-count (c/count %)))
             match (conj `#(c/re-matches ~match %)))]
    `(s/and string? ~@xs)))

(defmacro re-find
  "Returns a spec that validates a string satisfying re regexp, using
  clojure.core/re-find."
  [re]
  `(s/and string? #(c/re-find ~re %)))

(defmacro re-matches
  "Returns a spec that validates a string satisfying re regexp, using
  clojure.core/re-matches."
  [re]
  `(string :match ~re))
