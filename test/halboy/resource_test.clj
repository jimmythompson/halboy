(ns halboy.resource-test
  (:require
    [expectations :refer :all]
    [halboy.resource :refer :all]))

; should be able to add new-resource with self-link
(expect
  {:href "/orders"}
  (-> (new-resource {:href "/orders"})
      (get-link :self)))

; should be able to add and retrieve links from the resource
(expect
  {:href "/orders"}
  (-> (new-resource)
      (add-link :self {:href "/orders"})
      (get-link :self)))

(expect
  "/orders"
  (-> (new-resource)
      (add-link :self {:href "/orders"})
      (get-href :self)))

; should be able to add multiple links and they should stack
(expect
  [{:href "/admins/2" :title "Fred"}
   {:href "/admins/5" :title "Kate"}]
  (-> (new-resource)
      (add-links
        :ea:admin {:href "/admins/2" :title "Fred"}
        :ea:admin {:href "/admins/5" :title "Kate"})
      (get-link :ea:admin)))

; should return nil when getting a link which does not exist
(expect
  nil
  (-> (new-resource)
      (get-link :random)))

; should be able to add links using add-href
(expect
  {:href "/orders"}
  (-> (new-resource)
      (add-href :self "/orders")
      (get-link :self)))

; should be able to add multiple hrefs and they should stack
(expect
  [{:href "/admins/2"}
   {:href "/admins/5"}]
  (-> (new-resource)
      (add-hrefs
        :ea:admin "/admins/2"
        :ea:admin "/admins/5")
      (get-link :ea:admin)))

; should return nil when getting a href which does not exist
(expect
  nil
  (-> (new-resource)
      (get-href :random)))

; add-resource adds an embedded resource
(expect
  (-> (new-resource)
      (add-links {:self        {:href "/orders/123"}
                  :ea:basket   {:href "/baskets/98712"}
                  :ea:customer {:href "/customers/7809"}})
      (add-properties {:total    30.0
                       :currency "USD"
                       :status   "shipped"}))
  (-> (new-resource)
      (add-resource
        :ea:order
        (-> (new-resource)
            (add-links {:self        {:href "/orders/123"}
                        :ea:basket   {:href "/baskets/98712"}
                        :ea:customer {:href "/customers/7809"}})
            (add-properties {:total    30.0
                             :currency "USD"
                             :status   "shipped"})))
      (get-resource :ea:order)))

; should be able to add a list of resources
(expect
  [(-> (new-resource)
       (add-links {:self        {:href "/orders/123"}
                   :ea:basket   {:href "/baskets/98712"}
                   :ea:customer {:href "/customers/7809"}})
       (add-properties {:total    30.0
                        :currency "USD"
                        :status   "shipped"}))
   (-> (new-resource)
       (add-links {:self        {:href "/orders/124"}
                   :ea:basket   {:href "/baskets/97213"}
                   :ea:customer {:href "/customers/12369"}})
       (add-properties {:total    20.0
                        :currency "USD"
                        :status   "processing"}))]
  (-> (new-resource)
      (add-resource
        :ea:order
        (list (-> (new-resource)
                  (add-links {:self        {:href "/orders/123"}
                              :ea:basket   {:href "/baskets/98712"}
                              :ea:customer {:href "/customers/7809"}})
                  (add-properties {:total    30.0
                                   :currency "USD"
                                   :status   "shipped"}))
              (-> (new-resource)
                  (add-links {:self        {:href "/orders/124"}
                              :ea:basket   {:href "/baskets/97213"}
                              :ea:customer {:href "/customers/12369"}})
                  (add-properties {:total    20.0
                                   :currency "USD"
                                   :status   "processing"}))))
      (get-resource :ea:order)))

; should be able to add a vector of resources
(expect
  [(-> (new-resource)
       (add-links {:self        {:href "/orders/123"}
                   :ea:basket   {:href "/baskets/98712"}
                   :ea:customer {:href "/customers/7809"}})
       (add-properties {:total    30.0
                        :currency "USD"
                        :status   "shipped"}))
   (-> (new-resource)
       (add-links {:self        {:href "/orders/124"}
                   :ea:basket   {:href "/baskets/97213"}
                   :ea:customer {:href "/customers/12369"}})
       (add-properties {:total    20.0
                        :currency "USD"
                        :status   "processing"}))]
  (-> (new-resource)
      (add-resource
        :ea:order
        [(-> (new-resource)
             (add-links {:self        {:href "/orders/123"}
                         :ea:basket   {:href "/baskets/98712"}
                         :ea:customer {:href "/customers/7809"}})
             (add-properties {:total    30.0
                              :currency "USD"
                              :status   "shipped"}))
         (-> (new-resource)
             (add-links {:self        {:href "/orders/124"}
                         :ea:basket   {:href "/baskets/97213"}
                         :ea:customer {:href "/customers/12369"}})
             (add-properties {:total    20.0
                              :currency "USD"
                              :status   "processing"}))])
      (get-resource :ea:order)))

; should append resources to existing lists of resources
(expect
  [(-> (new-resource)
       (add-links {:self        {:href "/orders/123"}
                   :ea:basket   {:href "/baskets/98712"}
                   :ea:customer {:href "/customers/7809"}})
       (add-properties {:total    30.0
                        :currency "USD"
                        :status   "shipped"}))
   (-> (new-resource)
       (add-links {:self        {:href "/orders/124"}
                   :ea:basket   {:href "/baskets/97213"}
                   :ea:customer {:href "/customers/12369"}})
       (add-properties {:total    20.0
                        :currency "USD"
                        :status   "processing"}))
   (-> (new-resource)
       (add-links {:self        {:href "/orders/125"}
                   :ea:basket   {:href "/baskets/98716"}
                   :ea:customer {:href "/customers/2416"}})
       (add-properties {:total    18.0
                        :currency "GBP"
                        :status   "shipped"}))]
  (-> (new-resource)
      (add-resource
        :ea:order
        [(-> (new-resource)
             (add-links {:self        {:href "/orders/123"}
                         :ea:basket   {:href "/baskets/98712"}
                         :ea:customer {:href "/customers/7809"}})
             (add-properties {:total    30.0
                              :currency "USD"
                              :status   "shipped"}))
         (-> (new-resource)
             (add-links {:self        {:href "/orders/124"}
                         :ea:basket   {:href "/baskets/97213"}
                         :ea:customer {:href "/customers/12369"}})
             (add-properties {:total    20.0
                              :currency "USD"
                              :status   "processing"}))])
      (add-resource
        :ea:order
        (-> (new-resource)
            (add-links {:self        {:href "/orders/125"}
                        :ea:basket   {:href "/baskets/98716"}
                        :ea:customer {:href "/customers/2416"}})
            (add-properties {:total    18.0
                             :currency "GBP"
                             :status   "shipped"})))
      (get-resource :ea:order)))

; should merge two lists of resources added separately
(expect
  [(-> (new-resource)
       (add-links {:self        {:href "/orders/123"}
                   :ea:basket   {:href "/baskets/98712"}
                   :ea:customer {:href "/customers/7809"}})
       (add-properties {:total    30.0
                        :currency "USD"
                        :status   "shipped"}))
   (-> (new-resource)
       (add-links {:self        {:href "/orders/124"}
                   :ea:basket   {:href "/baskets/97213"}
                   :ea:customer {:href "/customers/12369"}})
       (add-properties {:total    20.0
                        :currency "USD"
                        :status   "processing"}))
   (-> (new-resource)
       (add-links {:self        {:href "/orders/125"}
                   :ea:basket   {:href "/baskets/98716"}
                   :ea:customer {:href "/customers/2416"}})
       (add-properties {:total    18.0
                        :currency "GBP"
                        :status   "shipped"}))
   (-> (new-resource)
       (add-links {:self        {:href "/orders/127"}
                   :ea:basket   {:href "/baskets/98723"}
                   :ea:customer {:href "/customers/2161"}})
       (add-properties {:total    28.0
                        :currency "USD"
                        :status   "shipped"}))]
  (-> (new-resource)
      (add-resource
        :ea:order
        [(-> (new-resource)
             (add-links {:self        {:href "/orders/123"}
                         :ea:basket   {:href "/baskets/98712"}
                         :ea:customer {:href "/customers/7809"}})
             (add-properties {:total    30.0
                              :currency "USD"
                              :status   "shipped"}))
         (-> (new-resource)
             (add-links {:self        {:href "/orders/124"}
                         :ea:basket   {:href "/baskets/97213"}
                         :ea:customer {:href "/customers/12369"}})
             (add-properties {:total    20.0
                              :currency "USD"
                              :status   "processing"}))])
      (add-resource
        :ea:order
        (list (-> (new-resource)
                  (add-links {:self        {:href "/orders/125"}
                              :ea:basket   {:href "/baskets/98716"}
                              :ea:customer {:href "/customers/2416"}})
                  (add-properties {:total    18.0
                                   :currency "GBP"
                                   :status   "shipped"}))
              (-> (new-resource)
                  (add-links {:self        {:href "/orders/127"}
                              :ea:basket   {:href "/baskets/98723"}
                              :ea:customer {:href "/customers/2161"}})
                  (add-properties {:total    28.0
                                   :currency "USD"
                                   :status   "shipped"}))))
      (get-resource :ea:order)))

; should be able to add multiple resources and they should stack
(expect
  [(-> (new-resource)
       (add-links {:self        {:href "/orders/123"}
                   :ea:basket   {:href "/baskets/98712"}
                   :ea:customer {:href "/customers/7809"}})
       (add-properties {:total    30.0
                        :currency "USD"
                        :status   "shipped"}))
   (-> (new-resource)
       (add-links {:self        {:href "/orders/124"}
                   :ea:basket   {:href "/baskets/97213"}
                   :ea:customer {:href "/customers/12369"}})
       (add-properties {:total    20.0
                        :currency "USD"
                        :status   "processing"}))]
  (-> (new-resource)
      (add-resources
        :ea:order
        (-> (new-resource)
            (add-links {:self        {:href "/orders/123"}
                        :ea:basket   {:href "/baskets/98712"}
                        :ea:customer {:href "/customers/7809"}})
            (add-properties {:total    30.0
                             :currency "USD"
                             :status   "shipped"}))
        :ea:order
        (-> (new-resource)
            (add-links {:self        {:href "/orders/124"}
                        :ea:basket   {:href "/baskets/97213"}
                        :ea:customer {:href "/customers/12369"}})
            (add-properties {:total    20.0
                             :currency "USD"
                             :status   "processing"})))
      (get-resource :ea:order)))

; should be able to add and retrieve properties from the resource
(expect
  14
  (-> (new-resource)
      (add-property :currently-processing 14)
      (get-property :currently-processing)))

; should be able to add multiple properties
(let [resource (-> (new-resource)
                   (add-properties
                     :currently-processing 14
                     :shipped-today 20))]
  (expect 14 (get-property resource :currently-processing))
  (expect 20 (get-property resource :shipped-today)))

; should be able to add a map of properties
(let [resource (-> (new-resource)
                   (add-properties
                     {:currently-processing 14
                      :shipped-today        20}))]
  (expect 14 (get-property resource :currently-processing))
  (expect 20 (get-property resource :shipped-today)))

; should be able to navigate deep within properties
(expect
  20
  (-> (new-resource)
      (add-property :currently-processing {:uk 20 :de 12})
      (get-in-properties [:currently-processing :uk])))
