(ns com.gfredericks.exact.impl
  "cljs impl."
  (:refer-clojure :exclude [= -compare compare numerator denominator integer?
                            mod rem quot even? odd?])
  (:require [cljs.core :as cljs]
            [goog.math.Integer :as int]))

(def ^:private MAX_INTEGER (apply * (repeat 53 2)))
(def ^:private MIN_INTEGER (- MAX_INTEGER))

(def two-to-fifty-three
  (apply * (repeat 53 2)))

(def minus-two-to-fifty-three
  (- two-to-fifty-three))

(defn native-integer?
  [num]
  (and (number? num)
       (cljs/integer? num)
       (<= MIN_INTEGER num MAX_INTEGER)))

(defn goog-integer?
  [x]
  (instance? goog.math.Integer x))

(def integer? (some-fn goog-integer? native-integer?))

(defn native->integer
  [num]
  {:pre [(native-integer? num)]}
  (int/fromNumber num))

(defn integer->native
  [x]
  {:post [(native-integer? %)]}
  (.toNumber x))

(defprotocol Add
  (-add [x y]))
(defprotocol AddWithNative
  (-add-with-native [x y]))
(defprotocol AddWithInteger
  (-add-with-integer [x y]))
(defprotocol AddWithRatio
  (-add-with-ratio [x y]))

(defprotocol Multiply
  (-multiply [x y]))
(defprotocol MultiplyWithNative
  (-multiply-with-native [x y]))
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
(defprotocol CompareToNative
  (-compare-to-native [x y]))
(defprotocol CompareToInteger
  (-compare-to-integer [x y]))
(defprotocol CompareToRatio
  (-compare-to-ratio [x y]))

(defn ^:private gcd-goog
  [x y]
  (if (.isZero y)
    x
    (recur y (.modulo x y))))

(defn ^:private gcd-native
  [x y]
  (if (cljs/zero? y)
    x
    (recur y (cljs/mod x y))))

(defn ^:private gcd
  [x y]
  (if (goog-integer? x)
    (gcd-goog x (cond-> y (not (goog-integer? y)) native->integer))
    (if (goog-integer? y)
      (gcd-goog (native->integer x) y)
      (gcd-native x y))))

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
  AddWithNative
  (-add-with-native [x y]
    (-add-with-integer x (native->integer y)))
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
  MultiplyWithNative
  (-multiply-with-native [x y]
    (-multiply-with-integer x (native->integer y)))
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
  CompareToNative
  (-compare-to-native [x y]
    (-compare-to-integer x (native->integer y)))
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
  ([n] (Ratio. n 1))
  ([n d] (Ratio. n d)))

(extend-type goog.math.Integer
  Add
  (-add [x y] (-add-with-integer y x))
  AddWithNative
  (-add-with-native [x y]
    (-add-with-integer x (native->integer y)))
  AddWithInteger
  (-add-with-integer [x y]
    (.add x y))
  AddWithRatio
  (-add-with-ratio [x y]
    (-add-with-ratio (-ratio x) y))
  Multiply
  (-multiply [x y]
    (-multiply-with-integer y x))
  MultiplyWithNative
  (-multiply-with-native [x y]
    (-multiply-with-integer x (native->integer y)))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (.multiply x y))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (-multiply-with-ratio (-ratio x) y))
  Negate
  (-negate [x] (.negate x))
  Invert
  (-invert [x]
    (if (.isZero x)
      (throw (js/Error. "Divide by zero"))
      (-ratio int/ONE x)))
  Ordered
  (-compare [x y] (cljs/- (-compare-to-integer y x)))
  CompareToNative
  (-compare-to-native [x y]
    (-compare-to-integer x (native->integer y)))
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

(extend-type number
  Add
  (-add [x y] (-add-with-native y x))
  AddWithNative
  (-add-with-native [x y]
    (let [z (cljs/+ x y)]
      (if (or (<= MAX_INTEGER z)
              (<= z MIN_INTEGER))
        (-add-with-integer (native->integer x)
                           (native->integer y))
        z)))
  AddWithInteger
  (-add-with-integer [x y]
    (-add-with-integer (native->integer x) y))
  AddWithRatio
  (-add-with-ratio [x y]
    (-add-with-ratio (-ratio x) y))
  Multiply
  (-multiply [x y]
    (-multiply-with-native y x))
  MultiplyWithNative
  (-multiply-with-native [x y]
    (let [z (cljs/* x y)]
      (if (or (<= MAX_INTEGER z)
              (<= z MIN_INTEGER))
        (-multiply-with-integer (native->integer x)
                                (native->integer y))
        z)))
  MultiplyWithInteger
  (-multiply-with-integer [x y]
    (-multiply-with-integer (native->integer x) y))
  MultiplyWithRatio
  (-multiply-with-ratio [x y]
    (-multiply-with-ratio (-ratio x) y))
  Negate
  (-negate [x] (cljs/- x))
  Invert
  (-invert [x]
    (if (cljs/zero? x)
      (throw (js/Error. "Divide by zero"))
      (-ratio 1 x)))
  Ordered
  (-compare [x y] (cljs/- (-compare-to-native y x)))
  CompareToNative
  (-compare-to-native [x y]
    (cljs/compare x y))
  CompareToInteger
  (-compare-to-integer
    [x y]
    (-compare-to-integer (native->integer x) y))
  CompareToRatio
  (-compare-to-ratio
    [x y]
    (-compare-to-ratio (-ratio x) y)))

(def ZERO 0)
(def ONE 1)

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
  (if (native-integer? x)
    (if (native-integer? n)
      (cljs/quot x n)
      (.divide (native->integer x) n))
    (.divide x (cond-> n (native-integer? n) native->integer))))

(defn rem
  [x n]
  {:pre [(integer? x) (integer? n)]}
  (if (native-integer? x)
    (if (native-integer? n)
      (cljs/rem x n)
      (.modulo (native->integer x) n))
    (.modulo x (cond-> n (native-integer? n) native->integer))))

(defn mod
  [x n]
  (let [y (rem x n)]
    (cond-> y (cljs/neg? (compare y 0))
            (add n))))

(defn odd?
  [n]
  (if (goog-integer? n)
    (.isOdd n)
    (cljs/odd? n)))

(defn even?
  [n]
  (if (goog-integer? n)
    (not (.isOdd n))
    (cljs/even? n)))
