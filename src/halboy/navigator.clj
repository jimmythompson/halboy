(ns halboy.navigator
  (:refer-clojure :exclude [get])
  (:require [clojure.walk :refer [keywordize-keys stringify-keys]]
            [halboy.resource :as hal]
            [halboy.data :refer [transform-values]]
            [halboy.json :as haljson]
            [halboy.http.default :as client]
            [halboy.http.protocol :as http]
            [halboy.params :as params]
            [halboy.argutils :refer [deep-merge]]
            [halboy.url :as url]))

(def default-options
  {:client           (client/new-http-client)
   :follow-redirects true
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
                     haljson/map->resource)]
    (->Navigator current-url options response resource)))

(defn- resource->Navigator
  [resource options]
  (let [current-url (get-resume-location resource options)
        response {:status nil}]
    (->Navigator current-url options response resource)))

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

(defn- with-custom-headers [request headers]
  (let [headers (merge (:headers request) headers)]
    (assoc request :headers headers)))

(defn- read-url [request options]
  (let [options (merge default-options options)
        request (with-custom-headers request (:headers options))
        client (:client options)]
    (-> (http/exchange client request)
        (response->Navigator options))))

(defn- get-url
  ([url options]
   (get-url url {} options))
  ([url params options]
   (read-url
     {:method       :get
      :url          url
      :query-params params}
     options)))

(defn- delete-url
  ([url options]
   (delete-url url {} options))
  ([url params options]
   (read-url
     {:method       :delete
      :url          url
      :query-params params}
     options)))

(defn- write-url [request options]
  (let [options (merge default-options options)
        request (with-custom-headers request (:headers options))
        client (:client options)
        result (-> (http/exchange client request)
                   (response->Navigator options))]
    (if (follow-redirect? result)
      (-> (extract-redirect-location result)
          (get-url options))
      result)))

(defn- post-url
  ([url body options]
   (post-url url body {} options))
  ([url body params options]
   (write-url
     {:method       :post
      :url          url
      :body         body
      :query-params params}
     options)))

(defn- put-url
  ([url body options]
   (put-url url body {} options))
  ([url body params options]
   (write-url
     {:method       :put
      :url          url
      :body         body
      :query-params params}
     options)))

(defn- patch-url
  ([url body options]
   (patch-url url body {} options))
  ([url body params options]
   (write-url
     {:method       :patch
      :url          url
      :body         body
      :query-params params}
     options)))

(defn location
  "Gets the current location of the navigator"
  [navigator]
  (:href navigator))

(defn options
  "DEPRECATED: Use 'settings' instead
   Gets the navigation options"
  {:deprecated "2.0.8"}
  [navigator]
  (:options navigator))

(defn settings
  "Gets the navigation settings"
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
   (get-url href options)))

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
     (get-url href query-params (:options navigator)))))

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
      (get-url (:options navigator))))

(defn get-header
  "Retrieves a specified header from the response"
  [navigator header]
  (extract-header navigator header))

(defn set-header
  "set header option for navigator"
  [navigator header-key header-value]
  (let [headers (get-in navigator [:options :headers] {})
        updated-headers (assoc headers header-key header-value)]
    (assoc-in navigator [:options :headers] updated-headers)))
