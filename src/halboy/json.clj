(ns halboy.json
  (:require [cheshire.core :as json]))

(defn json->map [s]
  (json/parse-string s))

(defn map->json [m]
  (json/generate-string m {:pretty true}))