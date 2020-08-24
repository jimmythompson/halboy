(ns halboy.support.clj-http-api
  (:require [clojure.walk :refer [stringify-keys]]
            [cheshire.core :as json]
            [halboy.resource :refer [new-resource add-links]]
            [halboy.json :refer [resource->json]]
            [clj-http.util :as http-util]))

(defn- fake-mismatch [req]
  (throw (ex-info "fake does not match" {:request req})))

(defn- headers-match? [req headers]
  (= (:headers req)
    headers))

(defn- body-match? [req body]
  (=
    (http-util/force-string (:body req) "utf-8")
    (json/generate-string body)))

(defn- redirect-to [location]
  {:status  201
   :headers {:location location}})

(defn on-discover [url & kvs]
  {url
   {:get
    (fn [_]
      {:status 200
       :body   (-> (new-resource)
                 ((partial apply add-links) kvs)
                 resource->json)})}})

(defn on-head
  ([url response]
   {url
    {:head (fn [_] response)}}))

(defn on-get
  ([url response]
   {url
    {:get (fn [_] response)}})
  ([url params response]
   {{:address      url
     :query-params params}
    (fn [_] response)}))

(defn on-post
  ([url response]
   {url
    {:post (fn [_] response)}})
  ([url body response]
   {url
    {:post
     (fn [req]
       (when-not (body-match? req body) (fake-mismatch req))
       response)}}))

(defn on-post-with-headers
  ([url headers response]
   {url
    {:post
     (fn [req]
       (when-not (headers-match? req headers) (fake-mismatch req))
       response)}})
  ([url headers body response]
   {url
    {:post
     (fn [req]
       (when-not (and (body-match? req body) (headers-match? req headers))
         (fake-mismatch req))
       response)}}))

(defn on-post-redirect
  ([url location]
   (on-post url (redirect-to location)))
  ([url body location]
   (on-post url body (redirect-to location))))

(defn on-put
  ([url response]
   {{:address url}
    {:put (fn [_] response)}})
  ([url body response]
   {{:address url}
    {:put
     (fn [req]
       (when-not (body-match? req body) (fake-mismatch req))
       response)}}))

(defn on-put-redirect
  ([url location]
   (on-put url (redirect-to location)))
  ([url body location]
   (on-put url body (redirect-to location))))

(defn on-patch
  ([url response]
   {{:address url}
    {:patch (fn [_] response)}})
  ([url body response]
   {{:address url}
    {:patch
     (fn [req]
       (when-not (body-match? req body) (fake-mismatch req))
       response)}}))

(defn on-patch-redirect
  ([url location]
   (on-patch url (redirect-to location)))
  ([url body location]
   (on-patch url body (redirect-to location))))

(defn on-delete
  ([url response]
   {url
    {:delete (fn [_] response)}})
  ([url params response]
   {{:address      url
     :method       :delete
     :query-params params}
    (fn [_] response)}))

(defn on-delete-with-headers
  [url headers response]
  {url
   {:delete
    (fn [req]
      (when-not (headers-match? req headers) (fake-mismatch req))
      response)}})

