(ns halboy.http.protocol)

(defprotocol HttpClient
  (exchange [self {:keys [method url params options]}]))
