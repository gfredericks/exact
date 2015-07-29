(defproject com.gfredericks/exact "0.1.0"
  :description "Portable exact arithmetic in Clojure"
  :url "https://github.com/gfredericks/exact"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3308"]]

  ;; temporary hack until https://github.com/technomancy/leiningen/issues/1940
  ;; is fixed
  :aliases {"test" ["test" "com.gfredericks.exact-test"]}

  :plugins [[lein-cljsbuild "1.0.6"]]
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
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.8.0-RC1"]]}}
  :deploy-repositories [["releases" :clojars]])
