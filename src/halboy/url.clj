(ns halboy.url
  (:import (java.net URL URI)))

(defn resolve-url [host endpoint]
  (-> (URL. host)
      (URL. endpoint)
      (.toString)))

(defn absolute? [url]
  (-> (URI. url)
      (.isAbsolute)))
