(ns halboy.params
  (:require [expectations :refer :all]))

(def not-nil? (complement nil?))

(defn- expand-param [param values]
  (let [expanded (get values (keyword param))]
    (if (not-nil? expanded)
      expanded
      "")))

(defn expand-link [href params]
  (clojure.string/replace
    href
    #"\{(\??\w+)\}"
    (fn [[_ key]] (expand-param key params))))
