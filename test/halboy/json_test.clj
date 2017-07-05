(ns halboy.json-test
  (:use clojure.pprint)
  (:require
    [expectations :refer :all]
    [halboy.resource
     :refer [new-resource
             add-link
             add-resource
             add-property]]
    [halboy.json :refer [json->resource resource->json]]
    [cheshire.core :as json]))

; json->resource

; resource->json
(let [resource (-> (new-resource)
                   (add-link :self {:href "/orders"})
                   (add-link :curies {:name      "ea",
                                      :href      "http://example.com/docs/rels/{rel}",
                                      :templated true})
                   (add-link :next {:href "/orders?page=2"})
                   (add-link :ea:find {:href      "/orders{?id}",
                                       :templated true})
                   (add-link :ea:admin {:href "/admins/2", :title "Fred"})
                   (add-link :ea:admin {:href "/admins/5", :title "Kate"})
                   (add-resource :ea:order (new-resource
                                             {:self        {:href "/orders/123"},
                                              :ea:basket   {:href "/baskets/98712"},
                                              :ea:customer {:href "/customers/7809"}}
                                             {}
                                             {:total    30.0,
                                              :currency "USD",
                                              :status   "shipped"}))
                   (add-resource :ea:order (new-resource
                                             {:self        {:href "/orders/124"},
                                              :ea:basket   {:href "/baskets/97213"},
                                              :ea:customer {:href "/customers/12369"}}
                                             {}
                                             {:total    20.0,
                                              :currency "USD",
                                              :status   "processing"}))
                   (add-property :currently-processing 14)
                   (add-property :shipped-today 20))
      json-representation (json/generate-string
                            {:_links               {:self     {:href "/orders"},
                                                    :curies   {:name      "ea",
                                                               :href      "http://example.com/docs/rels/{rel}",
                                                               :templated true},
                                                    :next     {:href "/orders?page=2"},
                                                    :ea:find  {:href      "/orders{?id}",
                                                               :templated true},
                                                    :ea:admin [{:href "/admins/2", :title "Fred"}
                                                               {:href "/admins/5", :title "Kate"}]}
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

  ; should marshal a resource into some json
  (expect
    json-representation
    (resource->json resource))

  ; should parse some json into a resource
  (expect
    resource
    (json->resource json-representation)))