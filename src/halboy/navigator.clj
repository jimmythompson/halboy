(ns halboy.navigator
  (:refer-clojure :exclude [get])
  (:require [clojure.walk :refer [keywordize-keys]]
            [halboy.resource :as resource]
            [halboy.data :refer [transform-values]]
            [halboy.json :refer [json->resource]]
            [halboy.http :refer [GET POST]]))

(defrecord Navigator [href response resource])

(defn- response->Navigator [response]
  (let [current-url (get-in response [:opts :url])
        resource (-> (:body response)
                     json->resource)]
    (->Navigator
      current-url
      response
      resource)))

(defn- fetch-url [url]
  (-> (GET url)
      response->Navigator))

(defn discover [href]
  (fetch-url href))

(def location :href)
(def resource :resource)
(def response :response)

(defn get [navigator link]
  (-> navigator
      resource
      (resource/get-link link)
      :href
      fetch-url))
