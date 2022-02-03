(ns halboy.params
  (:require
    [clojure.walk :refer [postwalk]]
    [clojure.set :refer [difference]]
    [medley.core :refer [map-vals]]
    [uritemplate-clj.core :refer [uritemplate tokenize parse-token]]
    [org.bovinegenius.exploding-fish :as uri]))

(defn- stringify-params [params]
  (let [f (fn [x]
            (cond
              (vector? x) x
              (map? x) (into {} (map (fn [[k v]] [(name k) v]) x))
              (keyword? x) (name x)
              :else (str x)))]
    (postwalk f params)))

(defn- extract-template-variable-names [templated-uri]
  (let [tokens (tokenize templated-uri)
        tokens-with-variables (filter #(= \{ (first %)) tokens)
        parsed-tokens-with-variables (map parse-token tokens-with-variables)
        variables (mapcat :variables parsed-tokens-with-variables)
        variable-names (map :text variables)]
    variable-names))

(defn- expand-template [templated-uri params]
  (uritemplate templated-uri params))

(defn- extract-query-params [uri]
  (let [pairs (uri/query-pairs uri)
        grouped-pairs (group-by first pairs)
        accumulated-pairs (map-vals
                            #(if (= 1 (count %))
                               (second (first %))
                               (mapv second %))
                            grouped-pairs)]
    (into {} accumulated-pairs)))

(defn- remove-query-params [uri]
  (-> uri (uri/uri) (dissoc :query) (str)))

(defn- determine-remaining-param-names [params template-variable-names]
  (difference (set (keys params)) (set template-variable-names)))

(defn- determine-full-query-params
  [params remaining-param-names expanded-uri-query-params]
  (merge (select-keys params remaining-param-names)
         expanded-uri-query-params))

(defn build-query [templated-uri params]
  (let [stringified-params (stringify-params params)

        template-variable-names (extract-template-variable-names templated-uri)

        expanded-uri (expand-template templated-uri stringified-params)
        expanded-uri-query-params (extract-query-params expanded-uri)
        expanded-uri-without-query-params (remove-query-params expanded-uri)

        remaining-param-names (determine-remaining-param-names
                                stringified-params template-variable-names)
        full-query-params (determine-full-query-params
                            stringified-params
                            remaining-param-names
                            expanded-uri-query-params)]
    {:href         expanded-uri-without-query-params
     :query-params full-query-params}))
