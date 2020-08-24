(ns halboy.http.default
  (:require
    [clojure.walk :refer [stringify-keys]]
    [org.httpkit.client :as http]
    [halboy.argutils :refer [deep-merge]]
    [halboy.http.protocol :as protocol]
    [halboy.http.utils :as utils]))

(def default-http-options
  {:as      :text
   :headers {"Content-Type" "application/json"
             "Accept"       "application/hal+json"}})

(defn http-method->fn [method]
  (get-in
    {:head   http/head
     :get    http/get
     :post   http/post
     :put    http/put
     :patch  http/patch
     :delete http/delete}
    [method]))

(defn- with-default-options [m]
  (deep-merge default-http-options m))

(defn- with-transformed-params [m]
  (utils/update-if-present m [:query-params] stringify-keys))

(defn- format-for-halboy [response]
  (merge
    (select-keys response [:error :body :headers :status])
    {:url (get-in response [:opts :url])
     :raw response}))

(deftype DefaultHttpClient []
  protocol/HttpClient
  (exchange [_ {:keys [url method] :as request}]
    (let [request (-> request
                      (with-default-options)
                      (with-transformed-params)
                      (utils/with-json-body))
          http-fn (http-method->fn method)]
      (-> @(http-fn url request)
          (utils/parse-json-response)
          (format-for-halboy)))))

(defn new-http-client []
  (DefaultHttpClient.))
