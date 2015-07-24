(ns com.gfredericks.exact-test
  (:require [#?(:clj clojure.test.check.clojure-test
                :cljs cljs.test.check.cljs-test)
             #?(:clj :refer :cljs :refer-macros)
             [defspec]]
            [#?(:clj clojure.test.check.generators
                :cljs cljs.test.check.generators) :as gen]
            [#?(:clj clojure.test.check.properties
                :cljs cljs.test.check.properties) :as prop]
            #?@(:cljs [[com.gfredericks.exact.impl :as impl]
                       [cljs.test.check]])
            [com.gfredericks.exact :as exact]))

(def gen-exact
  ;; TODO: embetter
  #?(:clj
     (gen/one-of [gen/int gen/ratio])

     :cljs
     (gen/fmap (comp impl/hacky-bigint str)
               gen/int)))

(defspec associativity-of-addition 1000
  (prop/for-all [x gen-exact
                 y gen-exact
                 z gen-exact]
    (exact/= (exact/+ x (exact/+ y z))
             (exact/+ (exact/+ x y) z))))

(defspec commutativity-of-addition 1000
  (prop/for-all [x gen-exact
                 y gen-exact]
    (exact/= (exact/+ x y) (exact/+ y x))))
