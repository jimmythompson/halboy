(ns halboy.url
  (:import (java.net URL URI)))

(defn resolve-url [host endpoint]
  (try
    (-> (URL. host)
        (URL. endpoint)
        (.toString))
    (catch Exception _ nil)))

(defn absolute? [url]
  (-> (URI. url)
      (.isAbsolute)))
