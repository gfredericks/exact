(ns com.gfredericks.exact.impl
  "clj-jvm impl."
  (:refer-clojure :exclude [= compare numerator denominator integer? ratio?
                            quot rem mod])
  (:import java.math.BigInteger))

(defn ^:private exact?
  [x]
  (or (instance? Long x)
      (instance? clojure.lang.BigInt x)
      (instance? clojure.lang.Ratio x)))

(defmacro validate-exact
  [arg]
  (when *assert*
    `(let [arg# ~arg]
       (when-not (or (instance? Long arg#)
                     (instance? clojure.lang.BigInt arg#)
                     (instance? clojure.lang.Ratio arg#))
         (throw (ex-info "Bad argument type!"
                         {:arg arg#}))))))

(def ^:const ZERO 0)
(def ^:const ONE 1)

(defn add
  [x y]
  (validate-exact x)
  (validate-exact y)
  (+' x y))

(defn negate
  [x]
  (validate-exact x)
  (-' x))

(defn =
  [x y]
  (validate-exact x)
  (validate-exact y)
  (clojure.core/= x y))

(defn multiply
  [x y]
  (validate-exact x)
  (validate-exact y)
  (*' x y))

(defn invert
  [x]
  (validate-exact x)
  (/ x))

(defn compare
  [x y]
  (validate-exact x)
  (validate-exact y)
  (clojure.core/compare x y))

(defn string->integer
  [^String s radix]
  (bigint (BigInteger. s (int radix))))

(defn integer->string
  [n radix]
  (.toString (biginteger n) radix))

(def numerator (comp bigint clojure.core/numerator))
(def denominator (comp bigint clojure.core/denominator))
(def integer? clojure.core/integer?)
(def ratio? clojure.core/ratio?)
(def quot clojure.core/quot)
(def mod clojure.core/mod)
(def rem clojure.core/rem)

(defn native-integer?
  [n]
  (instance? Long n))

(defn native->integer
  [num]
  {:pre [(native-integer? num)]}
  num)

(defn integer->native
  [x]
  {:pre [(exact? x)
         (<= Long/MIN_VALUE x Long/MAX_VALUE)]}
  (long x))
