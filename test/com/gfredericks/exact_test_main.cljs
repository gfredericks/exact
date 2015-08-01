(ns com.gfredericks.exact-test-main
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :as test :refer-macros [run-tests]]
            [com.gfredericks.exact-test]

            [com.gfredericks.goog.math.Integer :as int]))

(nodejs/enable-util-print!)

(defn -main []

  ;; proving that the embedded JS works
  (prn (int/fromString "2A" 16))

  #_
  (run-tests
    'com.gfredericks.exact-test))

(set! *main-cli-fn* -main)
