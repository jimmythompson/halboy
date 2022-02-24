(ns halboy.types
  (:require [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [halboy.data :refer [update-if-present]]
            [cheshire.core :as json])
  (:import [java.io InputStream]
           [org.httpkit BytesInputStream]
           [com.fasterxml.jackson.core JsonParseException]))

(defn- parse-json-response [response]
  (try
    (update-if-present response [:body]
      #(keywordize-keys (json/parse-string %)))
    (catch JsonParseException ex
      (assoc response
        :error {:code :not-valid-json
                :cause ex}))))

(defn extract-content-type [response]
  (let [content-type (get-in response [:headers :content-type] "application/json")
        [media-type properties] (str/split content-type #";" 2)]
    {:media-type   (str/trim media-type)
     :charset      (as-> (or properties "") %
                     (re-find #"charset=([A-Za-z0-9-]+)" %)
                     (nth % 2 "UTF-8")
                     (str/upper-case %))
     :content-type content-type}))

(def ^:private json-media-type?
  #{"application/json"
    "application/hal+json"})

(defn- json-content-type? [response]
  (let [content-type (extract-content-type response)]
    (json-media-type? (:media-type content-type))))

(defn- with-json-body [m]
  (update-if-present m [:body] json/generate-string))

(defn if-json-encode-body [request]
  (if (json-content-type? request)
    (with-json-body request)
    request))

(defprotocol Stringify
  (stringify [this charset] "Convert \"this\" to a string with \"charset\"."))

(extend-protocol Stringify
  (class (byte-array 0))
  (stringify [this charset]
    (String. ^bytes this ^String charset))
  nil
  (stringify [_ _] nil)
  String
  (stringify [this _] this)
  BytesInputStream
  (stringify [this charset]
    (stringify (.bytes this) ^String charset))
  InputStream
  (stringify [this charset]
    (stringify (.readAllBytes this) ^String charset)))

(defn- stream-body->string-body [response ^String charset]
  (if (:body response)
    (update response :body stringify charset)
    response))

(defn coerce-response-type [response]
  (let [{:keys [media-type charset]} (extract-content-type response)]
    (cond
      (str/includes? media-type "json")
      (parse-json-response (stream-body->string-body response charset))
      (or (str/starts-with? media-type "text/")
          (str/includes? media-type "xml"))
      (stream-body->string-body response charset)
      :else
      response)))
