(ns halboy.support.api
  (:require [clojure.walk :refer [stringify-keys]]
            [cheshire.core :as json]
            [halboy.resource :refer [new-resource add-links]]
            [halboy.json :refer [resource->json]]))

(defn on-discover [url & kvs]
  [{:method :get :url url}
   {:status 200
    :body   (-> (new-resource)
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
  ([url response]
   [{:method :post :url url}
    response])
  ([url body response]
   [{:method :post
     :url    url
     :body   (json/generate-string body)}
    response]))

(defn on-post-redirect
  ([url location]
   (on-post url {:status  201
                 :headers {:location location}}))
  ([url body location]
   (on-post url body {:status  201
                      :headers {:location location}})))
