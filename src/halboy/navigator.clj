(ns halboy.navigator
  (:refer-clojure :exclude [get])
  (:require [clojure.walk :refer [keywordize-keys stringify-keys]]
            [cheshire.core :as json]
            [halboy.resource :as resource]
            [halboy.data :refer [transform-values]]
            [halboy.json :refer [json->resource resource->json]]
            [halboy.http :refer [GET POST PUT DELETE]]
            [halboy.params :as params])
  (:import (java.net URL)))

(def default-options
  {:follow-redirects true})

(defrecord Navigator [href options response resource])

(defn- resolve-url [url endpoint]
  (-> (URL. url)
      (URL. endpoint)
      (.toString)))

(defn- extract-header [navigator header]
  (get-in navigator [:response :headers header]))

(defn- extract-redirect-location [navigator]
  (let [base-url (:href navigator)
        endpoint (extract-header navigator :location)]
    (resolve-url base-url endpoint)))

(defn- response->Navigator [response options]
  (let [current-url (get-in response [:opts :url])
        resource (-> (:body response)
                     json->resource)]
    (->Navigator current-url options response resource)))

(defn- fetch-url [url params options]
  (let [combined-options (merge default-options options)]
    (-> (GET url {:query-params (stringify-keys params)})
        (response->Navigator combined-options))))

(defn- post-url [url body params options]
  (let [combined-options (merge default-options options)
        post-response (-> (POST url {:body (json/generate-string body)})
                          (response->Navigator options))
        status (get-in post-response [:response :status])]
    (if (-> (= status 201)
            (and (:follow-redirects combined-options)))
      (-> (extract-redirect-location post-response)
          (fetch-url {} options))
      post-response)))

(defn- put-url [url body params options]
  (let [combined-options (merge default-options options)
        put-response (-> (PUT url {:body (json/generate-string body)})
                         (response->Navigator options))
        status (get-in put-response [:response :status])]
    (if (-> (= status 201)
            (and (:follow-redirects combined-options)))
      (-> (extract-redirect-location put-response)
          (fetch-url {} options))
      put-response)))

(defn- delete-url [url options]
  (let [combined-options (merge default-options options)]
    (-> (DELETE url)
        (response->Navigator options))))

(defn- resolve-absolute-href [navigator href]
  (resolve-url (:href navigator) href))

(defn- resolve-link [navigator link params]
  (-> (:resource navigator)
      (resource/get-href link)
      (params/build-query params)))

(defn location
  "Gets the current location of the navigator"
  [navigator]
  (:href navigator))

(defn options
  "Gets the navigation options"
  [navigator]
  (:options navigator))

(defn resource
  "Gets the resource from the navigator"
  [navigator]
  (:resource navigator))

(defn response
  "Gets the last response from the navigator"
  [navigator]
  (:response navigator))

(defn status
  "Gets the status code from the last response from the navigator"
  [navigator]
  (-> (response navigator)
      :status))

(defn discover
  "Starts a conversation with an API. Use this on the discovery endpoint."
  ([href]
   (discover href {}))
  ([href options]
   (fetch-url href {} options)))

(defn get
  "Fetches the contents of a link in an API."
  ([navigator link]
   (get navigator link {}))
  ([navigator link params]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))
         query-params (:query-params resolved-link)]
     (fetch-url href query-params (:options navigator)))))

(defn post
  "Posts content to a link in an API."
  ([navigator link body]
   (post navigator link {} body))
  ([navigator link params body]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))
         query-params (:query-params resolved-link)]
     (post-url href body query-params (:options navigator)))))

(defn put
  "Puts content to a link in an API."
  ([navigator link body]
   (put navigator link {} body))
  ([navigator link params body]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))
         query-params (:query-params resolved-link)]
     (put-url href body query-params (:options navigator)))))

(defn delete
  "Delete content of a link in an API."
  ([navigator link]
   (delete navigator link {}))
  ([navigator link params]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))]
     (delete-url href (:options navigator)))))

(defn follow-redirect
  "Fetches the url of the location header"
  [navigator]
  (-> (extract-redirect-location navigator)
      (fetch-url {} (:options navigator))))

(defn get-header
  "Retrieves a specified header from the response"
  [navigator header]
  (extract-header navigator header))
