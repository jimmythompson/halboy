(ns halboy.http.cachable
  (:require
    [clojure.walk :refer [stringify-keys]]
    [org.httpkit.client :as http]
    [clojure.core.cache :as cache]
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

(deftype CachableHttpClient [cache-store]
  protocol/HttpClient
  (exchange [_ request]
    (let [request (-> request
                      (with-default-options)
                      (with-transformed-params)
                      (types/if-json-encode-body))
          response (if (cache/has? @cache-store request)
                     (get (cache/hit @cache-store request) request)
                     (let [updated-cache (swap! cache-store #(cache/miss % request
                                                                         @(http/request request)))]
                       (get updated-cache request)))]
      (-> response
          (types/coerce-response-type)
          (format-for-halboy)))))

(defn new-http-client
  ([]
   (new-http-client (atom (cache/ttl-cache-factory {} :ttl 2000))))
  ([cache-store]
   (CachableHttpClient. cache-store)))
