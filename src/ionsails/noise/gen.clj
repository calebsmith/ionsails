(ns ionsails.noise.gen
  (:require [ionsails.noise.perlin :as perlin]))

(defn get-range
  ([width]
         (for [x (range width)]
           (vector x)))
  ([width height]
         (for [y (range height)
               x (range width)]
           (vector x y)))
  ([width height depth]
         (for [z (range depth)
               y (range height)
               x (range width)]
           (vector x y z)))
  ([width height depth time4d]
         (for [w (range time4d)
               z (range depth)
               y (range height)
               x (range width)]
           (vector x y z w))))


(defn ranged-noise [noise-func rate & ranges]
  (map (fn [coord]
         (conj coord (apply noise-func
                            (cons
                              (* rate 2 (+ 1 (apply max ranges)))
                              (map inc coord)))))
       (apply get-range ranges)))

(def perlin-over-ranges (partial ranged-noise perlin/perlin))

(defn octave-perlin-over-ranges [octave persistance rate & ranges]
  (apply ranged-noise (concat
                        [
                         (partial perlin/octave-perlin octave persistance)
                         rate]
                        ranges)))
