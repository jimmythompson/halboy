(ns halboy.http.cachable-test
  (:use org.httpkit.fake)
  (:require [clojure.test :refer :all]
            [halboy.http.cachable :as cachable-http-client]
            [clojure.core.cache :as cache]
            [halboy.http.protocol :as http]))

(def base-url "https://service.example.com")

(deftest halboy-http
  (testing "cachableHttpClient without cached data"
    (with-fake-http
      [{:url base-url :method :get} {:status 201
                                     :headers {:content-type "application/json"
                                               :server       "org.httpkit.fake"}
                                     :body "{}"}]
      (let [cache-store (cachable-http-client/new-http-client
                          (atom (cache/ttl-cache-factory {} :ttl 2000)))
            client cache-store
            request {:url    base-url
                     :method :get}]
        (is (=
              (http/exchange client request)
              {:body    {}
               :headers {:content-type "application/json"
                         :server       "org.httpkit.fake"}
               :raw     {:body    {}
                         :headers {:content-type "application/json"
                                   :server       "org.httpkit.fake"}
                         :opts    {:as      :text
                                   :headers {"Accept"       "application/hal+json"
                                             "Content-Type" "application/json"}
                                   :method  :get
                                   :url     "https://service.example.com"}
                         :status  201}
               :status  201
               :url     "https://service.example.com"})))))

  (testing "cachableHttpClient with cached data"
    (let [request {:url    base-url
                   :method :get}
          cache (atom (cache/ttl-cache-factory
                        {{:as      :text,
                          :headers {"Content-Type" "application/json",
                                    "Accept"       "application/hal+json"},
                          :url     base-url,
                          :method  :get}
                         {:cached true}}
                        :ttl 2000))
          cached-client (cachable-http-client/new-http-client cache)]
      (is (=
            (http/exchange cached-client request)
            {:raw {:cached true}
             :url nil})))))
