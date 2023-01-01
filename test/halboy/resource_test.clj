(ns halboy.resource-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [halboy.resource :as hal]))

(deftest halboy-resource
  (testing "should be able to add new-resource with self-link"
    (is (= {:href "/orders"}
           (-> (hal/new-resource "/orders")
               (hal/get-link :self))))

    (is (= {:href "/orders"}
           (-> (hal/new-resource {:href "/orders"})
               (hal/get-link :self)))))

  (testing "should be able to add and retrieve links from the resource"
    (is (= {:href "/orders"}
           (-> (hal/new-resource)
               (hal/add-link :self {:href "/orders"})
               (hal/get-link :self))))

    (is (= "/orders"
           (-> (hal/new-resource)
               (hal/add-link :self {:href "/orders"})
               (hal/get-href :self)))))

  (testing "should not add falsy links"
    (is (false?
          (contains?
            (-> (hal/new-resource)
                (hal/add-link :self nil)
                (hal/links))
            :self))))

  (testing "should be able to add multiple links and they should stack"
    (is (= [{:href "/admins/2" :title "Fred"}
            {:href "/admins/5" :title "Kate"}]
           (-> (hal/new-resource)
               (hal/add-links
                 :ea:admin {:href "/admins/2" :title "Fred"}
                 :ea:admin {:href "/admins/5" :title "Kate"})
               (hal/get-link :ea:admin))))

    (is (= ["/admins/2"
            "/admins/5"]
           (-> (hal/new-resource)
               (hal/add-links
                 :ea:admin {:href "/admins/2" :title "Fred"}
                 :ea:admin {:href "/admins/5" :title "Kate"})
               (hal/get-href :ea:admin))))

    (is (= {:href "/admins/2" :title "Fred"}
           (-> (hal/new-resource)
               (hal/add-links
                 :ea:admin nil
                 :ea:admin {:href "/admins/2" :title "Fred"})
               (hal/get-link :ea:admin)))))

  (testing "should be able to mix truthy and falsy links"
    (is (= nil
           (-> (hal/new-resource)
               (hal/add-links
                 :self {:href "/orders"}
                 :ea:admin nil)
               (hal/get-link :ea:admin)))))

  (testing "should return nil when getting a link which does not exist"
    (is (nil? (-> (hal/new-resource)
                  (hal/get-link :random)))))

  (testing "should be able to get all the links"
    (is (= {:self {:href "/orders"}}
           (-> (hal/new-resource)
               (hal/add-link :self {:href "/orders"})
               (hal/links)))))

  (testing "should be able to add hrefs and it turns them into links"
    (is (= {:href "/orders"}
           (-> (hal/new-resource)
               (hal/add-link :self "/orders")
               (hal/get-link :self))))

    (is (= {:href "/orders"}
           (-> (hal/new-resource)
               (hal/add-href :self "/orders")
               (hal/get-link :self)))))

  (testing "should be able to add multiple hrefs and they should stack"
    (is (= [{:href "/admins/2"}
            {:href "/admins/5"}]
           (-> (hal/new-resource)
               (hal/add-links
                 :ea:admin "/admins/2"
                 :ea:admin "/admins/5")
               (hal/get-link :ea:admin))))

    (is (= [{:href "/admins/2"}
            {:href "/admins/5"}]
           (-> (hal/new-resource)
               (hal/add-hrefs
                 :ea:admin "/admins/2"
                 :ea:admin "/admins/5")
               (hal/get-link :ea:admin))))

    (is (= ["/admins/2"
            "/admins/5"]
           (-> (hal/new-resource)
               (hal/add-links
                 :ea:admin "/admins/2"
                 :ea:admin "/admins/5")
               (hal/get-href :ea:admin)))))

  (testing "should return nil when getting a href which does not exist"
    (is (nil? (-> (hal/new-resource)
                  (hal/get-href :random)))))

  (testing "add-resource adds an embedded resource"
    (is (= (-> (hal/new-resource)
               (hal/add-links {:self        {:href "/orders/123"}
                           :ea:basket   {:href "/baskets/98712"}
                           :ea:customer {:href "/customers/7809"}})
               (hal/add-properties {:total    30.0
                                :currency "USD"
                                :status   "shipped"}))
           (-> (hal/new-resource)
               (hal/add-resource
                 :ea:order
                 (-> (hal/new-resource)
                     (hal/add-links {:self        {:href "/orders/123"}
                                 :ea:basket   {:href "/baskets/98712"}
                                 :ea:customer {:href "/customers/7809"}})
                     (hal/add-properties {:total    30.0
                                      :currency "USD"
                                      :status   "shipped"})))
               (hal/get-resource :ea:order)))))

  (testing "should not be able to add nil resources"
    (is (false?
          (contains?
            (-> (hal/new-resource)
                (hal/add-resource :ea:order nil)
                (hal/resources))
            :ea:order))))

  (testing "should be able to mix nil and not nil resources"
    (is (= (-> (hal/new-resource)
               (hal/add-links {:self        {:href "/orders/123"}
                           :ea:basket   {:href "/baskets/98712"}
                           :ea:customer {:href "/customers/7809"}})
               (hal/add-properties {:total    30.0
                                :currency "USD"
                                :status   "shipped"}))
           (-> (hal/new-resource)
               (hal/add-resources
                 :ea:order
                 (-> (hal/new-resource)
                     (hal/add-links {:self        {:href "/orders/123"}
                                 :ea:basket   {:href "/baskets/98712"}
                                 :ea:customer {:href "/customers/7809"}})
                     (hal/add-properties {:total    30.0
                                      :currency "USD"
                                      :status   "shipped"}))
                 :ea:order nil)
               (hal/get-resource :ea:order)))))

  (testing "should be able to add a list of resources"
    (is (= [(-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/123"}
                            :ea:basket   {:href "/baskets/98712"}
                            :ea:customer {:href "/customers/7809"}})
                (hal/add-properties {:total    30.0
                                 :currency "USD"
                                 :status   "shipped"}))
            (-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/124"}
                            :ea:basket   {:href "/baskets/97213"}
                            :ea:customer {:href "/customers/12369"}})
                (hal/add-properties {:total    20.0
                                 :currency "USD"
                                 :status   "processing"}))]
           (-> (hal/new-resource)
               (hal/add-resource
                 :ea:order
                 (list (-> (hal/new-resource)
                           (hal/add-links {:self        {:href "/orders/123"}
                                       :ea:basket   {:href "/baskets/98712"}
                                       :ea:customer {:href "/customers/7809"}})
                           (hal/add-properties {:total    30.0
                                            :currency "USD"
                                            :status   "shipped"}))
                       (-> (hal/new-resource)
                           (hal/add-links {:self        {:href "/orders/124"}
                                       :ea:basket   {:href "/baskets/97213"}
                                       :ea:customer {:href "/customers/12369"}})
                           (hal/add-properties {:total    20.0
                                            :currency "USD"
                                            :status   "processing"}))))
               (hal/get-resource :ea:order)))))

  (testing "should be able to add a vector of resources"
    (is (= [(-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/123"}
                            :ea:basket   {:href "/baskets/98712"}
                            :ea:customer {:href "/customers/7809"}})
                (hal/add-properties {:total    30.0
                                 :currency "USD"
                                 :status   "shipped"}))
            (-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/124"}
                            :ea:basket   {:href "/baskets/97213"}
                            :ea:customer {:href "/customers/12369"}})
                (hal/add-properties {:total    20.0
                                 :currency "USD"
                                 :status   "processing"}))]
           (-> (hal/new-resource)
               (hal/add-resource
                 :ea:order
                 [(-> (hal/new-resource)
                      (hal/add-links {:self        {:href "/orders/123"}
                                  :ea:basket   {:href "/baskets/98712"}
                                  :ea:customer {:href "/customers/7809"}})
                      (hal/add-properties {:total    30.0
                                       :currency "USD"
                                       :status   "shipped"}))
                  (-> (hal/new-resource)
                      (hal/add-links {:self        {:href "/orders/124"}
                                  :ea:basket   {:href "/baskets/97213"}
                                  :ea:customer {:href "/customers/12369"}})
                      (hal/add-properties {:total    20.0
                                       :currency "USD"
                                       :status   "processing"}))])
               (hal/get-resource :ea:order)))))

  (testing "should append resources to existing lists of resources"
    (is (= [(-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/123"}
                            :ea:basket   {:href "/baskets/98712"}
                            :ea:customer {:href "/customers/7809"}})
                (hal/add-properties {:total    30.0
                                 :currency "USD"
                                 :status   "shipped"}))
            (-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/124"}
                            :ea:basket   {:href "/baskets/97213"}
                            :ea:customer {:href "/customers/12369"}})
                (hal/add-properties {:total    20.0
                                 :currency "USD"
                                 :status   "processing"}))
            (-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/125"}
                            :ea:basket   {:href "/baskets/98716"}
                            :ea:customer {:href "/customers/2416"}})
                (hal/add-properties {:total    18.0
                                 :currency "GBP"
                                 :status   "shipped"}))]
           (-> (hal/new-resource)
               (hal/add-resource
                 :ea:order
                 [(-> (hal/new-resource)
                      (hal/add-links {:self        {:href "/orders/123"}
                                  :ea:basket   {:href "/baskets/98712"}
                                  :ea:customer {:href "/customers/7809"}})
                      (hal/add-properties {:total    30.0
                                       :currency "USD"
                                       :status   "shipped"}))
                  (-> (hal/new-resource)
                      (hal/add-links {:self        {:href "/orders/124"}
                                  :ea:basket   {:href "/baskets/97213"}
                                  :ea:customer {:href "/customers/12369"}})
                      (hal/add-properties {:total    20.0
                                       :currency "USD"
                                       :status   "processing"}))])
               (hal/add-resource
                 :ea:order
                 (-> (hal/new-resource)
                     (hal/add-links {:self        {:href "/orders/125"}
                                 :ea:basket   {:href "/baskets/98716"}
                                 :ea:customer {:href "/customers/2416"}})
                     (hal/add-properties {:total    18.0
                                      :currency "GBP"
                                      :status   "shipped"})))
               (hal/get-resource :ea:order)))))

  (testing "should merge two lists of resources added separately"
    (is (= [(-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/123"}
                            :ea:basket   {:href "/baskets/98712"}
                            :ea:customer {:href "/customers/7809"}})
                (hal/add-properties {:total    30.0
                                 :currency "USD"
                                 :status   "shipped"}))
            (-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/124"}
                            :ea:basket   {:href "/baskets/97213"}
                            :ea:customer {:href "/customers/12369"}})
                (hal/add-properties {:total    20.0
                                 :currency "USD"
                                 :status   "processing"}))
            (-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/125"}
                            :ea:basket   {:href "/baskets/98716"}
                            :ea:customer {:href "/customers/2416"}})
                (hal/add-properties {:total    18.0
                                 :currency "GBP"
                                 :status   "shipped"}))
            (-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/127"}
                            :ea:basket   {:href "/baskets/98723"}
                            :ea:customer {:href "/customers/2161"}})
                (hal/add-properties {:total    28.0
                                 :currency "USD"
                                 :status   "shipped"}))]
           (-> (hal/new-resource)
               (hal/add-resource
                 :ea:order
                 [(-> (hal/new-resource)
                      (hal/add-links {:self        {:href "/orders/123"}
                                  :ea:basket   {:href "/baskets/98712"}
                                  :ea:customer {:href "/customers/7809"}})
                      (hal/add-properties {:total    30.0
                                       :currency "USD"
                                       :status   "shipped"}))
                  (-> (hal/new-resource)
                      (hal/add-links {:self        {:href "/orders/124"}
                                  :ea:basket   {:href "/baskets/97213"}
                                  :ea:customer {:href "/customers/12369"}})
                      (hal/add-properties {:total    20.0
                                       :currency "USD"
                                       :status   "processing"}))])
               (hal/add-resource
                 :ea:order
                 (list (-> (hal/new-resource)
                           (hal/add-links {:self        {:href "/orders/125"}
                                       :ea:basket   {:href "/baskets/98716"}
                                       :ea:customer {:href "/customers/2416"}})
                           (hal/add-properties {:total    18.0
                                            :currency "GBP"
                                            :status   "shipped"}))
                       (-> (hal/new-resource)
                           (hal/add-links {:self        {:href "/orders/127"}
                                       :ea:basket   {:href "/baskets/98723"}
                                       :ea:customer {:href "/customers/2161"}})
                           (hal/add-properties {:total    28.0
                                            :currency "USD"
                                            :status   "shipped"}))))
               (hal/get-resource :ea:order)))))

  (testing "should be able to add multiple resources and they should stack"
    (is (= [(-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/123"}
                            :ea:basket   {:href "/baskets/98712"}
                            :ea:customer {:href "/customers/7809"}})
                (hal/add-properties {:total    30.0
                                 :currency "USD"
                                 :status   "shipped"}))
            (-> (hal/new-resource)
                (hal/add-links {:self        {:href "/orders/124"}
                            :ea:basket   {:href "/baskets/97213"}
                            :ea:customer {:href "/customers/12369"}})
                (hal/add-properties {:total    20.0
                                 :currency "USD"
                                 :status   "processing"}))]
           (-> (hal/new-resource)
               (hal/add-resources
                 :ea:order
                 (-> (hal/new-resource)
                     (hal/add-links {:self        {:href "/orders/123"}
                                 :ea:basket   {:href "/baskets/98712"}
                                 :ea:customer {:href "/customers/7809"}})
                     (hal/add-properties {:total    30.0
                                      :currency "USD"
                                      :status   "shipped"}))
                 :ea:order
                 (-> (hal/new-resource)
                     (hal/add-links {:self        {:href "/orders/124"}
                                 :ea:basket   {:href "/baskets/97213"}
                                 :ea:customer {:href "/customers/12369"}})
                     (hal/add-properties {:total    20.0
                                      :currency "USD"
                                      :status   "processing"})))
               (hal/get-resource :ea:order)))))

  (testing "should be able to get all the embedded resources"
    (is (= {:ea:order (-> (hal/new-resource)
                          (hal/add-links {:self        {:href "/orders/123"}
                                      :ea:basket   {:href "/baskets/98712"}
                                      :ea:customer {:href "/customers/7809"}})
                          (hal/add-properties {:total    30.0
                                           :currency "USD"
                                           :status   "shipped"}))}
           (-> (hal/new-resource)
               (hal/add-resources
                 {:ea:order (-> (hal/new-resource)
                                (hal/add-links {:self        {:href "/orders/123"}
                                            :ea:basket   {:href "/baskets/98712"}
                                            :ea:customer {:href "/customers/7809"}})
                                (hal/add-properties {:total    30.0
                                                 :currency "USD"
                                                 :status   "shipped"}))})
               (hal/resources)))))

  (testing "should be able to add and retrieve properties from the resource"
    (is (= 14
           (-> (hal/new-resource)
               (hal/add-property :currently-processing 14)
               (hal/get-property :currently-processing))))

    (is (false?
           (-> (hal/new-resource)
               (hal/add-property :active false)
               (hal/get-property :active)))))

  (testing "should not add nil properties to the resource"
    (is (false?
          (contains?
            (-> (hal/new-resource)
                (hal/add-property :active nil)
                (hal/properties))
            :active))))

  (testing "should be able to add multiple properties"
    (let [resource (-> (hal/new-resource)
                       (hal/add-properties
                         :currently-processing 14
                         :shipped-today 20))]
      (is (= 14 (hal/get-property resource :currently-processing)))
      (is (= 20 (hal/get-property resource :shipped-today)))))

  (testing "should be able to add a map of properties"
    (let [resource (-> (hal/new-resource)
                       (hal/add-properties
                         {:currently-processing 14
                          :shipped-today        20}))]
      (is (= 14 (hal/get-property resource :currently-processing)))
      (is (= 20 (hal/get-property resource :shipped-today)))))

  (testing "should be able to retrieve all the properties"
    (let [resource (-> (hal/new-resource)
                       (hal/add-properties
                         {:currently-processing 14
                          :shipped-today        20}))]
      (is (= {:currently-processing 14
              :shipped-today        20}
             (hal/properties resource)))))

  (testing "should be able to navigate deep within properties"
    (is (= 20
           (-> (hal/new-resource)
               (hal/add-property :currently-processing {:uk 20 :de 12})
               (hal/get-in-properties [:currently-processing :uk]))))))
