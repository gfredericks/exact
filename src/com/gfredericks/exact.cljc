(ns com.gfredericks.exact
  (:refer-clojure :exclude [+ - * / =])
  (:require [#?(:clj clojure.core :cljs cljs.core) :as core]
            [com.gfredericks.exact.impl :as impl]))

(defn +
  ([] impl/additive-identity)
  ([x] x)
  ([x y] (impl/add x y))
  ([x y & zs] (reduce impl/add (impl/add x y) zs)))

(defn -
  ([x] (impl/negate x))
  ([x y] (impl/add x (impl/negate y)))
  ([x y & zs] (impl/add x (impl/negate (reduce impl/add y zs)))))

(defn =
  ([x] true)
  ([x y] (impl/= x y))
  ([x y & zs] (and (impl/= x y)
                   (every? #(impl/= y %) zs))))
