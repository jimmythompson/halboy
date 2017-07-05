(ns halboy.support.api)

(defn on-discover [url response]
  [{:method :get :url url}
   response])