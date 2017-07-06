(ns halboy.json
  (:require
    [clojure.walk :refer [keywordize-keys]]
    [halboy.data :refer [transform-values]]
    [halboy.resource :refer [new-resource]]
    [cheshire.core :as json]))

(defn- extract-links [m]
  (:_links m))

(defn- extract-properties [m]
  (dissoc m :_links :_embedded))

(defn- map->embedded-resource [m]
  (if (map? m)
    (new-resource
      (extract-links m)
      {}
      (extract-properties m))
    (map map->embedded-resource m)))

(defn- extract-embedded [body]
  (-> (get body :_embedded {})
      (transform-values map->embedded-resource)))

(defn- links->map [resource]
  (let [links (:links resource)]
    (when (not (empty? links))
      {:_links links})))

(defn- embedded-resource->map [resource]
  (if (map? resource)
    (merge
      (links->map resource)
      (:properties resource))
    (map embedded-resource->map resource)))

(defn- embedded->map [resource]
  (let [resources (-> (:embedded resource)
                      (transform-values embedded-resource->map))]
    (when (not (empty? resources))
      {:_embedded resources})))


(defn map->resource
  "Parses a map representing a HAL+JSON response into a
  resource"
  [m]
  (new-resource
    (extract-links m)
    (extract-embedded m)
    (extract-properties m)))

(defn json->resource
  "Parses a HAL+JSON string into a resource"
  [s]
  (-> (json/parse-string s)
      keywordize-keys
      map->resource))

(defn resource->map
  "Transforms a resource into a map representing a HAL+JSON
  response"
  [resource]
  (merge
    (links->map resource)
    (embedded->map resource)
    (:properties resource)))

(defn resource->json
  "Transforms a resource into a HAL+JSON string"
  [resource]
  (-> (resource->map resource)
      json/generate-string))
