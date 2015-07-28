(ns com.gfredericks.exact.impl
  "cljs impl."
  (:refer-clojure :exclude [=])
  (:require [goog.math.Integer :as int]))

(defn bigint?
  [x]
  (instance? goog.math.Integer x))

(defn bigint
  [x]
  (if (bigint? x)
    x
    (int/fromString (str x))))

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

(declare ratio)

(extend-type goog.math.Integer
  Add
  (-add [x y] (-add-with-integer y x))
  AddWithInteger

  (-add-with-integer [x y]
    (.add x y))
  AddWithRatio
  (-add-with-ratio [x y]
    (-add-with-ratio (ratio x) y))
  Multiply
  (-multiply [x y]
    (-multiply-with-integer y x))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (.multiply x y))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (-multiply-with-ratio (ratio x) y))
  Negate
  (-negate [x] (.negate x))
  Invert
  (-invert [x] (ratio 1 x))
  Ordered
  (-compare [x y] (cljs/- (-compare-to-integer y x)))
  CompareToInteger
  (-compare-to-integer
    [x y]
    (.compare x y))
  CompareToRatio
  (-compare-to-ratio
    [x y]
    (-compare-to-ratio (ratio x) y)))

(defn gcd
  [x y]
  (if (.isZero y)
    x
    (recur y (.modulo x y))))

(deftype Ratio
  ;; "Ratios should not be constructed directly by user code; we assume n and d are
  ;;  canonical; i.e., they are coprime and at most n is negative."
  [n d]
  Add
  (-add [x y] (-add-with-ratio y x))
  AddWithInteger
  (-add-with-integer [x y]
    (-add-with-ratio x (ratio y)))
  AddWithRatio
  (-add-with-ratio [x y]
    (let [+ -add-with-integer
          * -multiply-with-integer
          n' (+ (* (.-n x) (.-d y))
                (* (.-d x) (.-n y)))
          d' (* (.-d x) (.-d y))]
      (ratio n' d')))
  Multiply
  (-multiply [x y] (-multiply-with-ratio y x))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (-multiply x (ratio y)))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (let [* -multiply-with-integer
          n' (* (.-n x) (.-n y))
          d' (* (.-d x) (.-d y))]
      (ratio n' d')))
  Negate
  (-negate [x]
    (Ratio. (-negate n) d))
  Invert
  (-invert [x]
    (if (.isNegative n)
      (Ratio. (.negate d) (.negate n))
      (Ratio. d n)))
  Ordered
  (-compare [x y]
    (cljs/- (-compare-to-ratio y x)))
  CompareToInteger
  (-compare-to-integer [x y]
    (-compare-to-ratio x (ratio y)))
  CompareToRatio
  (-compare-to-ratio [x y]
    (let [* -multiply-with-integer]
      (-compare-to-integer (* (.-n x) (.-d y))
                           (* (.-n y) (.-d x))))))

(def ZERO (bigint 0))
(def ONE (bigint 1))

(defn ratio
  ([x] (ratio x ONE))
  ([x y]
     (let [x (bigint x),
           y (bigint y),
           d (gcd x y)
           x' (.divide x d)
           y' (.divide y d)]
       (if (.isNegative y')
         (Ratio. (.negate x') (.negate y'))
         (Ratio. x' y')))))

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
