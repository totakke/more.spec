(ns more.spec.alpha
  (:refer-clojure :exclude [re-find re-matches])
  (:require [clojure.spec.alpha :as s]))

(alias 'c 'clojure.core)

(defn- unqualify-keyword
  [key]
  (keyword (name key)))

(defmacro only-keys
  [& {:keys [req req-un opt opt-un] :as m}]
  (let [keys-args (apply concat m)
        key-set (set (concat req
                             (map unqualify-keyword req-un)
                             opt
                             (map unqualify-keyword opt-un)))]
    `(s/and (s/keys ~@keys-args)
            (s/map-of ~key-set any?))))

(defmacro re-find
  [re]
  `(s/and string? #(c/re-find ~re %)))

(defmacro re-matches
  [re]
  `(s/and string? #(c/re-matches ~re %)))
