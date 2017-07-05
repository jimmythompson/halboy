(ns halboy.navigator
  (:require [clojure.walk :refer [keywordize-keys]]
            [halboy.resource :as resource]
            [halboy.data :refer [transform-values]]
            [halboy.json :refer [json->map map->json]]
            [halboy.http :refer [GET POST]]))

(defrecord Navigator [href resource meta])

(defn- extract-body [response]
  (->
    (:body response)
    json->map
    keywordize-keys))

(defn- extract-links [body]
  (:_links body))

(defn- extract-embedded [body]
  (get body :_embedded {}))

(defn- extract-properties [body]
  (dissoc body :_links :_embedded))

(defn- extract-resource [resource]
  (resource/new-resource
    (extract-links resource)
    (extract-embedded resource)
    (extract-properties resource)))

(defn- extract-navigator-from-response [response]
  (let [current-url (get-in response [:opts :url])
        resource (-> response
                     extract-body
                     extract-resource)]
    (->Navigator
      current-url
      resource
      (select-keys response [:status]))))

(defn- fetch-url [url]
  (-> (GET url)
      extract-navigator-from-response))

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
