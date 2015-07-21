(ns com.gfredericks.exact.impl
  "cljs impl."
  (:refer-clojure :exclude [=])
  (:require [goog.math.Integer :as int]))

;;
;; TODO: full ratio impl
;;

(defn ^:private exact?
  [x]
  (instance? goog.math.Integer x))

(defn hacky-bigint
  ;; can we use literals of some sort instead of this?
  [s]
  (int/fromString s))

(def additive-identity (hacky-bigint "0"))

(defn add
  [x y]
  {:pre [(exact? x) (exact? y)]}
  (.add x y))

(defn negate
  [x]
  {:pre [(exact? x)]}
  (.negate x))

(defn =
  [x y]
  {:pre [(exact? x) (exact? y)]}
  (.equals x y))
