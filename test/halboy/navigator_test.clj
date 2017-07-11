(ns halboy.navigator-test
  (:use clojure.pprint)
  (:use org.httpkit.fake)
  (:require [expectations :refer :all]
            [clojure.string :refer [capitalize]]
            [halboy.navigator :as navigator]
            [halboy.resource :as resource]
            [halboy.json :as json]
            [halboy.support.api
             :refer [on-discover
                     on-get
                     on-post
                     on-post-redirect]])
  (:import (java.net URL)))

(def base-url "https://service.example.com")
(defn- create-url [base-url resource]
  (-> (URL. base-url)
      (URL. resource)
      (.toString)))

(defn- create-user [name]
  (-> (resource/new-resource)
      (resource/add-link :self {:href (create-url base-url (format "/users/%s" name))})
      (resource/add-property :name (capitalize name))))

; should be able to navigate through links in an API
(with-fake-http
  (concat
    (on-discover
      base-url
      :users {:href      "/users{?admin}"
              :templated true})
    (on-get
      (create-url base-url "/users")
      {:status 200
       :body   (-> (resource/new-resource)
                   (resource/add-link :self {:href "/users"})
                   (resource/add-resources
                     :users (create-user "fred")
                     :users (create-user "sue")
                     :users (create-user "mary"))
                   (json/resource->json))}))
  (let [result (-> (navigator/discover base-url)
                   (navigator/get :users))
        status (navigator/status result)
        users (-> (navigator/resource result)
                  (resource/get-resource :users))]

    (expect 200 status)

    (expect
      ["Fred" "Sue" "Mary"]
      (map #(resource/get-property % :name) users))))

; should be able to navigate through links with query params
(with-fake-http
  (concat
    (on-discover
      base-url
      :users {:href      "/users{?admin}"
              :templated true})
    (on-get
      (create-url base-url "/users") {:admin true}
      {:status 200
       :body   (-> (resource/new-resource)
                   (resource/add-link :self {:href "/users"})
                   (resource/add-resources
                     :users (create-user "fred")
                     :users (create-user "sue")
                     :users (create-user "mary"))
                   (json/resource->json))}))
  (let [result (-> (navigator/discover base-url)
                   (navigator/get :users {:admin true}))
        status (navigator/status result)
        users (-> (navigator/resource result)
                  (resource/get-resource :users))]

    (expect 200 status)

    (expect
      ["Fred" "Sue" "Mary"]
      (map #(resource/get-property % :name) users))))

; should be able to create resources in an API
(with-fake-http
  (concat
    (on-discover
      base-url
      :users {:href "/users"})
    (on-post-redirect
      (create-url base-url "/users")
      {:name "Thomas"}
      "/users/thomas")
    (on-get
      (create-url base-url "/users/thomas")
      {:status 200
       :body   (-> (resource/new-resource)
                   (resource/add-link :self "/users/thomas")
                   (resource/add-property :name "Thomas")
                   (json/resource->json))}))
  (let [result (-> (navigator/discover base-url)
                   (navigator/post :users {:name "Thomas"}))
        status (navigator/status result)
        new-user (navigator/resource result)]

    (expect 200 status)
    (expect "Thomas" (resource/get-property new-user :name))))

; should not follow location headers when the status is not 201
(with-fake-http
  (concat
    (on-discover
      base-url
      :users {:href      "/users{?admin}"
              :templated true})
    (on-post
      (create-url base-url "/users")
      {:name "Thomas"}
      {:status 400}))
  (let [status (-> (navigator/discover base-url)
                   (navigator/post :users {:name "Thomas"})
                   (navigator/status))]
    (expect 400 status)))
