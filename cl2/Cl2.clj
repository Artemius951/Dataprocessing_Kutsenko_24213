(defn cl2 []
  (letfn [(sieve [numbers]
            (lazy-seq
             (let [prime (first numbers)
                   remaining (rest numbers)]
               (cons prime
                     (sieve
                      (remove #(zero? (mod % prime))
                              remaining))))))]
    (sieve (iterate inc 2))))

(defn test-prime-numbers []
  (let [primes (cl2)]

      (assert (= '(2 3 5 7 11 13 17 19 23 29)
                 (take 10 primes)))

      (assert (= 541
                 (nth primes 99)))

      (assert (apply < (take 100 primes)))

      (assert
       (every?
        (fn [number]
          (not-any?
           #(zero? (mod number %))
           (range 2
                  (inc (long (Math/sqrt number))))))
        (take 100 primes)))

      (println "Все тесты пройдены успешно!")))

(test-prime-numbers)