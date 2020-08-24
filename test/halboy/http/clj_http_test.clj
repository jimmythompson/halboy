(ns halboy.http.clj-http-test

  (:require [clojure.test :refer :all]
            [halboy.http.protocol :as http]
            [halboy.http.clj-http :as http-client]
            [halboy.resource :as hal]
            [cheshire.core :as json]
            [clj-http.util :as http-util])
  (:use clj-http.fake))

(deftest halboy-clj-http-test
  (let [base-url "https://service.example.com"
        test-resource (-> (hal/new-resource)
                        (hal/add-property :test "test"))]

    (testing "GET with content-type: text/html response - should still parse response"
      (with-fake-routes-in-isolation
        {base-url {:get (fn [_] {:status  201
                                 :body    "{}"
                                 :headers {:content-type "text/html"
                                           :server       "clj-http.fake"}})}}

        (let [client (http-client/new-http-client)
              request {:url    base-url
                       :method :get}
              response (http/exchange client request)]
          (is (=
                (update-in response [:raw] dissoc :request-time)
                {:body    {}
                 :headers {:content-type "text/html"
                           :server       "clj-http.fake"}

                 :raw     {:body                  {}
                           :headers               {:content-type "text/html"
                                                   :server       "clj-http.fake"}
                           :opts                  {:as      :text
                                                   :headers {"Accept"       "application/hal+json"
                                                             "Content-Type" "application/json"}
                                                   :method  :get
                                                   :url     "https://service.example.com"}
                           :orig-content-encoding nil
                           :status                201}
                 :status  201
                 :url     "https://service.example.com"})))))


    (testing "GET with content-type: application/json response"
      (let [expected-response-body {:embedded   {}
                                    :links      {}
                                    :properties {:test "test"}}
            expected-response-headers {:content-type "application/json"
                                       :server       "clj-http.fake"}]
        (with-fake-routes-in-isolation
          {base-url {:get (fn [_] {:status  201
                                   :body    (json/generate-string test-resource)
                                   :headers {:content-type "application/json"
                                             :server       "clj-http.fake"}})}}

          (let [client (http-client/new-http-client)
                request {:url    base-url
                         :method :get}
                response (http/exchange client request)]
            (is (= (update-in response [:raw] dissoc :request-time)
                  {:body    expected-response-body
                   :headers expected-response-headers

                   :raw     {:body                  {:embedded   {}
                                                     :links      {}
                                                     :properties {:test "test"}}
                             :headers               {:content-type "application/json"
                                                     :server       "clj-http.fake"}
                             :opts                  {:as      :text
                                                     :headers {"Accept"       "application/hal+json"
                                                               "Content-Type" "application/json"}
                                                     :method  :get
                                                     :url     "https://service.example.com"}
                             :orig-content-encoding nil
                             :status                201}
                   :status  201
                   :url     "https://service.example.com"}))))))

    (testing "GET with query params and content-type: application/json response"
      (let [expected-response-body {:embedded   {}
                                    :links      {}
                                    :properties {:test "test"}}
            expected-response-headers {:content-type "application/json"
                                       :server       "clj-http.fake"}]
        (with-fake-routes-in-isolation
          {{:address      base-url
            :query-params {:user "123"}}
           (fn [_]
             {:status  201
              :body    (json/generate-string test-resource)
              :headers {:content-type "application/json"
                        :server       "clj-http.fake"}})}

          (let [client (http-client/new-http-client)
                request {:url          base-url
                         :query-params {:user 123}
                         :method       :get}
                response (http/exchange client request)]
            (is (= (update-in response [:raw] dissoc :request-time)
                  {:body    expected-response-body
                   :headers expected-response-headers

                   :raw     {:body                  {:embedded   {}
                                                     :links      {}
                                                     :properties {:test "test"}}
                             :headers               {:content-type "application/json"
                                                     :server       "clj-http.fake"}
                             :opts                  {:as      :text
                                                     :query-params {:user 123}
                                                     :headers {"Accept"       "application/hal+json"
                                                               "Content-Type" "application/json"}
                                                     :method  :get
                                                     :url     "https://service.example.com"}
                             :orig-content-encoding nil
                             :status                201}
                   :status  201
                   :url     "https://service.example.com"}))))))

    (testing "POST with content-type application/json"
      (let [expected-request-body (json/generate-string test-resource)
            expected-request-headers {"Accept"          "application/hal+json"
                                      "Content-Type"    "application/json"
                                      "accept-encoding" "gzip, deflate"}]
        (with-fake-routes-in-isolation
          {base-url {:post
                     (fn [req]
                       (when (and
                               (= expected-request-body
                                 (http-util/force-string (:body req) "utf-8"))
                               (= expected-request-headers
                                 (:headers req)))

                         {:status  201
                          :body    (json/generate-string
                                     (-> (hal/new-resource)
                                       (hal/add-property :test "test")))
                          :headers {:content-type "application/json"
                                    :server       "clj-http.fake"}}))}}

          (let [client (http-client/new-http-client)
                request {:url    base-url
                         :body   (-> (hal/new-resource)
                                   (hal/add-property :test "test"))
                         :method :post}
                response (http/exchange client request)]

            (is (= (update-in response [:raw] dissoc :request-time)
                  {:body    {:embedded   {}
                             :links      {}
                             :properties {:test "test"}}
                   :headers {:content-type "application/json"
                             :server       "clj-http.fake"}

                   :raw     {:body                  {:embedded   {}
                                                     :links      {}
                                                     :properties {:test "test"}}
                             :headers               {:content-type "application/json"
                                                     :server       "clj-http.fake"}
                             :opts                  {:as      :text
                                                     :body    (json/generate-string test-resource)
                                                     :headers {"Accept"       "application/hal+json"
                                                               "Content-Type" "application/json"}
                                                     :method  :post
                                                     :url     "https://service.example.com"}
                             :orig-content-encoding nil
                             :status                201}
                   :status  201
                   :url     "https://service.example.com"}))))))))
