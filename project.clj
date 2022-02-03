(defproject halboy "5.1.1"
  :description "a hypermedia parser and navigator"
  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  :url "https://github.com/jimmythompson/halboy"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [http-kit "2.5.3"]
                 [cheshire "5.10.2"]
                 [medley "1.3.0"]
                 [uritemplate-clj "1.3.1"]
                 [org.clojure/core.cache "1.0.225"]
                 [org.bovinegenius/exploding-fish "0.3.6"]]
  :plugins [[lein-eftest "0.5.9"]]
  :profiles {:shared {:dependencies [[nrepl "0.9.0"]]}
             :test   [:shared {:dependencies [[http-kit.fake "0.2.2"]
                                              [eftest "0.5.9"]]}]}
  :eftest {:multithread? false})
