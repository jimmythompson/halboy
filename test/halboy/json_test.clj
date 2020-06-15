(ns halboy.json-test
  (:require
    [clojure.test :refer :all]
    [halboy.resource :as hal]
    [halboy.json :as haljson]
    [cheshire.core :as json]))

(deftest halboy-json
  (let [resource
        (-> (hal/new-resource)
            (hal/add-link :self "/orders")
            (hal/add-link :curies {:name      "ea",
                                   :href      "http://example.com/docs/rels/{rel}",
                                   :templated true})
            (hal/add-link :next "/orders?page=2")
            (hal/add-link :ea:find {:href      "/orders{?id}",
                                    :templated true})
            (hal/add-link :ea:admin {:href "/admins/2" :title "Fred"})
            (hal/add-link :ea:admin {:href "/admins/5" :title "Kate"})
            (hal/add-resource :ea:order (-> (hal/new-resource "/orders/123")
                                            (hal/add-links {:ea:basket   "/baskets/98712"
                                                            :ea:customer "/customers/7809"})
                                            (hal/add-properties {:total    30.0
                                                                 :currency "USD"
                                                                 :status   "shipped"})))
            (hal/add-resource :ea:order (-> (hal/new-resource "/orders/124")
                                            (hal/add-links {:ea:basket   "/baskets/97213"
                                                            :ea:customer "/customers/12369"})
                                            (hal/add-properties {:total    20.0,
                                                                 :currency "USD",
                                                                 :status   "processing"})))
            (hal/add-property :currently-processing 14)
            (hal/add-property :shipped-today 20))

        json-representation
        (json/generate-string
          {:_links               {:self     {:href "/orders"},
                                  :curies   {:name      "ea",
                                             :href      "http://example.com/docs/rels/{rel}",
                                             :templated true},
                                  :next     {:href "/orders?page=2"},
                                  :ea:find  {:href      "/orders{?id}",
                                             :templated true},
                                  :ea:admin [{:href "/admins/2" :title "Fred"}
                                             {:href "/admins/5" :title "Kate"}]}
           :_embedded            {:ea:order [(merge
                                               {:_links {:self        {:href "/orders/123"},
                                                         :ea:basket   {:href "/baskets/98712"},
                                                         :ea:customer {:href "/customers/7809"}}}
                                               {:total    30.0,
                                                :currency "USD",
                                                :status   "shipped"})
                                             (merge
                                               {:_links {:self        {:href "/orders/124"},
                                                         :ea:basket   {:href "/baskets/97213"},
                                                         :ea:customer {:href "/customers/12369"}}}
                                               {:total    20.0,
                                                :currency "USD",
                                                :status   "processing"})]}
           :currently-processing 14
           :shipped-today        20})]

    (testing "resource->json should marshal a resource into some json"
      (is (= json-representation
             (haljson/resource->json resource))))

    (testing "json->resource should parse some json into a resource"
      (is (= resource
             (haljson/json->resource json-representation))))

    (testing "get-href should work correctly after applying json->resource"
      (is (= (-> json-representation
               (haljson/json->resource)
               (hal/get-href :ea:admin))
            ["/admins/2"
             "/admins/5"]))))

  (testing "map->resource should parse links"
    (is (= (hal/new-resource "/orders")
           (haljson/map->resource {:_links {:self {:href "/orders"}}}))))

  (testing "map->resource should include all information about a link"
    (is (= (-> (hal/new-resource)
               (hal/add-link :ea:find {:href      "/orders{?id}",
                                       :templated true}))
           (haljson/map->resource {:_links {:ea:find {:href      "/orders{?id}",
                                                      :templated true}}}))))

  (testing "map->resource should parse arrays of links"
    (is (= (-> (hal/new-resource)
               (hal/add-links :ea:admin "/admins/2"
                              :ea:admin "/admins/5"))
           (haljson/map->resource {:_links {:ea:admin [{:href "/admins/2"}
                                                       {:href "/admins/5"}]}}))))

  (testing "map->resource should parse embedded resources"
    (let [order-resource (hal/new-resource "/orders/123")]
      (is (= (-> (hal/new-resource)
                 (hal/add-resource :ea:order order-resource))
             (haljson/map->resource {:_embedded {:ea:order {:_links {:self {:href "/orders/123"}}}}})))))

  (testing "map->resource should parse doubly embedded resources"
    (let [purchaser-resource (hal/new-resource "/customers/1")
          order-resource (-> (hal/new-resource "/orders/123")
                             (hal/add-resource :customer purchaser-resource))]
      (is (= (-> (hal/new-resource)
                 (hal/add-resource :ea:order order-resource))
             (haljson/map->resource {:_embedded {:ea:order {:_links    {:self {:href "/orders/123"}}
                                                            :_embedded {:customer {:_links {:self {:href "/customers/1"}}}}}}})))))

  (testing "map->resource should parse arrays of embedded resources"
    (let [first-order (hal/new-resource "/orders/123")
          second-order (hal/new-resource "/orders/124")]
      (is (= (-> (hal/new-resource)
                 (hal/add-resources :ea:order first-order
                                    :ea:order second-order))
             (haljson/map->resource {:_embedded {:ea:order [{:_links {:self {:href "/orders/123"}}}
                                                            {:_links {:self {:href "/orders/124"}}}]}})))))

  (testing "map->resource should parse properties"
    (is (= (-> (hal/new-resource)
               (hal/add-property :total 20.0))
           (haljson/map->resource {:total 20.0}))))

  (testing "resource->map should render doubly embedded resources"
    (let [purchaser-resource (hal/new-resource "/customers/1")

          order-resource (-> (hal/new-resource "/orders/123")
                             (hal/add-resource :customer purchaser-resource))]
      (is (= (-> (hal/new-resource)
                 (hal/add-resource :ea:order order-resource)
                 (haljson/resource->map))
             {:_embedded {:ea:order {:_links    {:self {:href "/orders/123"}}
                                     :_embedded {:customer {:_links {:self {:href "/customers/1"}}}}}}}))))

  (testing "json->resource should throw an exception when the string is not valid json"
    (is (thrown? clojure.lang.ExceptionInfo
                 (haljson/json->resource
                   "Not valid JSON!")))))
