(ns halboy.params)

(def not-nil? (complement nil?))

(defn- expand-param [param values]
  (let [expanded (get values (keyword param))]
    (if (not-nil? expanded)
      (format "/%s" expanded)
      "")))

(defn expand-params [href params]
  (clojure.string/replace
    href
    #"\{\??(\w+)\}"
    (fn [[_ key]] (expand-param key params))))
