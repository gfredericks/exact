(ns com.gfredericks.exact-test
  (:require [#?(:clj clojure.test.check.clojure-test
                :cljs cljs.test.check.cljs-test)
             #?(:clj :refer :cljs :refer-macros)
             [defspec]]
            [#?(:clj clojure.test.check.generators
                :cljs cljs.test.check.generators) :as gen]
            [#?(:clj clojure.test.check.properties
                :cljs cljs.test.check.properties) :as prop]
            #?(:cljs [cljs.test.check])
            [com.gfredericks.exact :as exact]))

(def digits [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9])

(def gen-integer
  (gen/bind gen/nat
            (fn [digit-count]
              (gen/fmap (fn [[f the-digits]]
                          (f (exact/->integer (apply str the-digits))))
                        (gen/tuple (gen/elements [exact/+ exact/-])
                                   (gen/vector (gen/elements digits) (inc digit-count)))))))

(def gen-ratio
  (gen/fmap (fn [[n d]] (exact// n d))
            (gen/tuple gen-integer
                       (gen/such-that (complement exact/zero?) gen-integer))))

(def gen-exact
  (gen/one-of [gen-integer gen-ratio]))

(defspec associativity-of-addition 100
  (prop/for-all [x gen-exact
                 y gen-exact
                 z gen-exact]
    (= (exact/+ x (exact/+ y z))
       (exact/+ (exact/+ x y) z))))

(defspec commutativity-of-addition 100
  (prop/for-all [x gen-exact
                 y gen-exact]
    (= (exact/+ x y) (exact/+ y x))))

(defspec associativity-of-multiplication 100
  (prop/for-all [x gen-exact
                 y gen-exact
                 z gen-exact]
    (= (exact/* x (exact/* y z))
       (exact/* (exact/* x y) z))))

(defspec commutativity-of-multiplication 100
  (prop/for-all [x gen-exact
                 y gen-exact]
    (= (exact/* x y) (exact/* y x))))

(defspec inc-is-always-different 100
  (prop/for-all [x gen-exact] (-> x exact/inc (not= x))))

(defspec dec-is-always-different 100
  (prop/for-all [x gen-exact] (-> x exact/dec (not= x))))

(defspec inc-dec-is-identity 100
  (prop/for-all [x gen-exact] (-> x exact/inc exact/dec (= x))))

(defspec dec-inc-is-identity 100
  (prop/for-all [x gen-exact] (-> x exact/dec exact/inc (= x))))

(defspec inc-is-greater 100
  (prop/for-all [x gen-exact] (exact/< x (exact/inc x))))

(defspec dec-is-lesser 100
  (prop/for-all [x gen-exact] (exact/< (exact/dec x) x)))
