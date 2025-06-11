# more.spec

Just a bit more convenience with [clojure.spec](https://clojure.org/about/spec)

## Usage

`only-keys`:

```clojure
(require '[clojure.spec.alpha :as s]
         '[more.spec.alpha :as ms])

(s/def :acct/first-name string?)
(s/def :acct/last-name string?)
(s/def :acct/middle-name string?)

(s/def :acct/person
  (ms/only-keys :req [:acct/first-name :acct/last-name]
                :opt [:acct/middle-name]))

(s/valid? :acct/person
  {:acct/first-name "Bugs"
   :acct/last-name "Bunny"
   :acct/email "test@example.com"}) ; :acct/email is not defined.
;;=> false
```

`re-find`:

```clojure
(s/def :greeting/hello (ms/re-find #"Hello"))

(s/valid? :greeting/hello "Hello, world!")
;;=> true

(s/valid? :greeting/hello "Goodbye, world!")
;;=> false
```

`re-matches`:

```clojure
(s/def :greeting/hello (ms/re-matches #"Hello"))

(s/valid? :greeting/hello "Hello")
;;=> true

(s/valid? :greeting/hello "Hello, world!")
;;=> false
```

## License

Copyright 2025 [Toshiki Takeuchi](https://totakke.net/)

Distributed under the MIT License.
