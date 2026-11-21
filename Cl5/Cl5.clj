(ns cl5
  (:gen-class))

(defn sleep-ms [n]
  (Thread/sleep (long n)))

(defn take-forks!
  [left right attempts]
  (loop []
    (let [taken?
          (dosync
           (swap! attempts inc)
           (ensure left)
           (ensure right)
           (if (and (pos? (:available @left))
                    (pos? (:available @right)))
             (do
               (alter left update :available dec)
               (alter left update :uses inc)
               (alter right update :available dec)
               (alter right update :uses inc)
               true)
             false))]
      (if taken?
        true
        (do
          (sleep-ms 1)
          (recur))))))

(defn release-forks!
  [left right]
  (dosync
   (alter left update :available inc)
   (alter right update :available inc)))

(defn philosopher
  [id forks meals think-ms eat-ms attempts meals-done]
  (let [left (forks (mod id (count forks)))
        right (forks (mod (inc id) (count forks)))]
    (dotimes [_ meals]
      (sleep-ms think-ms)
      (take-forks! left right attempts)
      (sleep-ms eat-ms)
      (release-forks! left right)
      (swap! meals-done inc))))

(defn run_simul!
  [{:keys [philosophers meals think-ms eat-ms]
    :or {philosophers 5
         meals 100
         think-ms 10
         eat-ms 10}}]
  (when (< philosophers 2)
    (throw
     (ex-info
      "Количество философов должно быть не меньше 2"
      {:philosophers philosophers})))
  (let [forks
        (vec
         (repeatedly philosophers
                     #(ref {:available 1
                            :uses 0})))

        attempts (atom 0)
        meals-done (atom 0)
        started-at (System/nanoTime)

        workers
        (doall
         (for [id (range philosophers)]
           (future
             (philosopher
              id
              forks
              meals
              think-ms
              eat-ms
              attempts
              meals-done))))]

    (doseq [worker workers]
      @worker)

    (let [elapsed-ms
          (/ (- (System/nanoTime) started-at) 1e6)

          successful-meals @meals-done
          transaction-executions @attempts]

      {:philosophers philosophers
       :meals successful-meals
       :elapsed-ms elapsed-ms
       :transaction-executions transaction-executions
       :additional-executions
       (- transaction-executions successful-meals)
       :fork-usage
       (mapv :uses (mapv deref forks))})))
