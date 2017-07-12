(defproject halboy "0.3.2"
  :description "a hypermedia parser and navigator"
  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]]
  :profiles {:shared {:plugins      [[jonase/eastwood "0.2.3"]]
                      :dependencies [[org.clojure/tools.nrepl "0.2.12"]
                                     [org.clojure/tools.namespace "0.2.11"]
                                     [expectations "2.2.0-beta1"]
                                     [http-kit "2.2.0"]
                                     [cheshire "5.7.1"]]}
             :dev    [:shared {:source-paths ["dev"]
                               :dependencies [[http-kit.fake "0.2.1"]]}]
             :test   [:shared
                      {:plugins [[lein-expectations "0.0.8"]
                                 [lein-autoexpect "1.9.0"]]}]})
