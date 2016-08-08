(ns com.gfredericks.exact-test
  (:require [#?@(:clj
                 [clojure.test :refer]
                 :cljs
                 [cljs.test :refer-macros])
             [deftest is]]
            [clojure.test.check.clojure-test
             #?(:clj :refer :cljs :refer-macros)
             [defspec]]
            [clojure.test.check.generators :as gen #?@(:cljs [:include-macros true])]
            [clojure.test.check.properties :as prop #?@(:cljs [:include-macros true])]
            #?(:cljs [clojure.test.check])
            [com.gfredericks.exact :as exact]))

(def digits [\0 \1 \2 \3 \4 \5 \6 \7 \8 \9 \a \b \c \d \e \f])

(defn gen-integer-with-digits
  [base]
  (gen/let [digit-count (gen/scale #(* 2 %) gen/nat)

            [f the-digits]
            (gen/tuple (gen/elements [exact/+ exact/-])
                       (gen/vector (gen/elements (take base digits))
                                   (inc digit-count)))]
    (f (exact/string->integer (apply str the-digits) base))))

(def word-max (apply * (repeat 32 2)))

(def gen-word
  (gen/let [[f x]
            (gen/tuple (gen/elements [identity
                                      #(- word-max % 1)])
                       (gen/large-integer* {:min 0 :max (dec word-max)}))]
    (exact/native->integer (f x))))

;; generating with 32 bit words to make it more likely to catch
;; js impl edge cases
(def gen-integer-with-words*
  (let [word-max (exact/native->integer word-max)]
    (gen/let [word-count (gen/scale #(max % 10) gen/nat)
              words (gen/vector gen-word word-count)]
      (->> words
           (reduce (fn [acc word]
                     (-> acc
                         (exact/* word-max)
                         (exact/+ word)))
                   exact/ZERO)))))

(def gen-integer-with-words
  (gen/let [[x f] (gen/tuple gen-integer-with-words*
                             (gen/elements [exact/+ exact/-]))]
    (f x)))

(def gen-integer
  (gen/one-of [(gen-integer-with-digits 10)
               (gen-integer-with-digits 16)
               gen-integer-with-words]))

(def gen-integer-nonzero
  (gen/such-that (complement exact/zero?) gen-integer))

(def gen-ratio
  (gen/let [[n d] (gen/tuple gen-integer gen-integer-nonzero)]
    (exact// n d)))

(def gen-exact
  (gen/one-of [gen-integer gen-ratio]))

(def gen-exact-nonzero
  (gen/such-that (complement exact/zero?) gen-exact))

(defspec multiplication-distributes-over-addition 50
  (prop/for-all [x gen-exact
                 y gen-exact
                 z gen-exact]
    (= (exact/* x (exact/+ y z))
       (exact/+ (exact/* x y) (exact/* x z)))))

(defspec integer-multiplication-distributes-over-addition 1000
  ;; using integers makes this faster so we can run it more times
  (prop/for-all [x gen-integer
                 y gen-integer
                 z gen-integer]
    (= (exact/* x (exact/+ y z))
       (exact/+ (exact/* x y) (exact/* x z)))))

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

(defspec integer-serialization-roundtrip 200
  (prop/for-all [x gen-integer
                 radix (gen/choose 2 36)]
    (-> x
        (exact/integer->string radix)
        (exact/string->integer radix)
        (= x))))

(defspec quot-and-rem 200
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

(defspec everything-is-even-or-odd 100
  (prop/for-all [x gen-integer]
    (or (exact/even? x)
        (exact/odd? x))))

(defspec plus-one-inverts-parity 100
  (prop/for-all [x gen-integer]
    (let [x1 (exact/inc x)]
      (not= (exact/even? x)
            (exact/even? x1)))))

(defspec plus-two-preserves-parity 100
  (prop/for-all [x gen-integer]
    (let [x1 (-> x exact/inc exact/inc)]
      (= (exact/even? x)
         (exact/even? x1)))))

(defspec native-conversion 100
  (prop/for-all [num (gen/choose -1000000000000
                                 1000000000000)]
    (let [n (exact/native->integer num)]
      (and (exact/integer? n)
           (= (str num) (exact/integer->string n))
           (= num (exact/integer->native n))))))

(defspec IComparable-impl-works 100
  (prop/for-all [xs (gen/list gen-exact)]
    (->> xs
         (sort)
         (partition 2 1)
         (every? (fn [[x y]]
                   (exact/<= x y))))))

(defspec =-is-reflexive 100
  (prop/for-all [x gen-exact]
    (= x x)))

(defspec =-is-symmetric 100
  (prop/for-all [x gen-exact
                 y gen-exact]
    (= (= x y) (= y x))))

(defspec zero-pos-neg-are-disjoint-and-complete 100
  (prop/for-all [x gen-exact]
    (= 1 (+ (if (exact/neg? x) 1 0)
            (if (exact/zero? x) 1 0)
            (if (exact/pos? x) 1 0)))))

(def gen-unique-numbers-via-group-by
  (gen/let [xs (gen/not-empty (gen/list gen-exact))]
    (->> xs
         (group-by identity)
         (vals)
         (map first))))

(defspec work-correctly-as-map-keys 50
  (prop/for-all [xs gen-unique-numbers-via-group-by]
    (apply distinct? (map str xs))))

(deftest closure-bug-703
  (is (= "eea1c478f4683b323f0953c9c8e067e3967d97e7ed0bf05862cecac60f30077f170e480beee2cd0c1d5516764d58bc260cafe5705bc6b6df63cf4c057cb9f090"
         (let [b (exact/string->integer
                  "f729d763a14ecd55ffffebab43f388d0f7cbae584d3765d509b5557d6048ea0c"
                  16)]
           (exact/integer->string (exact/* b b) 16)))))
