(ns com.gfredericks.exact-test-main
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :as test :refer-macros [run-tests]]
            [com.gfredericks.exact-test :as et]

            [cljs.test.check :refer [quick-check]]
            [cljs.test.check.properties :as prop]
            [com.gfredericks.goog.math.Integer :as int]
            [com.gfredericks.exact :as e]
))

(nodejs/enable-util-print!)

(defn -main []

  #_
  (prn (.slowDivide (e/native->integer 18)
                    (e/native->integer 1)))

  #_
  (prn
   (quick-check 100
                (prop/for-all [x et/gen-integer
                               y et/gen-integer-nonzero]
                  (let [x (e/abs x)
                        y (e/abs y)]
                    (= (.divide x y)
                       (.slowDivide x y))))))

  ;; I think this pretty well proves we wrote it wrong
  #_(prn (int/fromString (apply str (repeat 400 "7"))))


  (run-tests
    'com.gfredericks.exact-test))

(set! *main-cli-fn* -main)
