(defproject halboy "5.1.0"
  :description "a hypermedia parser and navigator"
  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [http-kit "2.3.0"]
                 [cheshire "5.8.0"]
                 [medley "1.0.0"]
                 [uritemplate-clj "1.2.1"]
                 [org.clojure/core.cache "0.7.1"]
                 [org.bovinegenius/exploding-fish "0.3.6"]]
  :plugins [[lein-eftest "0.5.2"]]
  :profiles {:shared {:dependencies [[nrepl "0.6.0"]]}
             :test   [:shared {:dependencies [[http-kit.fake "0.2.2"]
                                              [eftest "0.5.2"]]}]}
  :eftest {:multithread? false})
