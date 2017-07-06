(ns halboy.support.api
  (:require [clojure.walk :refer [stringify-keys]]
            [cheshire.core :as json]
            [halboy.resource :refer [new-resource add-links]]
            [halboy.json :refer [resource->json]]))

(defn on-discover [url & kvs]
  [{:method :get :url url}
   {:status 200 :body (-> (new-resource)
                          ((partial apply add-links) kvs)
                          resource->json)}])

(defn on-get
  ([url response]
   [{:method :get :url url}
    response])
  ([url params response]
   [{:method       :get
     :url          url
     :query-params (stringify-keys params)}
    response]))

(defn on-post
  ([url location]
   [{:method :post :url url}
    {:status 201 :headers {:location location}}])
  ([url body location]
   [{:method :post
     :url    url
     :body   (json/generate-string body)}
    {:status 201 :headers {:location location}}]))
