(ns halboy.data)

(defn transform-values [m f]
  (into {} (for [[k v] m] [k (f v)])))

(defn update-if-present [m ks fn]
  (if (get-in m ks)
    (update-in m ks #(fn %))
    m))
