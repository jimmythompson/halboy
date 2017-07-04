(ns halboy.resource-test
  (:require [halresource.resource
             :refer [new-resource
                     add-link]]
            [expectations :refer :all]
            [halboy.resource :as halboy]))

(let [resource
      (halboy/new-resource
        {:self     {:href "/orders"},
         :curies   [{:name      "ea",
                     :href      "http://example.com/docs/rels/{rel}",
                     :templated true}],
         :next     {:href "/orders?page=2"},
         :ea:find  {:href      "/orders{?id}",
                    :templated true},
         :ea:admin [{:href "/admins/2", :title "Fred"}
                    {:href "/admins/5", :title "Kate"}]}
        {:ea:order [(halboy/new-embedded-resource
                      {:self        {:href "/orders/123"},
                       :ea:basket   {:href "/baskets/98712"},
                       :ea:customer {:href "/customers/7809"}}
                      {:total    30.0,
                       :currency "USD",
                       :status   "shipped"})
                    (halboy/new-embedded-resource
                      {:self        {:href "/orders/124"},
                       :ea:basket   {:href "/baskets/97213"},
                       :ea:customer {:href "/customers/12369"}}
                      {:total    20.0,
                       :currency "USD",
                       :status   "processing"})]}
        {:currently-processing 14
         :shipped-today        20})]

  ; get-link should get a link from a resource
  (expect
    "/orders?page=2"
    (halboy/get-link resource :next))

  ; get-link should expand templated urls
  (expect
    "/orders/my-order"
    (halboy/get-link resource :ea:find {:id "my-order"}))

  ; get-link should remove unresolved template parameters
  (expect
    "/orders"
    (halboy/get-link resource :ea:find))

  ; get-embedded should get an embedded resource
  (expect
    (halboy/new-embedded-resource
      {:self        {:href "/orders/123"},
       :ea:basket   {:href "/baskets/98712"},
       :ea:customer {:href "/customers/7809"}}
      {:total    30.0,
       :currency "USD",
       :status   "shipped"})
    (-> (halboy/get-embedded resource :ea:order)
        first))

  ; get-property should get a property from the body
  (expect
    14
    (halboy/get-property resource :currently-processing)))