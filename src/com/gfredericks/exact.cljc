(ns com.gfredericks.exact
  (:refer-clojure :exclude [+ - * / = < > <= >= zero? inc dec
                            min max min-key max-key pos? neg?])
  (:require [#?(:clj clojure.core :cljs cljs.core) :as core]
            [com.gfredericks.exact.impl :as impl]))

(def ZERO impl/ZERO)
(def ONE impl/ONE)

(defn +
  ([] impl/ZERO)
  ([x] x)
  ([x y] (impl/add x y))
  ([x y & zs] (reduce impl/add (impl/add x y) zs)))

(defn -
  ([x] (impl/negate x))
  ([x y] (impl/add x (impl/negate y)))
  ([x y & zs] (impl/add x (impl/negate (reduce impl/add y zs)))))

(defn =
  ([x] true)
  ([x y] (core/zero? (impl/compare x y)))
  ([x y & zs] (and (= x y)
                   (every? #(= y %) zs))))

(defn *
  ([] impl/ONE)
  ([x] x)
  ([x y] (impl/multiply x y))
  ([x y & zs] (reduce impl/multiply (impl/multiply x y) zs)))

(defn /
  ([x] (impl/invert x))
  ([x y] (impl/multiply x (impl/invert y)))
  ([x y & zs] (impl/multiply x (impl/invert (reduce impl/multiply y zs)))))

(defn ->integer
  [s]
  (impl/->integer s))

(defn zero?
  [x]
  (= x impl/ZERO))

(defn inc [x] (+ x impl/ONE))
(defn dec [x] (- x impl/ONE))

(defn <
  ([x] true)
  ([x y] (core/neg? (impl/compare x y)))
  ([x y & more]
   (if (< x y)
     (if (next more)
       (recur y (first more) (next more))
       (< y (first more)))
     false)))

(defn >
  ([x] true)
  ([x y] (core/pos? (impl/compare x y)))
  ([x y & more]
   (if (> x y)
     (if (next more)
       (recur y (first more) (next more))
       (> y (first more)))
     false)))

(defn <=
  ([x] true)
  ([x y] (not (core/pos? (impl/compare x y))))
  ([x y & more]
   (if (<= x y)
     (if (next more)
       (recur y (first more) (next more))
       (<= y (first more)))
     false)))

(defn >=
  ([x] true)
  ([x y] (not (core/neg? (impl/compare x y))))
  ([x y & more]
   (if (>= x y)
     (if (next more)
       (recur y (first more) (next more))
       (>= y (first more)))
     false)))

(defn max
  ([x] x)
  ([x y] (if (> x y) x y))
  ([x y & more]
     (reduce max (max x y) more)))

(defn min
  ([x] x)
  ([x y] (if (< x y) x y))
  ([x y & more]
     (reduce min (min x y) more)))

(defn min-key
  ([k x] x)
  ([k x y] (if (< (k x) (k y)) x y))
  ([k x y & more]
   (reduce #(min-key k %1 %2) (min-key k x y) more)))

(defn max-key
  ([k x] x)
  ([k x y] (if (> (k x) (k y)) x y))
  ([k x y & more]
   (reduce #(max-key k %1 %2) (max-key k x y) more)))

(defn pos? [x] (< ZERO x))
(defn neg? [x] (< x ZERO))
