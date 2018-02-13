(ns halboy.http.default
  (:require
    [clojure.walk :refer [stringify-keys]]
    [cheshire.core :as json]
    [org.httpkit.client :as http]
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
    {:get    (fn [url request] @(http/get url request))
     :post   (fn [url request] @(http/post url request))
     :put    (fn [url request] @(http/put url request))
     :patch  (fn [url request] @(http/patch url request))
     :delete (fn [url request] @(http/delete url request))}
    [method]))

(defn- with-default-options [m]
  (deep-merge default-http-options m))

(defn- with-json-body [m]
  (update-if-present m [:body] json/generate-string))

(defn- with-transformed-params [m]
  (update-if-present m [:query-params] stringify-keys))

(deftype DefaultHttpClient []
  protocol/HttpClient
  (exchange [_ {:keys [url method] :as request}]
    (let [request (-> request
                      (with-default-options)
                      (with-transformed-params)
                      (with-json-body))
          http-fn (http-method->fn method)]
      (http-fn url request))))

(defn new-http-client []
  (DefaultHttpClient.))