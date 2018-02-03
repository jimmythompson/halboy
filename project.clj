(defproject halboy "2.0.4"
  :description "a hypermedia parser and navigator"
  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [http-kit "2.2.0"]
                 [cheshire "5.7.1"]]
  :profiles {:test {:dependencies [[http-kit.fake "0.2.1"]]}})
