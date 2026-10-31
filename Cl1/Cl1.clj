(defn cl1
  [alphabet n]
  (reduce
   (fn [partial-strings _]
     (reduce
      concat
      '()
      (map
       (fn [s]
         (map
          (fn [ch]
            (str s ch))
          (remove
           (fn [ch]
             (and
              (seq s)
              (= (str (last s)) ch)))
           alphabet)))
       partial-strings)))
   '("")
   (range n)))