(defproject com.gfredericks/exact "0.1.10"
  :description "Portable exact arithmetic in Clojure"
  :url "https://github.com/gfredericks/exact"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;; cljs version 473 has compile warnings,
                 ;; and with 521 the tests won't run at all
                 [org.clojure/clojurescript "1.9.456"]
                 [com.gfredericks/goog-integer "1.0.1"]]

  :plugins [[lein-cljsbuild "1.1.6"]]
  :cljsbuild
  {:builds
   [{:id "node-dev"
     :source-paths ["src" "test"]
     :notify-command ["node" "resources/run.js"]
     :compiler {:optimizations :none
                :static-fns true
                :target :nodejs
                :output-to "target/cljs/node_dev/tests.js"
                :output-dir "target/cljs/node_dev/out"
                :source-map true}}]}
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}}
  :aliases {"circle-ci"
            ["do"
             ["test"]
             ["cljsbuild" "once"]]}
  :deploy-repositories [["releases" :clojars]])
