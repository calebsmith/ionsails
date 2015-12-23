(ns ionsails-web.util.string)

(defn splitter [s]
  ((fn step [xys]
     (lazy-seq
      (when-let [c (ffirst xys)]
        (cond
          (= " " c)
          (step (rest xys))
          (= \" c)
          (let [[w* r*]
                (split-with (fn [[x y]]
                              (or (not= \" x)
                                  (not (or (nil? y)
                                           (= " " y)))))
                            (rest xys))]
            (if (= \" (ffirst r*))
              (cons (apply str (map first w*)) (step (rest r*)))
              (cons (apply str (map first w*)) nil)))
          :else
          (let [[w r] (split-with (fn [[x y]] (not (= " " x))) xys)]
            (cons (apply str (map first w)) (step r)))))))
   (partition 2 1 (lazy-cat s [nil]))))

(comment

  (splitter "A b \"as sdf\" ")

  )
