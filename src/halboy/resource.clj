(ns halboy.resource
  (:require [clojure.string :as str]
            [halboy.params :as params]))

(defn- create-or-append [l r]
  (cond
    (nil? l) r
    (list? l) (conj l r)
    :else (list l r)))

(defn- apply-with-pairs [f resource kvs]
  (if kvs
    (if (next kvs)
      (let [rel (first kvs)
            val (second kvs)]
        (recur f (f resource rel val) (nnext kvs)))
      (throw (IllegalArgumentException.
               "expected an even number of arguments, found odd number")))
    resource))

(defrecord Resource [links embedded properties])

(defn new-resource
  "Creates a new HAL resource"
  ([]
   (->Resource {} {} {}))
  ([links embedded properties]
   (->Resource links embedded properties)))

(defn get-link
  "Gets a link from a resource"
  [resource key]
  (get-in resource [:links key]))

(defn get-resource [resource key]
  "Gets an embedded resource from a resource"
  (get-in resource [:embedded key]))

(defn get-property [resource key]
  "Gets an property from a resource"
  (get-in resource [:properties key]))

(defn add-link [resource rel m]
  "Adds a link to a resource. If the rel is already present,
  the values will form a vector."
  (let [existing-links (:links resource)
        updated-link (-> (get existing-links rel)
                         (create-or-append m))]
    (->Resource
      (assoc existing-links rel updated-link)
      (:embedded resource)
      (:properties resource))))

(defn add-links [resource & args]
  "Adds each rel->link to the resource"
  (apply-with-pairs add-link resource args))

(defn add-resource
  "Adds an embedded resource to the resource. If the key is
  already present, the values will form a vector."
  [resource key r]
  (let [existing-resources (:embedded resource)
        updated-resource (-> (get existing-resources key)
                             (create-or-append r))]
    (->Resource
      (:links resource)
      (assoc existing-resources key updated-resource)
      (:properties resource))))

(defn add-resources
  "Adds each key->resource pair to the resource. If the same key
  is used, the values will form a vector."
  [resource & args]
  (apply-with-pairs add-resource resource args))

(defn add-property [resource rel r]
  "Adds a new property to the resource. If the key is already
  present, it will be overwritten."
  (let [existing-properties (:properties resource)]
    (->Resource
      (:links resource)
      (:embedded resource)
      (assoc existing-properties rel r))))

(defn add-properties
  "Adds each key->value pair to the resource. If the key is
  already present, it will be overwritten."
  [resource & args]
  (apply-with-pairs add-property resource args))
