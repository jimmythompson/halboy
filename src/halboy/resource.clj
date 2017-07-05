(ns halboy.resource
  (:require [clojure.string :as str]
            [halboy.params :as params]))

(defn- create-or-append [previous current]
  (cond
    (nil? previous) current
    (list? previous) (conj previous current)
    :else (list previous current)))

(defrecord Resource [links embedded properties])

(defn new-resource
  ([]
   (->Resource {} {} {}))
  ([links embedded properties]
   (->Resource links embedded properties)))

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

(defn add-link [resource rel m]
  (let [existing-links (:links resource)
        updated-link (-> (get existing-links rel)
                         (create-or-append m))]
    (->Resource
      (assoc existing-links rel updated-link)
      (:embedded resource)
      (:properties resource))))

(defn add-resource [resource rel r]
  (let [existing-resources (:embedded resource)
        updated-resource (-> (get existing-resources rel)
                             (create-or-append r))]
    (->Resource
      (:links resource)
      (assoc existing-resources rel updated-resource)
      (:properties resource))))

(defn add-property [resource rel r]
  (let [existing-properties (:properties resource)]
    (->Resource
      (:links resource)
      (:embedded resource)
      (assoc existing-properties rel r))))