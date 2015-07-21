(ns com.gfredericks.exact.impl
  "clj-jvm impl."
  (:refer-clojure :exclude [=]))

(defn ^:private exact?
  [x]
  (or (instance? Long x)
      (instance? clojure.lang.BigInt x)
      (instance? clojure.lang.Ratio x)))

(def additive-identity 0)

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
