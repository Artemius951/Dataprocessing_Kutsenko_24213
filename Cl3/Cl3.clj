(defn pfilter
  ([pred coll]
   (pfilter pred coll 100 4))

  ([pred coll block-size parallelism]
   (when (<= block-size 0)
     (throw (IllegalArgumentException.
             "block-size must be positive")))

   (when (<= parallelism 0)
     (throw (IllegalArgumentException.
             "parallelism must be positive")))

   (let [blocks (partition-all block-size coll)]
     (letfn [(process-blocks [remaining-blocks]
               (lazy-seq
                (when-let [current-blocks
                           (seq (take parallelism remaining-blocks))]
                  (let [futures
                        (mapv
                         (fn [block]
                           (future
                             (doall (filter pred block))))
                         current-blocks)]
                    (concat
                     (mapcat deref futures)
                     (process-blocks
                      (drop parallelism remaining-blocks)))))))]
       (process-blocks blocks)))))


(defn test-pfilter []
  (try
    (assert
     (= '(2 4 6 8 10)
        (doall
         (pfilter even? (range 1 11)))))

    (assert
     (= (doall
         (filter #(zero? (mod % 3))
                 (range 100)))
        (doall
         (pfilter #(zero? (mod % 3))
                  (range 100)
                  10
                  4))))

    (assert
     (= '(0 2 4 6 8 10 12 14 16 18)
        (doall
         (take 10
               (pfilter even?
                        (range)
                        5
                        2)))))
    (assert
     (try
       (pfilter even? [1 2 3] 0 2)
       false
       (catch IllegalArgumentException _
         true)))

    (println "Все тесты пройдены успешно!")

    (catch AssertionError error
      (println "Тест провален:")
      (println (.getMessage error)))))

(defn timer-even [x]
  (Thread/sleep 10)
  (even? x))

(println "Обычный filter:")
(time
 (doall
  (filter timer-even (range 100))))

(println "Параллельный pfilter:")
(time
 (doall
  (pfilter timer-even (range 100) 10 4)))

(test-pfilter)