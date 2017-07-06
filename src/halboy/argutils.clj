(ns halboy.argutils)

(defn- map->key-value-pairs [m]
  (-> (seq m)
      flatten))

(defn- arg-map? [kvs]
  (and (= 1 (count kvs)) (map? (first kvs))))

(defn ensure-key-value-pairs [n]
  (if (arg-map? n)
    (-> (first n)
        map->key-value-pairs)
    n))

(defn apply-pairs [f resource kvs]
  (if kvs
    (if (next kvs)
      (let [rel (first kvs)
            val (second kvs)]
        (recur f (f resource rel val) (nnext kvs)))
      (throw (IllegalArgumentException.
               "expected an even number of arguments, found odd number")))
    resource))

(defn apply-pairs-or-map [f resource kvs]
  (apply-pairs f resource (ensure-key-value-pairs kvs)))