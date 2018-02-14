(defproject halboy "2.0.7"
  :description "a hypermedia parser and navigator"
  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [http-kit "2.2.0"]
                 [cheshire "5.7.1"]]
  :plugins [[lein-eftest "0.4.3"]]
  :profiles {:test {:dependencies [[http-kit.fake "0.2.1"]]}}
  :eftest {:multithread? false})
