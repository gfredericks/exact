(ns com.gfredericks.exact.impl
  "cljs impl."
  (:refer-clojure :exclude [= -compare compare numerator denominator integer?
                            mod rem quot even? odd?])
  (:require [cljs.core :as cljs]
            [goog.math.Integer :as int]))

(defn integer?
  [x]
  (instance? goog.math.Integer x))

(defprotocol Add
  (-add [x y]))
(defprotocol AddWithInteger
  (-add-with-integer [x y]))
(defprotocol AddWithRatio
  (-add-with-ratio [x y]))

(defprotocol Multiply
  (-multiply [x y]))
(defprotocol MultiplyWithInteger
  (-multiply-with-integer [x y]))
(defprotocol MultiplyWithRatio
  (-multiply-with-ratio [x y]))

(defprotocol Invert
  (-invert [x]))

(defprotocol Negate
  (-negate [x]))

(defprotocol Ordered
  (-compare [x y]))
(defprotocol CompareToInteger
  (-compare-to-integer [x y]))
(defprotocol CompareToRatio
  (-compare-to-ratio [x y]))

(defn ^:private gcd
  [x y]
  (if (.isZero y)
    x
    (recur y (.modulo x y))))

(declare -ratio)

(defn ^:private normalize
  [n d]
  (if (.isNegative d)
    (let [n' (.negate n)
          d' (.negate d)]
      (if (.equals d' int/ONE)
        n'
        (-ratio n' d')))
    (if (.equals d int/ONE)
      n
      (-ratio n d))))

(deftype Ratio
  ;; "Ratios should not be constructed directly by user code; we assume n and d are
  ;;  canonical; i.e., they are coprime and at most n is negative."
  [n d]
  Object
  (toString [_]
    (str "#ratio [" n " " d "]"))

  Add
  (-add [x y] (-add-with-ratio y x))
  AddWithInteger
  (-add-with-integer [x y]
    (-add-with-ratio x (-ratio y)))
  AddWithRatio
  (-add-with-ratio [x y]
    (let [+ -add-with-integer
          * -multiply-with-integer
          n' (+ (* (.-n x) (.-d y))
                (* (.-d x) (.-n y)))
          d' (* (.-d x) (.-d y))
          the-gcd (gcd n' d')]
      (normalize (.divide n' the-gcd) (.divide d' the-gcd))))
  Multiply
  (-multiply [x y] (-multiply-with-ratio y x))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (-multiply x (-ratio y)))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (let [* -multiply-with-integer
          n' (* (.-n x) (.-n y))
          d' (* (.-d x) (.-d y))
          the-gcd (gcd n' d')]
      (normalize (.divide n' the-gcd) (.divide d' the-gcd))))
  Negate
  (-negate [x]
    (Ratio. (-negate n) d))
  Invert
  (-invert [x]
    (normalize d n))
  Ordered
  (-compare [x y]
    (cljs/- (-compare-to-ratio y x)))
  CompareToInteger
  (-compare-to-integer [x y]
    (-compare-to-ratio x (-ratio y)))
  CompareToRatio
  (-compare-to-ratio [x y]
    (let [* -multiply-with-integer]
      (-compare-to-integer (* (.-n x) (.-d y))
                           (* (.-n y) (.-d x)))))

  IEquiv
  (-equiv [_ other]
    (and (instance? Ratio other)
         (cljs/= n (.-n other))
         (cljs/= d (.-d other))))
  IHash
  (-hash [_]
    (bit-xor 124790411 (-hash n) (-hash d)))
  IComparable
  (-compare [x y]
    (-compare x y)))

(defn ^:private -ratio
  ([n] (Ratio. n int/ONE))
  ([n d] (Ratio. n d)))

(extend-type goog.math.Integer
  Add
  (-add [x y] (-add-with-integer y x))
  AddWithInteger

  (-add-with-integer [x y]
    (.add x y))
  AddWithRatio
  (-add-with-ratio [x y]
    (-add-with-ratio (-ratio x) y))
  Multiply
  (-multiply [x y]
    (-multiply-with-integer y x))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (.multiply x y))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (-multiply-with-ratio (-ratio x) y))
  Negate
  (-negate [x] (.negate x))
  Invert
  (-invert [x] (-ratio int/ONE x))
  Ordered
  (-compare [x y] (cljs/- (-compare-to-integer y x)))
  CompareToInteger
  (-compare-to-integer
    [x y]
    (.compare x y))
  CompareToRatio
  (-compare-to-ratio
    [x y]
    (-compare-to-ratio (-ratio x) y))

  IEquiv
  (-equiv
    [x y]
    (and (integer? y) (.equals x y)))
  IHash
  (-hash
    [self]
    ;; dunno?
    (reduce bit-xor 899242490 (.-bits_ self)))
  IComparable
  (-compare [x y]
    (-compare x y)))


(def ZERO int/ZERO)
(def ONE int/ONE)

(defn ratio?
  [x]
  (instance? Ratio x))

(defn add
  [x y]
  (-add x y))

(defn negate
  [x]
  (-negate x))

(defn multiply
  [x y]
  (-multiply x y))

(defn invert
  [x]
  (-invert x))

(defn compare
  [x y]
  (-compare x y))

(defn string->integer
  [s radix]
  (int/fromString s radix))

(defn integer->string
  [n radix]
  (.toString n radix))

(defn numerator
  [x]
  {:pre [(ratio? x)]}
  (.-n x))

(defn denominator
  [x]
  {:pre [(ratio? x)]}
  (.-d x))

(defn quot
  [x n]
  {:pre [(integer? x) (integer? n)]}
  (.divide x n))

(defn rem
  [x n]
  {:pre [(integer? x) (integer? n)]}
  (.modulo x n))

(defn mod
  [x n]
  (let [y (rem x n)]
    (cond-> y (.isNegative y) (.add n))))

(defn odd?
  [n]
  (.isOdd n))

(defn even?
  [n]
  (not (.isOdd n)))

(def two-to-fifty-three
  (apply * (repeat 53 2)))

(def minus-two-to-fifty-three
  (- two-to-fifty-three))

(defn native-integer?
  [num]
  (and (number? num)
       (<= minus-two-to-fifty-three
           num
           two-to-fifty-three)
       (cljs/integer? num)))

(defn native->integer
  [num]
  {:pre [(native-integer? num)]}
  (int/fromNumber num))

(defn integer->native
  [x]
  {:post [(native-integer? %)]}
  (.toNumber x))
