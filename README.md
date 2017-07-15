# halboy

[![Current Version](https://clojars.org/halboy/latest-version.svg)](https://clojars.org/halboy)

A Clojure library for all things hypermedia.

* Create hypermedia resources
* Marshal to and from JSON, or a map
* Navigate JSON+HAL APIs

## API

### Resources

With Halboy you can create resources, and pull information from them.

```clojure
(require '[halboy.resource :as hal])

(def my-resource
    (-> (hal/new-resource {:href "/orders/123"})
        (hal/add-link :creator {:href "/users/rob"})
        (hal/add-resource :items (-> (hal/new-resource {:href "/items/534"})
                                     (hal/add-property :price 25.48)))
        (hal/add-property :state :dispatching)))

(hal/get-link my-resource :self)
; { :href "/orders/123" }

(hal/get-href my-resource :creator)
; "/users/rob"

(hal/get-property my-resource :state)
; :dispatching

(-> (hal/get-resource my-resource :items)
    (hal/get-property :price))
; 25.48
```

### Marshalling

You can also marshal your hal resources to and from maps, or JSON.

```clojure
(require '[halboy.resource :as hal])
(require '[halboy.json :as haljson])

(def my-resource
    (-> (hal/new-resource {:href "/orders/123"})
        (hal/add-link :creator {:href "/users/rob"})
        (hal/add-resource :items (-> (hal/new-resource {:href "/items/534"})
                                     (hal/add-property :price 25.48)))
        (hal/add-property :state :dispatching)))

(haljson/resource->map my-resource)
; { :_links { :self { :href "/orders/123" },
;           :creator { :href "/users/rob" } },
;   :_embedded {:items { :_links { :self { :href "/items/534" } },
;                      :price 25.48 } },
;   :state :dispatching }

(haljson/resource->json my-resource)
; Formatted in these docs only.
;
; {
;   \"_links\": {
;     \"self\": {
;       \"href\": \"/orders/123\"
;     },
;     \"creator\": {
;       \"href\": \"/users/rob\"
;     }
;   },
;   \"_embedded\": {
;     \"items\": {
;       \"_links\": {
;         \"self\": {
;           \"href\": \"/items/534\"
;         }
;       },
;       \"price\": 25.48
;     }
;   },
;   \"state\": \"dispatching\"
; }

(-> (haljson/resource->json my-resource)
    (haljson/json->resource)
    (hal/get-href :self))
; "/orders/123"
```

### Navigation

Provided you're calling a HAL+JSON API, you can discover the API and navigate
through its links. When you've found what you want, you call
`navigator/resource` and you get a plain old hal reosurce, which you can inspect
using any of the methods above.

```clojure
(require '[halboy.resource :as hal])
(require '[halboy.navigator :as navigator])

; GET / - 200 OK
; {
;  "_links": {
;    "self": {
;      "href": "/"
;    },
;    "users": {
;      "href": "/users"
;    },
;    "user": {
;      "href": "/users/{id}",
;      "templated": true
;    }
;  }
;}

(-> (navigator/discover "https://api.example.com/")
    (navigator/get :users)
    (navigator/location))
; "https://api.example.com/users"

(-> (navigator/discover "https://api.example.com/")
    (navigator/get :user {:id "rob"})
    (navigator/location))
; "https://api.example.com/users/rob"

(def sue-result
     (-> (navigator/discover "https://api.example.com/")
         (navigator/post :users {:id "sue" :name "Sue" :title "Dev"}))

(navigator/location sue-result)
; "https://api.example.com/users/sue"

(-> (navigator/resource sue-result)
    (hal/get-property :title))
; "Dev"
```

## Contributing

I'm happy to receive and go through feedback, bug reports, and pull requests.

If you need to contact me, my email is jimmy[at]jimmythompson.co.uk.

### Development 
To run the tests:

```sh
$ lein test
```
