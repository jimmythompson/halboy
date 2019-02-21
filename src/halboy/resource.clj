(ns halboy.resource
  (:require [halboy.argutils :refer [apply-pairs-or-map]]))

(defn- create-or-append [l r]
  (if (not (nil? l))
    (flatten [l r])
    r))

(defn- ensure-link [x]
  (if (string? x)
    {:href x}
    x))

(defrecord Resource [links embedded properties])

(defn links
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
  (let [link (get-link resource key)]
    (if (seq? link)
      (map :href link)
      (:href link))))

(defn resources
  "Gets all the embedded resources as a map"
  [resource]
  (:embedded resource))

(defn get-resource
  "Gets an embedded resource from a resource"
  [resource key]
  (get-in resource [:embedded key]))

(defn properties
  "Gets all the properties from a resource as a map"
  [resource]
  (:properties resource))

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
  (if-let [m (ensure-link m)]
    (let [existing-links (:links resource)
          updated-link (-> (get existing-links rel)
                           (create-or-append m))]
      (->Resource
        (assoc existing-links rel updated-link)
        (:embedded resource)
        (:properties resource)))
    resource))

(def add-href add-link)

(defn add-links
  "Adds each rel->link to the resource"
  [resource & args]
  (apply-pairs-or-map add-link resource args))

(def add-hrefs add-links)

(defn add-resource
  "Adds an embedded resource to the resource. If the key is
  already present, the values will form a vector."
  [resource key r]
  (if (some? r)
    (let [existing-resources (:embedded resource)
          updated-resource (-> (get existing-resources key)
                               (create-or-append r))]
      (->Resource
        (:links resource)
        (assoc existing-resources key updated-resource)
        (:properties resource)))
    resource))

(defn add-resources
  "Adds each key->resource pair to the resource. If the same key
  is used, the values will form a vector."
  [resource & args]
  (apply-pairs-or-map add-resource resource args))

(defn add-property
  "Adds a new property to the resource. If the key is already
  present, it will be overwritten."
  [resource rel r]
  (if (some? r)
    (let [existing-properties (:properties resource)]
      (->Resource
        (:links resource)
        (:embedded resource)
        (assoc existing-properties rel r)))
    resource))

(defn add-properties
  "Takes a map, or key->value pairs. It adds each key->value pair to the
  resource. If the key is already present, it will be overwritten."
  [resource & args]
  (apply-pairs-or-map add-property resource args))

(defn new-resource
  "Creates a new HAL resource"
  ([]
   (->Resource {} {} {}))
  ([self]
   (-> (->Resource {} {} {})
       (add-link :self self))))
