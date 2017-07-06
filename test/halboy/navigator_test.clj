(ns halboy.navigator-test
  (:use clojure.pprint)
  (:use org.httpkit.fake)
  (:require [expectations :refer :all]
            [clojure.string :refer [capitalize]]
            [halboy.navigator :as navigator]
            [halboy.resource :as resource]
            [halboy.json :as json]
            [halboy.support.api :refer [on-discover
                                        on-get]])
  (:import (java.net URL)))

(def base-url "https://service.example.com")
(defn create-url [base-url resource]
  (-> (URL. base-url)
      (URL. resource)
      (.toString)))

(defn create-user [name]
  (-> (resource/new-resource)
      (resource/add-link :self (create-url base-url (format "/users/%s" name)))
      (resource/add-property :name (capitalize name))))

; should be able to navigate through links in an API
(with-fake-http
  (concat
    (on-discover
      base-url
      :users {:href (create-url base-url "/users")})
    (on-get
      (create-url base-url "/users")
      {:status 200
       :body   (-> (resource/new-resource)
                   (resource/add-link :self (create-url base-url "/users"))
                   (resource/add-resources
                     :users (create-user "fred")
                     :users (create-user "sue")
                     :users (create-user "mary"))
                   (json/resource->json))}))
  (let [finish (-> (navigator/discover base-url)
                   (navigator/get :users))
        response (navigator/response finish)
        users (-> (navigator/resource finish)
                  (resource/get-resource :users))]

    (expect 200 (:status response))

    (expect
      ["Fred" "Sue" "Mary"]
      (map #(resource/get-property % :name) users))))