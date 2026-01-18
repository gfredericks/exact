(defproject com.gfredericks/exact "0.1.12-SNAPSHOT"
  :description "Portable exact arithmetic in Clojure"
  :url "https://github.com/gfredericks/exact"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.12.4"]
                 [org.clojure/clojurescript "1.12.134"]
                 ;; need at least this version to avoid a couple
                 ;; Integer bugs
                 [org.clojure/google-closure-library "0.0-20250515-87401eb8"]]
  :plugins [[lein-cljsbuild "1.1.8"]]
  :cljsbuild
  {:builds
   [{:id "node-dev"
     :source-paths ["src" "test"]
     :notify-command ["node" "target/cljs/node_dev/tests.js"]
     :compiler {:optimizations :none
                :static-fns true
                :target :nodejs
                :output-to "target/cljs/node_dev/tests.js"
                :output-dir "target/cljs/node_dev/out"
                :main com.gfredericks.exact-test-main
                :source-map true}}]}
  :profiles {:dev {:dependencies [[org.clojure/test.check "1.1.3"]]}}
  :aliases {"circle-ci"
            ["do"
             ["test"]
             ["cljsbuild" "once"]]}
  :deploy-repositories [["releases" :clojars]])
