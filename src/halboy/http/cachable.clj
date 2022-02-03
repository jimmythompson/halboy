(ns halboy.http.cachable
  (:require
    [clojure.walk :refer [stringify-keys keywordize-keys]]
    [org.httpkit.client :as http]
    [clojure.core.cache :as cache]
    [halboy.argutils :refer [deep-merge]]
    [halboy.data :refer [update-if-present]]
    [halboy.json :as haljson]
    [halboy.http.protocol :as protocol]))

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
  (update-if-present m [:query-params] stringify-keys))

(defn- format-for-halboy [response]
  (merge
    (select-keys response [:error :body :headers :status])
    {:url (get-in response [:opts :url])
     :raw response}))

(deftype CachableHttpClient [cache-store]
  protocol/HttpClient
  (exchange [_ {:keys [url method] :as request}]
    (let [request (-> request
                      (with-default-options)
                      (with-transformed-params)
                      (haljson/if-json-encode-body))
          http-fn (http-method->fn method)
          response (if (cache/has? @cache-store request)
                     (get (cache/hit @cache-store request) request)
                     (let [updated-cache (swap! cache-store #(cache/miss % request
                                                                         @(http-fn url request)))]
                       (get updated-cache request)))]
      (-> response
          (haljson/if-json-parse-response)
          (format-for-halboy)))))

(defn new-http-client
  ([]
   (new-http-client (atom (cache/ttl-cache-factory {} :ttl 2000))))
  ([cache-store]
   (CachableHttpClient. cache-store)))
