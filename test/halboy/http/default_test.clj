(ns halboy.http.default-test
  (:use org.httpkit.fake)
  (:require [clojure.test :refer :all]
            [halboy.http.default :as http-client]
            [halboy.http.protocol :as http]))

(def base-url "https://service.example.com")

(deftest halboy-http
  (testing "defaultHttpClient HTML Content-Type header"
    (with-fake-http
      [{:url base-url :method :get} {:status 201
                                     :headers {:content-type "text/html"
                                               :server       "org.httpkit.fake"}
                                     :body "{}"}]
      (let [client (http-client/new-http-client)
            request {:url    base-url
                     :method :get}]
        (is (=
              (http/exchange client request)
              {:body    "{}"
               :headers {:content-type "text/html"
                         :server       "org.httpkit.fake"}
               :raw     {:body    "{}"
                         :headers {:content-type "text/html"
                                   :server       "org.httpkit.fake"}
                         :opts    {:as      :stream
                                   :headers {"Accept"       "application/hal+json"
                                             "Content-Type" "application/json"}
                                   :method  :get
                                   :url     "https://service.example.com"}
                         :status  201}
               :status  201
               :url     "https://service.example.com"})))))

  (testing "defaultHttpClient JSON Content-Type header"
    (with-fake-http
      [{:url base-url :method :get} {:status 201
                                     :headers {:content-type "application/json"
                                               :server       "org.httpkit.fake"}
                                     :body "{}"}]
      (let [client (http-client/new-http-client)
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
                         :opts    {:as      :stream
                                   :headers {"Accept"       "application/hal+json"
                                             "Content-Type" "application/json"}
                                   :method  :get
                                   :url     "https://service.example.com"}
                         :status  201}
               :status  201
               :url     "https://service.example.com"}))))))
