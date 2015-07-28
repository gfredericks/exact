(ns com.gfredericks.exact-test-main
  (:require [cljs.nodejs :as nodejs]
            [cljs.test :as test :refer-macros [run-tests]]
            [com.gfredericks.exact-test]))

(nodejs/enable-util-print!)

(defn -main []
  (run-tests
    'com.gfredericks.exact-test))

(set! *main-cli-fn* -main)
