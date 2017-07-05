(ns halboy.resource-test
  (:require
    [expectations :refer :all]
    [halboy.resource :as halboy]
    [halboy.resource
     :refer [new-resource
             add-link
             add-resource
             add-property
             get-link
             get-embedded
             get-property]]))

(defn- new-embedded-resource [links properties]
  (new-resource links {} properties))

(let [resource
      (-> (new-resource)
          (add-link :self {:href "/orders"})
          (add-link :curies {:name      "ea",
                             :href      "http://example.com/docs/rels/{rel}",
                             :templated true})
          (add-link :next {:href "/orders?page=2"})
          (add-link :ea:find {:href      "/orders{?id}",
                              :templated true})
          (add-link :ea:admin {:href "/admins/2", :title "Fred"})
          (add-link :ea:admin {:href "/admins/5", :title "Kate"})
          (add-resource :ea:order (new-embedded-resource
                                    {:self        {:href "/orders/123"},
                                     :ea:basket   {:href "/baskets/98712"},
                                     :ea:customer {:href "/customers/7809"}}
                                    {:total    30.0,
                                     :currency "USD",
                                     :status   "shipped"}))
          (add-resource :ea:order (new-embedded-resource
                                    {:self        {:href "/orders/124"},
                                     :ea:basket   {:href "/baskets/97213"},
                                     :ea:customer {:href "/customers/12369"}}
                                    {:total    20.0,
                                     :currency "USD",
                                     :status   "processing"}))
          (add-property :currently-processing 14)
          (add-property :shipped-today 20))]

  ; get-link should get a link from a resource
  (expect
    {:href "/orders?page=2"}
    (get-link resource :next))

  ; get-link should get multiple links if they have been added
  (expect
    [{:href "/admins/2", :title "Fred"}
     {:href "/admins/5", :title "Kate"}]
    (get-link resource :ea:admin))

  ; get-embedded should get an embedded resource
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
    (get-embedded resource :ea:order))

  ; get-property should get a property from the body
  (expect
    14
    (get-property resource :currently-processing)))
