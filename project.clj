(defproject halboy "0.1.0-SNAPSHOT"
  :description "a hypermedia parser and navigator"
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [halresource "0.2.0-20141122.033800-1"]]
  :profiles {:shared {:plugins [[jonase/eastwood "0.2.3"]]
                      :dependencies [[org.clojure/tools.nrepl "0.2.12"]
                                     [org.clojure/tools.namespace "0.2.11"]
                                     [expectations "2.2.0-beta1"]]}
             :dev    [:shared {:source-paths ["dev"]}]
             :test   [:shared
                      {:plugins      [[lein-expectations "0.0.8"]
                                      [lein-autoexpect "1.9.0"]]}]})
