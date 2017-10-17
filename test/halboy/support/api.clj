(ns halboy.support.api
  (:require [clojure.walk :refer [stringify-keys]]
            [cheshire.core :as json]
            [halboy.resource :refer [new-resource add-links]]
            [halboy.json :refer [resource->json]]))

(defn- redirect-to [location]
  {:status  201
   :headers {:location location}})

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

(defn on-post-with-headers
  ([url headers response]
   [{:method  :post
     :url     url
     :headers headers}
    response])
  ([url headers body response]
   [{:method  :post
     :url     url
     :headers headers
     :body    (json/generate-string body)}
    response]))

(defn on-post-redirect
  ([url location]
   (on-post url (redirect-to location)))
  ([url body location]
   (on-post url body (redirect-to location))))

(defn on-put
  ([url response]
   [{:method :put :url url}
    response])
  ([url body response]
   [{:method :put
     :url    url
     :body   (json/generate-string body)}
    response]))

(defn on-put-redirect
  ([url location]
   (on-put url (redirect-to location)))
  ([url body location]
   (on-put url body (redirect-to location))))

(defn on-patch
  ([url response]
   [{:method :patch :url url}
    response])
  ([url body response]
   [{:method :patch
     :url    url
     :body   (json/generate-string body)}
    response]))

(defn on-patch-redirect
  ([url location]
   (on-patch url (redirect-to location)))
  ([url body location]
   (on-patch url body (redirect-to location))))

(defn on-delete
  ([url response]
   [{:method :delete :url url}
    response])
  ([url params response]
   [{:method       :delete
     :url          url
     :query-params (stringify-keys params)}
    response]))

(defn on-delete-with-headers
  [url headers response]
  [{:method  :delete
    :url     url
    :headers headers}
   response])

