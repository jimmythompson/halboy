(ns halboy.params
  (:require [expectations :refer :all]))

(defn- expand-param [param values]
  (if-let [expanded (->> (keyword param)
                         (get values))]
    expanded
    ""))

(defn expand-link [href params]
  (clojure.string/replace
    href
    #"\{(\??\w+)\}"
    (fn [[_ key]] (expand-param key params))))
