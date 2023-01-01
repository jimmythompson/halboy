(defproject halboy "6.0.0"
  :description "a hypermedia parser and navigator"
  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  :url "https://github.com/jimmythompson/halboy"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/core.rrb-vector "0.1.2"]
                 [http-kit "2.6.0"]
                 [cheshire "5.11.0"]
                 [medley "1.4.0"]
                 [uritemplate-clj "1.3.1"]
                 [org.clojure/core.cache "1.0.225"]
                 [org.bovinegenius/exploding-fish "0.3.6"]
                 [clj-http "3.10.2"]]
  :plugins [[lein-eftest "0.5.2"]]
  :profiles {:shared {:dependencies [[nrepl "1.0.0"]]}
             :test   [:shared {:dependencies [[http-kit.fake "0.2.2"]
                                              [clj-http-fake "1.0.3"]
                                              [eftest "0.6.0"]]}]}
  :eftest {:multithread? false})
