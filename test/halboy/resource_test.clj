(ns halboy.resource-test
  (:require
    [expectations :refer :all]
    [halboy.resource :as halboy]
    [halboy.resource :refer :all]))

(defn- new-embedded-resource [links properties]
  (new-resource links {} properties))

; should be able to add and retrieve links from the resource
(expect
  {:href "/orders"}
  (-> (new-resource)
      (add-link :self {:href "/orders"})
      (get-link :self)))

; should be able to add multiple links under the same rel, and they should stack
(expect
  [{:href "/admins/2", :title "Fred"}
   {:href "/admins/5", :title "Kate"}]
  (-> (new-resource)
      (add-links
        :ea:admin {:href "/admins/2", :title "Fred"}
        :ea:admin {:href "/admins/5", :title "Kate"})
      (get-link :ea:admin)))

; add-resource adds an embedded resource
(expect
  (new-embedded-resource
    {:self        {:href "/orders/123"},
     :ea:basket   {:href "/baskets/98712"},
     :ea:customer {:href "/customers/7809"}}
    {:total    30.0,
     :currency "USD",
     :status   "shipped"})
  (-> (new-resource)
      (add-resource
        :ea:order (new-embedded-resource
                    {:self        {:href "/orders/123"},
                     :ea:basket   {:href "/baskets/98712"},
                     :ea:customer {:href "/customers/7809"}}
                    {:total    30.0,
                     :currency "USD",
                     :status   "shipped"}))
      (get-resource :ea:order)))

; should be able to add multiple resources, and they should stack
(expect
  [(new-embedded-resource
     {:self        {:href "/orders/123"},
      :ea:basket   {:href "/baskets/98712"},
      :ea:customer {:href "/customers/7809"}}
     {:total    30.0,
      :currency "USD",
      :status   "shipped"})
   (new-embedded-resource
     {:self        {:href "/orders/124"},
      :ea:basket   {:href "/baskets/97213"},
      :ea:customer {:href "/customers/12369"}}
     {:total    20.0,
      :currency "USD",
      :status   "processing"})]
  (-> (new-resource)
      (add-resources
        :ea:order (new-embedded-resource
                    {:self        {:href "/orders/123"},
                     :ea:basket   {:href "/baskets/98712"},
                     :ea:customer {:href "/customers/7809"}}
                    {:total    30.0,
                     :currency "USD",
                     :status   "shipped"})
        :ea:order (new-embedded-resource
                    {:self        {:href "/orders/124"},
                     :ea:basket   {:href "/baskets/97213"},
                     :ea:customer {:href "/customers/12369"}}
                    {:total    20.0,
                     :currency "USD",
                     :status   "processing"}))
      (get-resource :ea:order)))

; should be able to add and retrieve properties from the resource
(expect
  14
  (-> (new-resource)
      (add-property :currently-processing 14)
      (get-property :currently-processing)))

; add-properties adds multiple properties to the resource
(let [resource (-> (new-resource)
                   (add-properties
                     :currently-processing 14
                     :shipped-today 20))]
  (expect 14 (get-property resource :currently-processing))
  (expect 20 (get-property resource :shipped-today)))
