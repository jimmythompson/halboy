(ns halboy.navigator
  (:refer-clojure :exclude [get])
  (:require [clojure.walk :refer [keywordize-keys stringify-keys]]
            [cheshire.core :as json]
            [halboy.resource :as hal]
            [halboy.data :refer [transform-values]]
            [halboy.json :refer [json->resource resource->json]]
            [halboy.http :refer [GET POST PUT PATCH DELETE]]
            [halboy.params :as params]
            [halboy.argutils :refer [deep-merge]]
            [halboy.url :as url]))

(def default-options
  {:follow-redirects true
   :headers          {}})

(defrecord Navigator [href options response resource])

(defn- follow-redirect? [navigator]
  (and (= 201 (get-in navigator [:response :status]))
       (get-in navigator [:options :follow-redirects])))

(defn- extract-header [navigator header]
  (get-in navigator [:response :headers header]))

(defn- extract-redirect-location [navigator]
  (let [base-url (:href navigator)
        endpoint (extract-header navigator :location)]
    (url/resolve-url base-url endpoint)))

(defn- get-resume-location [resource options]
  (let [resume-from (:resume-from options)
        self-link (hal/get-href resource :self)]
    (if resume-from
      resume-from
      (if (and self-link (url/absolute? self-link))
        self-link
        (throw
          (ex-info "No :resume-from option, and self link not absolute"
                   {:self-link-value self-link}))))))

(defn- response->Navigator [response options]
  (let [current-url (get-in response [:opts :url])
        resource (-> (:body response)
                     json->resource)]
    (->Navigator current-url options response resource)))

(defn- resource->Navigator
  [resource options]
  (let [current-url (get-resume-location resource options)
        response {:status nil}]
    (->Navigator current-url options response resource)))

(defn- fetch-url [url params options]
  (let [combined-options (merge default-options options)]
    (-> (GET url {:query-params (stringify-keys params)
                  :headers      (:headers options)})
        (response->Navigator combined-options))))

(defn- post-url [url body _ options]
  (let [combined-options (merge default-options options)
        post-response (-> (POST url {:body    (json/generate-string body)
                                     :headers (:headers options)})
                          (response->Navigator options))
        status (get-in post-response [:response :status])]
    (if (-> (= status 201)
            (and (:follow-redirects combined-options)))
      (-> (extract-redirect-location post-response)
          (fetch-url {} options))
      post-response)))

(defn- put-url [url body _ options]
  (let [options (merge default-options options)
        result (-> (PUT url {:body    (json/generate-string body)
                             :headers (:headers options)})
                   (response->Navigator options))]
    (if (follow-redirect? result)
      (-> (extract-redirect-location result)
          (fetch-url {} options))
      result)))

(defn- patch-url [url body _ options]
  (let [options (merge default-options options)
        result (-> (PATCH url {:body    (json/generate-string body)
                               :headers (:headers options)})
                   (response->Navigator options))]
    (if (follow-redirect? result)
      (-> (extract-redirect-location result)
          (fetch-url {} options))
      result)))

(defn- delete-url [url params options]
  (-> (DELETE url {:query-params (stringify-keys params)
                   :headers      (:headers options)})
      (response->Navigator options)))

(defn- resolve-absolute-href [navigator href]
  (url/resolve-url (:href navigator) href))

(defn- resolve-link [navigator link params]
  (let [resource (:resource navigator)
        href (hal/get-href resource link)]
    (if (nil? href)
      (throw (ex-info
               "Attempting to follow a link which does not exist"
               {:missing-rel    link
                :available-rels (hal/links resource)
                :resource       resource
                :response       (:response navigator)}))
      (params/build-query href params))))

(defn location
  "Gets the current location of the navigator"
  [navigator]
  (:href navigator))

(defn options
  "Gets the navigation options"
  [navigator]
  (:options navigator))

(defn resource
  "Gets the resource from the navigator"
  [navigator]
  (:resource navigator))

(defn response
  "Gets the last response from the navigator"
  [navigator]
  (:response navigator))

(defn status
  "Gets the status code from the last response from the navigator"
  [navigator]
  (-> (response navigator)
      :status))

(defn discover
  "Starts a conversation with an API. Use this on the discovery endpoint."
  ([href]
   (discover href {}))
  ([href options]
   (fetch-url href {} options)))

(defn resume
  "Resumes a conversation with an API. Your resource needs a self
  link for this to work. If your self link is not absolute, you
  can pass an absolute url in the :resume-from key in the options
  parameter."
  ([resource]
   (resume resource {}))
  ([resource options]
   (resource->Navigator resource options)))

(defn get
  "Fetches the contents of a link in an API."
  ([navigator link]
   (get navigator link {}))
  ([navigator link params]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))
         query-params (:query-params resolved-link)]
     (fetch-url href query-params (:options navigator)))))

(defn post
  "Posts content to a link in an API."
  ([navigator link body]
   (post navigator link {} body))
  ([navigator link params body]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))
         query-params (:query-params resolved-link)]
     (post-url href body query-params (:options navigator)))))

(defn put
  "Puts content to a link in an API."
  ([navigator link body]
   (put navigator link {} body))
  ([navigator link params body]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))
         query-params (:query-params resolved-link)]
     (put-url href body query-params (:options navigator)))))

(defn patch
  "Patch content to a link in an API."
  ([navigator link body]
   (patch navigator link {} body))
  ([navigator link params body]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))
         query-params (:query-params resolved-link)]
     (patch-url href body query-params (:options navigator)))))

(defn delete
  "Delete content of a link in an API."
  ([navigator link]
   (delete navigator link {}))
  ([navigator link params]
   (let [resolved-link (resolve-link navigator link params)
         href (resolve-absolute-href navigator (:href resolved-link))
         query-params (:query-params resolved-link)]
     (delete-url href query-params (:options navigator)))))

(defn follow-redirect
  "Fetches the url of the location header"
  [navigator]
  (-> (extract-redirect-location navigator)
      (fetch-url {} (:options navigator))))

(defn get-header
  "Retrieves a specified header from the response"
  [navigator header]
  (extract-header navigator header))

(defn set-header
  "set header option for navigator"
  [navigator header-key header-value]
  (let [headers (or
                  (get-in navigator [:options :headers])
                  {})
        updated-headers (assoc headers header-key header-value)]
    (assoc-in navigator [:options :headers] updated-headers)))
