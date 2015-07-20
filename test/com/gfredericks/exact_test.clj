(ns com.gfredericks.exact-test
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [com.gfredericks.exact :as exact]))

(def gen-exact
  ;; TODO: embetter
  (gen/one-of [gen/int gen/ratio]))

(defspec associativity-of-addition 1000
  (prop/for-all [x gen-exact
                 y gen-exact
                 z gen-exact]
    (= (exact/+ x (exact/+ y z))
       (exact/+ (exact/+ x y) z))))

(defspec commutativity-of-addition 1000
  (prop/for-all [x gen-exact
                 y gen-exact]
    (= (exact/+ x y) (exact/+ y x))))
