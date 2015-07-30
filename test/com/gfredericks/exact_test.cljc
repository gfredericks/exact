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
                          (f (exact/string->integer (apply str the-digits))))
                        (gen/tuple (gen/elements [exact/+ exact/-])
                                   (gen/vector (gen/elements digits) (inc digit-count)))))))

(def gen-integer-nonzero
  (gen/such-that (complement exact/zero?) gen-integer))

(def gen-ratio
  (gen/fmap (fn [[n d]] (exact// n d))
            (gen/tuple gen-integer gen-integer-nonzero)))

(def gen-exact
  (gen/one-of [gen-integer gen-ratio]))

(def gen-exact-nonzero
  (gen/such-that (complement exact/zero?) gen-exact))

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

(defspec x-plus-minus-x-is-zero 100
  (prop/for-all [x gen-exact]
    (-> x exact/- (exact/+ x) (= exact/ZERO))))

(defspec x-times-one-over-x-is-one 100
  (prop/for-all [x gen-exact-nonzero]
    (-> x exact// (exact/* x) (= exact/ONE))))

(defspec x-times-x-is-non-negative 100
  (prop/for-all [x gen-exact]
    (-> x (exact/* x) (exact/neg?) (not))))

(defspec numerator-works 100
  (prop/for-all [x gen-integer-nonzero]
    (let [x (-> x exact/abs exact/inc)]
      (-> (exact// (exact/inc x) x)
          (exact/numerator)
          (= (exact/inc x))))))

(defspec denominator-works 100
  (prop/for-all [x gen-integer-nonzero]
    (let [x (-> x exact/abs exact/inc)]
      (-> (exact// (exact/inc x) x)
          (exact/denominator)
          (= x)))))

(defspec integer-and-ratio-work 100
  (prop/for-all [x gen-exact]
    (let [b1 (exact/integer? x)
          b2 (exact/ratio? x)]
      (and (or b1 b2)
           (not (and b1 b2))))))

(defspec num-and-dem-return-integers 100
  (prop/for-all [x gen-exact]
    (or (exact/integer? x)
        (and (-> x exact/numerator exact/integer?)
             (-> x exact/denominator exact/integer?)))))

(defspec integer-serialization-roundtrip 100
  (prop/for-all [x gen-integer
                 ;; Using max radix of 35 because of
                 ;; https://github.com/google/closure-library/pull/498
                 radix (gen/choose 2 35)]
    (-> x
        (exact/integer->string radix)
        (exact/string->integer radix)
        (= x))))

(defspec quot-and-rem 100
  (prop/for-all [x gen-integer
                 n (gen/fmap exact/abs gen-integer-nonzero)]
    (let [the-quot (exact/quot x n)
          the-rem (exact/rem x n)]
      (= x (exact/+ the-rem (exact/* the-quot n))))))

(defspec mod-never-negative 100
  (prop/for-all [x gen-integer
                 n (gen/fmap exact/abs gen-integer-nonzero)]
    (exact/<= exact/ZERO
              (exact/mod x n)
              (exact/dec n))))
