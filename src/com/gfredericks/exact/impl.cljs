(ns com.gfredericks.exact.impl
  "cljs impl."
  (:refer-clojure :exclude [= -compare compare numerator denominator])
  (:require [cljs.core :as cljs]
            [goog.math.Integer :as int]))

(defn bigint?
  [x]
  (instance? goog.math.Integer x))

(defn bigint
  [x]
  (if (bigint? x)
    x
    (int/fromString (str x))))

(def hacky-bigint bigint)

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
    (zero? (.compare x y)))
  IComparable
  (-compare [x y]
    (-compare x y)))


(def ZERO (bigint 0))
(def ONE (bigint 1))

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

(defn ->integer
  [s]
  (int/fromString s))

(defn numerator
  [x]
  {:pre [(ratio? x)]}
  (.-n x))

(defn denominator
  [x]
  {:pre [(ratio? x)]}
  (.-d x))
