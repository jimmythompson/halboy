(ns halboy.support.api
  (:require [halboy.resource :refer [new-resource add-links]]
            [halboy.json :refer [resource->json]]))

(defn on-discover [url & kvs]
  [{:method :get :url url}
   {:status 200 :body (-> (new-resource)
                          ((partial apply add-links) kvs)
                          resource->json)}])

(defn on-get [url response]
  [{:method :get :url url}
   response])