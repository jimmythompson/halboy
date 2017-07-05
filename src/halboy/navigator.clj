(ns halboy.navigator
  (:require [clojure.walk :refer [keywordize-keys]]
            [halboy.resource :as resource]
            [halboy.data :refer [transform-values]]
            [halboy.json :refer [json->resource]]
            [halboy.http :refer [GET POST]]))

(defrecord Navigator [href resource meta])

(defn- response->navigator [response]
  (let [current-url (get-in response [:opts :url])
        resource (-> (:body response)
                     json->resource)]
    (->Navigator
      current-url
      resource
      (select-keys response [:status]))))

(defn- fetch-url [url]
  (-> (GET url)
      response->navigator))

(defn discover [href]
  (fetch-url href))

(def get-current-location :href)
(def get-resource :resource)
(def get-meta :meta)

(defn navigate [navigator link]
  (-> navigator
      get-resource
      (resource/get-link link)
      fetch-url))
