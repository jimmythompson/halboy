(ns halboy.navigator-clj-http-client-test

  (:require [clojure.test :refer :all]
            [clojure.string :refer [capitalize]]
            [halboy.navigator :as navigator]
            [halboy.resource :as hal]
            [halboy.json :as json]
            [halboy.http.clj-http :as clj-http]
            [halboy.support.clj-http-api :as stubs]
            [clj-http.fake :refer [with-fake-routes-in-isolation]])
  (:import [java.net URL]
           [clojure.lang ExceptionInfo]))

(def base-url "https://service.example.com")
(defn- create-url [base-url resource]
  (-> (URL. base-url)
    (URL. resource)
    (.toString)))

(defn- create-user [name]
  (-> (hal/new-resource)
    (hal/add-link :self {:href (create-url base-url (format "/users/%s" name))})
    (hal/add-property :name (capitalize name))))

(def default-settings
  {:client (clj-http/new-http-client)})


(deftest halboy-navigator-clj-http-client
  (testing "should be able to retrieve the settings"
    (with-fake-routes-in-isolation
      (stubs/on-discover
        base-url
        :users {:href      "/users{?admin}"
                :templated true})
      (let [options (-> (navigator/discover base-url default-settings)
                      (navigator/settings))]
        (is (true? (:follow-redirects options)))
        (is (empty? (:headers options)))
        (is (not (nil? (:client options)))))))

  (testing "should be able to pass options to the HTTP client"
    (let [resource-url (create-url base-url "/users/thomas")]
      (with-fake-routes-in-isolation
        (merge
          (stubs/on-discover
            base-url
            :user {:href      "/users/{id}"
                   :templated true})
          (stubs/on-delete-with-headers
            resource-url
            {"Content-Type"    "application/json"
             "Accept"          "application/hal+json"
             "accept-encoding" "gzip, deflate"
             "My-Header"       "some-value"}
            {:status 204}))
        (let [result (->
                       (navigator/discover base-url
                         (merge default-settings
                           {:http {:headers {"My-Header" "some-value"}}}))
                       (navigator/delete :user {:id "thomas"}))
              status (navigator/status result)]
          (is (= 204 status))))))

  (testing "should be able to navigate through links in an API"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href      "/users{?admin}"
                  :templated true})
        (stubs/on-get
          (create-url base-url "/users")
          {:status 200
           :body   (-> (hal/new-resource "/users")
                     (hal/add-resources
                       :users (create-user "fred")
                       :users (create-user "sue")
                       :users (create-user "mary"))
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/get :users))
            status (navigator/status result)
            users (-> (navigator/resource result)
                    (hal/get-resource :users))]

        (is (= 200 status))

        (is (= ["Fred" "Sue" "Mary"]
              (map #(hal/get-property % :name) users))))))

  (testing "should throw an error when trying to get a link which does not exist"
    (with-fake-routes-in-isolation
      (stubs/on-discover base-url)
      (is (thrown-with-msg?
            ExceptionInfo
            #"Attempting to follow a link which does not exist"
            (-> (navigator/discover base-url default-settings)
              (navigator/get :users))))))

  (testing "should throw an error when the response is not JSON"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href      "/users{?admin}"
                  :templated true})
        (stubs/on-get
          (create-url base-url "/users")
          {:status 200
           :body   "I am not JSON"}))
      (is (thrown-with-msg?
            ExceptionInfo
            #"Response body was not valid JSON"
            (-> (navigator/discover base-url default-settings)
              (navigator/get :users))))))

  (testing "should be able to navigate through links with query params"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href      "/users{?admin}"
                  :templated true})
        (stubs/on-get
          (create-url base-url "/users")
          {:admin "true"}
          {:status 200
           :body   (-> (hal/new-resource "/users")
                     (hal/add-resources
                       :users (create-user "fred")
                       :users (create-user "sue")
                       :users (create-user "mary"))
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/get :users {:admin true}))
            status (navigator/status result)
            users (-> (navigator/resource result)
                    (hal/get-resource :users))]

        (is (= 200 status))

        (is (= ["Fred" "Sue" "Mary"]
              (map #(hal/get-property % :name) users))))))

  (testing "should be able to navigate through links with multiple query params"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href      "/users{?admin,owner}"
                  :templated true})
        (stubs/on-get
          (create-url base-url "/users")
          {:admin "true"
           :owner "false"}
          {:status 200
           :body   (-> (hal/new-resource "/users")
                     (hal/add-resources
                       :users (create-user "fred")
                       :users (create-user "sue"))
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/get :users {:admin true
                                            :owner false}))
            status (navigator/status result)
            users (-> (navigator/resource result)
                    (hal/get-resource :users))]

        (is (= 200 status))

        (is (= ["Fred" "Sue"]
              (map #(hal/get-property % :name) users))))))

  (testing "should be able to navigate with a mixture of template and query params"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :friends {:href      "/users/{id}/friends{?mutual}"
                    :templated true})
        (stubs/on-get
          (create-url base-url "/users/thomas/friends") {:mutual "true"}
          {:status 200
           :body   (-> (hal/new-resource "/users/thomas/friends")
                     (hal/add-resources
                       :users (create-user "fred")
                       :users (create-user "sue")
                       :users (create-user "mary"))
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/get :friends {:id "thomas" :mutual true}))
            status (navigator/status result)
            users (-> (navigator/resource result)
                    (hal/get-resource :users))]

        (is (= 200 status))

        (is (= ["Fred" "Sue" "Mary"]
              (map #(hal/get-property % :name) users))))))

  (testing "should be able to create resources in an API"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href "/users"})
        (stubs/on-post-redirect
          (create-url base-url "/users")
          {:name "Thomas"}
          "/users/thomas")
        (stubs/on-get
          (create-url base-url "/users/thomas")
          {:status 200
           :body   (-> (hal/new-resource "/users/thomas")
                     (hal/add-property :name "Thomas")
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/post :users {:name "Thomas"}))
            status (navigator/status result)
            new-user (navigator/resource result)]

        (is (= 200 status))
        (is (= "Thomas" (hal/get-property new-user :name))))))

  (testing "should be able to remove resources in an API"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :user {:href      "/users/{id}"
                 :templated true})
        (stubs/on-delete
          (create-url base-url "/users/thomas")
          {:status 204}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/delete :user {:id "thomas"}))
            status (navigator/status result)]
        (is (= 204 status)))))

  (testing "should be able to update resources in an API"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :user {:href      "/users/{id}"
                 :templated true})
        (stubs/on-patch-redirect
          (create-url base-url "/users/thomas")
          {:surname "Svensson"}
          "/users/thomas")
        (stubs/on-get
          (create-url base-url "/users/thomas")
          {:status 200
           :body   (-> (hal/new-resource "/users/thomas")
                     (hal/add-property :name "Thomas")
                     (hal/add-property :surname "Svensson")
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/patch :user {:id "thomas"} {:surname "Svensson"}))
            status (navigator/status result)
            new-user (navigator/resource result)]

        (is (= 200 status))
        (is (= "Thomas" (hal/get-property new-user :name)))
        (is (= "Svensson" (hal/get-property new-user :surname))))))

  (testing "should handle query params on post"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href      "/users{?first,second}"
                  :templated true})
        (stubs/on-post-redirect
          (create-url base-url "/users")
          {:name "Thomas"}
          "/users/thomas")
        (stubs/on-get
          (create-url base-url "/users/thomas")
          {:status 200
           :body   (-> (hal/new-resource)
                     (hal/add-link :self "/users/thomas")
                     (hal/add-property :name "Thomas")
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/post :users {:name "Thomas"}))
            status (navigator/status result)
            new-user (navigator/resource result)]

        (is (= 200 status))
        (is (= "Thomas" (hal/get-property new-user :name))))))

  (testing "should handle query params on delete"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href      "/users{?name}"
                  :templated true})
        (stubs/on-delete
          (create-url base-url "/users") {:name "thomas"}
          {:status 204}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/delete :users {:name "thomas"}))
            status (navigator/status result)]
        (is (= 204 status)))))

  (testing "should be able to use template params when creating resources"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :useritems {:href      "/users/{id}/items"
                      :templated true})
        (stubs/on-post-redirect
          (create-url base-url "/users/thomas/items")
          {:name "Sponge"}
          "/users/thomas/items/1")
        (stubs/on-get
          (create-url base-url "/users/thomas/items/1")
          {:status 200
           :body   (-> (hal/new-resource "/users/thomas/items/1")
                     (hal/add-property :name "Sponge")
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/post :useritems {:id "thomas"} {:name "Sponge"}))
            status (navigator/status result)
            new-item (navigator/resource result)]

        (is (= 200 status))
        (is (= "Sponge" (hal/get-property new-item :name))))))

  (testing "should not follow location headers when the status is not 201"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href      "/users{?admin}"
                  :templated true})
        (stubs/on-post
          (create-url base-url "/users")
          {:name "Thomas"}
          {:status 400}))
      (let [status (-> (navigator/discover base-url default-settings)
                     (navigator/post :users {:name "Thomas"})
                     (navigator/status))]
        (is (= 400 status)))))

  (testing "should not follow location headers when the options say not to"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href      "/users{?admin}"
                  :templated true})
        (stubs/on-post-redirect
          (create-url base-url "/users")
          {:name "Thomas"}
          "/users/thomas"))
      (let [result (-> (navigator/discover base-url (merge default-settings {:follow-redirects false}))
                     (navigator/post :users {:name "Thomas"}))
            status (navigator/status result)]

        (is (= 201 status))

        (is (= "/users/thomas"
              (navigator/get-header result :location))))))

  (testing "should be able to continue the conversation even if we do not follow redirects"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href "/users"})
        (stubs/on-post-redirect
          (create-url base-url "/users")
          {:name "Thomas"}
          "/users/thomas")
        (stubs/on-get
          (create-url base-url "/users/thomas")
          {:status 200
           :body   (-> (hal/new-resource "/users/thomas")
                     (hal/add-property :name "Thomas")
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url (merge default-settings {:follow-redirects false}))
                     (navigator/post :users {:name "Thomas"})
                     (navigator/follow-redirect))
            status (navigator/status result)
            new-user (navigator/resource result)]

        (is (= 200 status))
        (is (= "Thomas" (hal/get-property new-user :name))))))

  (testing "should throw when trying to follow a redirect without a location header"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :users {:href "/users"})
        (stubs/on-post
          (create-url base-url "/users")
          {:name "Thomas"}
          {:status  201
           :headers {}}))
      (is (thrown? ExceptionInfo
            (-> (navigator/discover base-url (merge default-settings {:follow-redirects false}))
              (navigator/post :users {:name "Thomas"})
              (navigator/follow-redirect))))))

  (testing "should be able to put to a resource"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :user {:href      "/users/{id}"
                 :templated true})
        (stubs/on-put
          (create-url base-url "/users/thomas")
          {:name "Thomas"}
          {:status 200
           :body   (-> (hal/new-resource "/users/thomas")
                     (hal/add-property :name "Thomas")
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/put :user {:id "thomas"} {:name "Thomas"}))
            status (navigator/status result)
            user (navigator/resource result)]

        (is (= 200 status))
        (is (= "Thomas" (hal/get-property user :name))))))

  (testing "should follow redirects when putting to a resource returns a 201"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :user {:href      "/users/{id}"
                 :templated true})
        (stubs/on-put-redirect
          (create-url base-url "/users/thomas")
          {:name "Thomas"}
          "/users/thomas")
        (stubs/on-get
          (create-url base-url "/users/thomas")
          {:status 200
           :body   (-> (hal/new-resource "/users/thomas")
                     (hal/add-property :name "Thomas")
                     (json/resource->json))}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/put :user {:id "thomas"} {:name "Thomas"}))
            status (navigator/status result)
            user (navigator/resource result)]

        (is (= 200 status))
        (is (= "Thomas" (hal/get-property user :name))))))

  (testing "should be able to head to a resource"
    (with-fake-routes-in-isolation
      (merge
        (stubs/on-discover
          base-url
          :user {:href      "/users/{id}"
                 :templated true})
        (stubs/on-head
          (create-url base-url "/users/thomas")
          {:status 200}))
      (let [result (-> (navigator/discover base-url default-settings)
                     (navigator/head :user {:id "thomas"}))
            status (navigator/status result)]

        (is (= 200 status)))))

  (testing "should be able to set header for delete"
    (let [resource-url (create-url base-url "/users/thomas")]
      (with-fake-routes-in-isolation
        (merge
          (stubs/on-discover
            base-url
            :user {:href      "/users/{id}"
                   :templated true})
          (stubs/on-delete-with-headers
            resource-url
            {"Content-Type"        "application/json"
             "Accept"              "application/hal+json"
             "accept-encoding"     "gzip, deflate"
             "X-resource-location" resource-url}
            {:status 204}))
        (let [result (->
                       (navigator/discover base-url default-settings)
                       (navigator/set-header "X-resource-location" resource-url)
                       (navigator/delete :user {:id "thomas"}))
              status (navigator/status result)]
          (is (= 204 status))))))

  (testing "should be able to set header for post"
    (let [resource-url (create-url base-url "/users/thomas")]
      (with-fake-routes-in-isolation
        (merge
          (stubs/on-discover
            base-url
            :users {:href "/users"})
          (stubs/on-post-with-headers
            (create-url base-url "/users")
            {"Content-Type"        "application/json"
             "Accept"              "application/hal+json"
             "accept-encoding"     "gzip, deflate"
             "X-resource-location" resource-url}
            {:name "Thomas"}
            {:status 201}))
        (let [result (-> (navigator/discover base-url  (merge default-settings {:follow-redirects false}))
                       (navigator/set-header "X-resource-location" resource-url)
                       (navigator/post :users {:name "Thomas"}))
              status (navigator/status result)]

          (is (= 201 status))))))

  (testing "should be able to resume conversations"
    (with-fake-routes-in-isolation
      (stubs/on-get
        (create-url base-url "/users")
        {:status 200
         :body   (-> (hal/new-resource "/users")
                   (hal/add-resources
                     :users [(create-user "fred")
                             (create-user "sue")
                             (create-user "mary")])
                   (json/resource->json))})
      (let [resource (-> (hal/new-resource base-url)
                       (hal/add-link :users {:href      "/users{?admin}"
                                             :templated true}))
            result (-> (navigator/resume resource default-settings)
                     (navigator/get :users))
            status (navigator/status result)
            users (-> (navigator/resource result)
                    (hal/get-resource :users))]

        (is (= 200 status))

        (is (= ["Fred" "Sue" "Mary"]
              (map #(hal/get-property % :name) users))))))

  (testing "should be able to hint at the location when the self link is not absolute"
    (with-fake-routes-in-isolation
      (stubs/on-get
        (create-url base-url "/users")
        {:status 200
         :body   (-> (hal/new-resource "/users")
                   (hal/add-resources
                     :users [(create-user "fred")
                             (create-user "sue")
                             (create-user "mary")])
                   (json/resource->json))})
      (let [resource (-> (hal/new-resource "/")
                       (hal/add-link :users {:href      "/users{?admin}"
                                             :templated true}))
            result (-> (navigator/resume resource (merge default-settings
                                                    {:resume-from base-url}))
                     (navigator/get :users))
            status (navigator/status result)
            users (-> (navigator/resource result)
                    (hal/get-resource :users))]

        (is (= 200 status))

        (is (= ["Fred" "Sue" "Mary"]
              (map #(hal/get-property % :name) users))))))

  (testing "should throw an error when trying to resume a conversation
          with a resource that lacks a self link"
    (is (thrown? ExceptionInfo
          (navigator/resume
            (hal/new-resource)
            default-settings))))

  (testing "should throw an error when trying to resume a conversation
          with a resource with a relative self link, and no :resume-from option"
    (is (thrown? ExceptionInfo
          (navigator/resume
            (-> (hal/new-resource)
              (hal/add-href :self "/users"))
            default-settings)))))
