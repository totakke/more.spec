# more.spec

[![Clojars Project](https://img.shields.io/clojars/v/net.totakke/more.spec.svg)](https://clojars.org/net.totakke/more.spec)
[![build](https://github.com/totakke/more.spec/actions/workflows/build.yml/badge.svg)](https://github.com/totakke/more.spec/actions/workflows/build.yml)

Just a bit more convenience with [clojure.spec](https://clojure.org/about/spec)

## Installation

Clojure CLI/deps.edn:

```clojure
net.totakke/more.spec {:mvn/version "0.2.15"}
```

Leiningen/Boot:

```clojure
[net.totakke/more.spec "0.2.15"]
```

## Usage

### `more.spec.alpha`

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

### `more.spec.alpha.test`

`valid?` in clojure.test:

```clojure
(require '[clojure.test :refer [deftest is]]
         '[more.spec.alpha.test])

(s/def :int/pos (s/and integer? pos?))

(deftest int-pos-test
  (is (valid? :int/pos -1)))

(int-pos-test)
;; FAIL in (int-pos-test)
;; expected: (valid? :int/pos -1)
;; actual: -1 - failed: pos? spec: :int/pos  ; spec failure is explained.
;;=> nil
```

### `more.spec.alpha.specs`

`::mspecs/email`:

```clojure
(require '[clojure.spec.gen.alpha :as gen]
         '[more.spec.alpha.specs :as mspecs])

(s/valid? ::mspecs/email "test@example.com")
;;=> true

(gen/generate (s/gen ::mspecs/email))
;;=> "di.i003glc@ehmipb.osed.org"
```

`::mspecs/iso-local-date`:

```clojure
(s/valid? ::mspecs/iso-local-date "2025-06-11")
;;=> true

(gen/generate (s/gen ::mspecs/iso-local-date))
;;=> "2017-12-07"
```

## License

Copyright 2025 [Toshiki Takeuchi](https://totakke.net/)

Distributed under the MIT License.
