(ns halboy.http.utils
  (:require [clojure.walk :refer [keywordize-keys]]
            [cheshire.core :as json])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(defn update-if-present [m ks fn]
  (if (get-in m ks)
    (update-in m ks #(fn %))
    m))

(defn with-json-body [m]
  (update-if-present m [:body] json/generate-string))

(defn parse-json-response [response]
  (try
    (update-if-present
      response [:body]
      #(-> (json/parse-string %)
         (keywordize-keys)))
    (catch JsonParseException ex
      (assoc response
        :error {:code :not-valid-json
                :cause ex}))))
