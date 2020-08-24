(ns halboy.http.clj-http
  (:require [halboy.http.protocol :as protocol]
            [clj-http.client :as client]
            [halboy.http.utils :as http-utils]
            [halboy.argutils :refer [deep-merge]]))

(defn- format-for-halboy [response url opts]
  (merge
    (select-keys response [:error :body :headers :status])
    {:url url
     :raw (assoc response :opts opts)}))

(defn default-clj-http-options [m]
  (deep-merge
    {:throw-exceptions false
     :headers {"Accept"       "application/hal+json"
               "Content-Type" "application/json"}
     :as      :text}
    m))

(deftype CljHttpClient []
  protocol/HttpClient
  (exchange [_ {:keys [url] :as opts}]
    (let [opts (-> opts
                 (default-clj-http-options)
                 (http-utils/with-json-body))]
      (->
        (client/request opts)
        (http-utils/parse-json-response)
        (format-for-halboy url opts)))))

(defn new-http-client []
  (CljHttpClient.))
