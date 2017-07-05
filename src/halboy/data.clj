(ns halboy.data)

(defn transform-values [m f]
  (into {} (for [[k v] m] [k (f v)])))