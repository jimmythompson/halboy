(ns halboy.params-test
  (:require [clojure.test :refer :all]
            [halboy.params :refer :all]))

(deftest on-build-query
  (testing "expands single path param"
    (is (= {:href         "/orders/my-order/items"
            :query-params {}}
           (build-query "/orders/{id}/items" {:id "my-order"}))))

  (testing "expands multiple path params"
    (is (= {:href         "/orders/my-order/items/my-item"
            :query-params {}}
           (build-query "/orders/{order_id}/items/{item_id}"
                        {:order_id "my-order" :item_id "my-item"}))))

  (testing "expands path and query params"
    (is (= {:href         "/orders/my-order/items"
            :query-params {"completed" "true"}}
           (build-query "/orders/{id}/items{?completed}"
                        {:id "my-order" :completed true}))))

  (testing "expands multiple query params"
    (is (= {:href         "/orders/my-order/items"
            :query-params {"completed" "true"
                           "shipped"   "true"}}
           (build-query "/orders/{id}/items{?completed,shipped}"
                        {:id "my-order" :completed true :shipped true}))))

  (testing "ignores template query params with no value"
    (is (= {:href         "/orders"
            :query-params {}}
           (build-query "/orders{?completed,shipped}" {}))))

  (testing "ignores template query params with no value when others have value"
    (is (= {:href         "/orders"
            :query-params {"completed" "true"}}
           (build-query "/orders{?completed,shipped}" {:completed true}))))

  (testing "retains additional query params"
    (is (= {:href         "/orders"
            :query-params {"completed" "true"}}
           (build-query "/orders{?shipped}" {:completed true}))))

  (testing "supports list type query params"
    (is (= {:href         "/orders"
            :query-params {"ids" ["1" "2" "3"]}}
           (build-query "/orders{?ids*}" {:ids [1 2 3]}))))

  (testing "supports standard query params when list supplied"
    (is (= {:href         "/orders"
            :query-params {"ids" "1,2,3"}}
           (build-query "/orders{?ids}" {:ids [1 2 3]})))))
