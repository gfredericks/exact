(defproject com.gfredericks/exact "0.1.11-SNAPSHOT"
  :description "Portable exact arithmetic in Clojure"
  :url "https://github.com/gfredericks/exact"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.671"]
                 ;; need at least this version to avoid a couple
                 ;; Integer bugs
                 [org.clojure/google-closure-library "0.0-20170519-fa0499ef"]]
  :plugins [[lein-cljsbuild "1.1.6"]]
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
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}}
  :aliases {"circle-ci"
            ["do"
             ["test"]
             ["cljsbuild" "once"]]}
  :deploy-repositories [["releases" :clojars]])
