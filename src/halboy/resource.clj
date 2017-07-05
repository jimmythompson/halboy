(ns halboy.resource
  (:require [clojure.string :as str]
            [halboy.params :as params]))

(defn- create-or-append [previous current]
  (cond
    (nil? previous) current
    (list? previous) (conj previous current)
    :else (list previous current)))

(defn- apply-with-pairs [f resource kvs]
  (if kvs
    (if (next kvs)
      (let [rel (first kvs)
            val (second kvs)]
        (recur f (f resource rel val) (nnext kvs)))
      (throw (IllegalArgumentException.
               "apply-with-pairs expects even number of arguments, found odd number")))
    resource))

(defrecord Resource [links embedded properties])

(defn new-resource
  ([]
   (->Resource {} {} {}))
  ([links embedded properties]
   (->Resource links embedded properties)))

(defn get-link [resource key]
  (get-in resource [:links key]))

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

(defn add-links [resource & args]
  (apply-with-pairs add-link resource args))

(defn add-resource [resource rel r]
  (let [existing-resources (:embedded resource)
        updated-resource (-> (get existing-resources rel)
                             (create-or-append r))]
    (->Resource
      (:links resource)
      (assoc existing-resources rel updated-resource)
      (:properties resource))))

(defn add-resources [resource & args]
  (apply-with-pairs add-resource resource args))

(defn add-property [resource rel r]
  (let [existing-properties (:properties resource)]
    (->Resource
      (:links resource)
      (:embedded resource)
      (assoc existing-properties rel r))))

(defn add-properties [resource & args]
  (apply-with-pairs add-property resource args))
