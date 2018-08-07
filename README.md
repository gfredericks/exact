# exact

[![Circle CI](https://circleci.com/gh/gfredericks/exact.svg?style=svg)](https://circleci.com/gh/gfredericks/exact)

A portable clojure library for doing exact arithmetic. I.e., you can
write code that uses bigints and ratios and it works in CLJS too.

Note: there are no BigDecimals in the Exact library, in the same way that there are no decimals in exact arithmetic. In other words, there's no way to convert `2.2` (or `2.2M`) to an Exact representation.

## Obtention

``` clojure
[com.gfredericks/exact "0.1.11"]
```

## Usage

``` clojure
(ns my.namespace
  ;; here you can choose to exclude clojure's functions and use those
  ;; from exact if you want it to look like normal arithmetic
  (:require [com.gfredericks.exact :as e]))

(def TWO (e/native->integer 2))
(defn square [x] (e/* x x))
(defn avg [a b] (-> a (e/+ b) (e// TWO)))

(defn newtonian-square-root
  "Returns a ratio between (- (sqrt x) epsilon) and (+ (sqrt x) epsilon)"
  [x epsilon]
  (let [ee (square epsilon)]
    (loop [guess (avg x e/ZERO)]
      (if (-> guess square (e/- x) e/abs (e/< ee))
        guess
        (recur (-> x (e// guess) (avg guess)))))))

(def epsilon
  (e// (e/string->integer "1000000000000000000000000")))

(newtonian-square-root TWO epsilon)
;; => 1572584048032918633353217/1111984844349868137938112
```

### Things in `com.gfredericks.exact`

- `ZERO`
- `ONE`
- `+`
- `-`
- `*`
- `/`
- `zero?`
- `inc`
- `dec`
- `<`
- `>`
- `<=`
- `>=`
- `max`
- `min`
- `min-key`
- `max-key`
- `pos?`
- `neg?`
- `numerator`
- `denominator`
- `integer?`
- `ratio?`
- `quot`
- `mod`
- `rem`
- `abs`
- `even?`
- `odd?`
- `string->integer`
- `integer->string`
- `native->integer`
- `integer->native`

## Caveats

Most of the functions are not designed to be used with native numbers,
since this can be problematic in ClojureScript. To use it portably,
you must create integers with `native->integer` or `string->integer`.

## License

Copyright © 2015 Gary Fredericks

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
