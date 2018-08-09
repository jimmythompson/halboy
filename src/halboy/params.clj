(ns halboy.params)

(def template-param-pattern #"\{([\w-_]+)\}")
(def param-pattern #"\{(\??[\w-_,]+)\}")

(defn- query-param? [param]
  (clojure.string/starts-with? param "?"))

(defn- clean-param-name [param-name]
  (if (query-param? param-name)
    (rest param-name)
    param-name))

(defn- expand-param [param values]
  (if-let [expanded
           (->> (clean-param-name param)
                (keyword)
                (get values))]
    expanded
    ""))

(defn- expand-href [href params]
  (clojure.string/replace
    href
    param-pattern
    (fn [[_ key]] (expand-param key params))))

(defn- get-template-params [href]
  (->> (re-seq template-param-pattern href)
       (flatten)
       (apply hash-map)
       (vals)
       (map keyword)))

(defn build-query [href params]
  (let [expanded-href (expand-href href params)
        param-names (get-template-params href)]
    {:href         expanded-href
     :query-params (apply dissoc params param-names)}))
