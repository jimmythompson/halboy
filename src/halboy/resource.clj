(ns halboy.resource
  (:require [clojure.string :as str]
            [halboy.params :as params]
            [halboy.argutils :refer [apply-pairs-or-map]]))

(defn- create-or-append [l r]
  (if (not (nil? l))
    (flatten [l r])
    r))

(defrecord Resource [links embedded properties])

(defn new-resource
  "Creates a new HAL resource"
  ([]
   (->Resource {} {} {}))
  ([self-link]
   (->Resource {:self self-link} {} {})))

(defn get-links
  "Gets a map of all the links in the resource"
  [resource]
  (:links resource))

(defn get-link
  "Gets a link from a resource"
  [resource key]
  (get-in resource [:links key]))

(defn get-href
  "Gets a href within a resource"
  [resource key]
  (-> (get-link resource key)
      :href))

(defn get-resource
  "Gets an embedded resource from a resource"
  [resource key]
  (get-in resource [:embedded key]))

(defn get-property
  "Gets an property from a resource"
  [resource key]
  (get-in resource [:properties key]))

(defn get-in-properties
  "Navigates through the keys in properties"
  [resource keys]
  (get-in resource (into [:properties] keys)))

(defn add-link
  "Adds a link to a resource. If the rel is already present,
  the values will form a vector."
  [resource rel m]
  (let [existing-links (:links resource)
        updated-link (-> (get existing-links rel)
                         (create-or-append m))]
    (->Resource
      (assoc existing-links rel updated-link)
      (:embedded resource)
      (:properties resource))))

(defn add-links
  "Adds each rel->link to the resource"
  [resource & args]
  (apply-pairs-or-map add-link resource args))

(defn add-href
  "Adds a link with the given href to a resource. If the rel
  is already present, the values will form a vector."
  [resource rel href]
  (add-link resource rel {:href href}))

(defn add-hrefs
  "Adds each rel->href to the resource"
  [resource & args]
  (apply-pairs-or-map add-href resource args))

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
  (apply-pairs-or-map add-resource resource args))

(defn add-property
  "Adds a new property to the resource. If the key is already
  present, it will be overwritten."
  [resource rel r]
  (let [existing-properties (:properties resource)]
    (->Resource
      (:links resource)
      (:embedded resource)
      (assoc existing-properties rel r))))

(defn add-properties
  "Takes a map, or key->value pairs. It adds each key->value pair to the
  resource. If the key is already present, it will be overwritten."
  [resource & args]
  (apply-pairs-or-map add-property resource args))
