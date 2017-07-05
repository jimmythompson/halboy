(ns halboy.resource
  (:require [clojure.string :as str]
            [halboy.params :as params]))

(defrecord Resource [links embedded properties])

(defn new-resource [links embedded properties]
  (->Resource links embedded properties))

(defn new-embedded-resource [links properties]
  (->Resource links {} properties))

(defn new-index-resource [links]
  (->Resource links {} {}))

(defn get-link
  ([resource rel]
   (get-link resource rel {}))
  ([resource rel params]
   (-> (get-in resource [:links rel :href])
       (params/expand-params params))))

(defn get-embedded [resource key]
  (get-in resource [:embedded key]))

(defn get-property [resource key]
  (get-in resource [:properties key]))
