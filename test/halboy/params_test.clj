(ns halboy.params-test
    (:require [expectations :refer :all]
              [halboy.params :refer :all]))

; expand-link should fill out link params
(expect
    "/orders/my-order/items"
    (expand-link "/orders/{id}/items" {:id "my-order"}))

; expand-link should remove query params - for now
(expect
    "/orders"
    (expand-link "/orders{?id}" {}))
