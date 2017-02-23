(defproject lambdaisland/uri "1.0.0"
  :description "RFC compliant URI library"
  :url "http://github.com/lambdaisland/uri"
  :license {:name "Mozilla Public License 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :plugins [[lein-codox "0.10.3"]
            [lein-doo "0.1.7"]
            [lein-cljsbuild "1.1.5"]]
  :codox {:output-path "gh-pages"}
  :doo {:build "test"
        :alias {:default [:phantom]}}
  :cljsbuild {:builds [{:id "test"
                        :source-paths ["src" "test"]
                        :compiler {:output-to "target/testable.js"
                                   :output-dir "target"
                                   :main lambdaisland.test-runner
                                   :optimizations :none}}]}
  :aliases {"test-all" ["do" "test" ["doo" "phantom" "test" "once"]]})
