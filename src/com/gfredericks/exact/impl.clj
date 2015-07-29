(ns com.gfredericks.exact.impl
  "clj-jvm impl."
  (:refer-clojure :exclude [= compare numerator denominator])
  (:import java.math.BigInteger))

(defn ^:private exact?
  [x]
  (or (instance? Long x)
      (instance? clojure.lang.BigInt x)
      (instance? clojure.lang.Ratio x)))

(def ^:const ZERO 0)
(def ^:const ONE 1)

(defn add
  [x y]
  {:pre [(exact? x) (exact? y)]}
  (+' x y))

(defn negate
  [x]
  {:pre [(exact? x)]}
  (-' x))

(defn =
  [x y]
  {:pre [(exact? x) (exact? y)]}
  (clojure.core/= x y))

(defn multiply
  [x y]
  {:pre [(exact? x) (exact? y)]}
  (*' x y))

(defn invert
  [x]
  {:pre [(exact? x)]}
  (/ x))

(defn compare
  [x y]
  {:pre [(exact? x) (exact? y)]}
  (clojure.core/compare x y))

(defn ->integer
  [^String s]
  (bigint (BigInteger. s)))

(def numerator clojure.core/numerator)
(def denominator clojure.core/denominator)
