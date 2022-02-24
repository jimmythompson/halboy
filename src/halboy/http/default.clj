(ns halboy.http.default
  (:require
    [clojure.walk :refer [stringify-keys]]
    [org.httpkit.client :as http]
    [halboy.argutils :refer [deep-merge]]
    [halboy.data :refer [update-if-present]]
    [halboy.http.protocol :as protocol]
    [halboy.types :as types]))

(def default-http-options
  {:as      :stream
   :headers {"Content-Type" "application/json"
             "Accept"       "application/hal+json"}})

(defn- with-default-options [m]
  (deep-merge default-http-options m))

(defn- with-transformed-params [m]
  (update-if-present m [:query-params] stringify-keys))

(defn- format-for-halboy [response]
  (merge
    (select-keys response [:error :body :headers :status])
    {:url (get-in response [:opts :url])
     :raw response}))

(deftype DefaultHttpClient []
  protocol/HttpClient
  (exchange [_ request]
    (let [request (-> request
                      (with-default-options)
                      (with-transformed-params)
                      (types/if-json-encode-body))]
      (-> @(http/request request)
          (types/coerce-response-type)
          (format-for-halboy)))))

(defn new-http-client []
  (DefaultHttpClient.))
