(ns halboy.params-test
  (:require [clojure.test :refer :all]
            [halboy.params :refer :all]))

(deftest halboy-params
  (testing "expand-link should fill out link params"
    (is (= {:href         "/orders/my-order/items"
            :query-params {:completed true}}
           (build-query "/orders/{id}/items" {:id "my-order" :completed true})))

    (is (= {:href         "/orders/my-order/items/my-item"
            :query-params {}}
           (build-query "/orders/{order-id}/items/{item-id}"
                        {:order-id "my-order" :item-id "my-item"}))))

  (testing "expand-link should remove query params from uri templates"
    (is (= {:href         "/orders"
            :query-params {}}
           (build-query "/orders{?completed}" {})))

    (is (= {:href         "/orders"
            :query-params {:completed true}}
           (build-query "/orders{?completed}" {:completed true})))))
