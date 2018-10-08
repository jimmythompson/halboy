(ns halboy.http.cachable
  (:require
    [clojure.walk :refer [stringify-keys keywordize-keys]]
    [cheshire.core :as json]
    [org.httpkit.client :as http]
    [clojure.core.cache :as cache]
    [halboy.argutils :refer [deep-merge]]
    [halboy.http.protocol :as protocol]))

(def default-http-options
  {:as      :text
   :headers {"Content-Type" "application/json"
             "Accept"       "application/hal+json"}})

(defn- update-if-present [m ks fn]
  (if (get-in m ks)
    (update-in m ks #(fn %))
    m))

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

(defn- with-json-body [m]
  (update-if-present m [:body] json/generate-string))

(defn- parse-json-response [response]
  (update-if-present
    response [:body]
    #(-> (json/parse-string %)
       (keywordize-keys))))

(defn- with-transformed-params [m]
  (update-if-present m [:query-params] stringify-keys))

(defn- format-for-halboy [response]
  (merge
    (select-keys response [:body :headers :status])
    {:url (get-in response [:opts :url])
     :raw response}))


(deftype CachableHttpClient [cache-store]
  protocol/HttpClient
  (exchange [_ {:keys [url method] :as request}]
    (let [request (-> request
                    (with-default-options)
                    (with-transformed-params)
                    (with-json-body))
          http-fn (http-method->fn method)
          response (if (cache/has? @cache-store request)
                     (get (cache/hit @cache-store request) request)
                     (let [updated-cache (swap! cache-store #(cache/miss % request
                                                               @(http-fn url request)))]
                       (get updated-cache request))
                     )
          ]

      (-> response
        (parse-json-response)
        (format-for-halboy)))))

(defn new-http-client
  ([]
   (new-http-client (atom (cache/ttl-cache-factory {} :ttl 2000))))
  ([cache-store]
     (CachableHttpClient. cache-store)))