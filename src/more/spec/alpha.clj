(ns more.spec.alpha
  "Provides just a bit more convenient functionality to clojure.spec."
  (:refer-clojure :exclude [re-find re-matches])
  (:require [clojure.spec.alpha :as s]))

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

(defmacro re-find
  "Returns a spec that validates a string satisfying re regexp, using
  clojure.core/re-find."
  [re]
  `(s/and string? #(c/re-find ~re %)))

(defmacro re-matches
  "Returns a spec that validates a string satisfying re regexp, using
  clojure.core/re-matches."
  [re]
  `(s/and string? #(c/re-matches ~re %)))
