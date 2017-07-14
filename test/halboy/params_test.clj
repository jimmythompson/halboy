(ns halboy.params-test
  (:require [expectations :refer :all]
            [halboy.params :refer :all]))

; expand-link should fill out link params
(expect
  {:href "/orders/my-order/items"
   :query-params {:completed true}}
  (build-query "/orders/{id}/items" {:id "my-order" :completed true}))

(expect
  {:href "/orders/my-order/items/my-item"
   :query-params {}}
  (build-query "/orders/{order-id}/items/{item-id}"
               {:order-id "my-order" :item-id "my-item"}))

; expand-link should remove query params from uri templates
(expect
  {:href "/orders"
   :query-params {}}
  (build-query "/orders{?completed}" {}))

(expect
  {:href "/orders"
   :query-params {:completed true}}
  (build-query "/orders{?completed}" {:completed true}))
