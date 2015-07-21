(ns com.gfredericks.exact-test-main
  (:require [clojure.string :as string]
            [cljs.test]
            [com.gfredericks.exact-test]))

(cljs.test/deftest holle
  (cljs.test/is (= 41 (* 2 3 7))))

(defn ^:export -main
  []
  (cljs.test/run-tests 'com.gfredericks.exact-test)
  )

(set! *print-fn* #(.log js/console (string/trim %)))
(set! cljs.core/*main-cli-fn* -main)
