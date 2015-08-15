(ns ionsails.noise.gen
  (:require [ionsails.noise.perlin :as perlin]))

(defn power-series
  "Given a base, returns the lazy sequence of powers from 0"
  [base]
  (map #(Math/pow base %) (range)))

(defn octave-perlin
  "Given a sample rate, number of octaves, a 'persistance' and 1 to 4 arguments
  for each dimenion, returns a sampled/scaled perlin noise with a sample for each 'octave',
  each applied to the previous depending on the amount of 'persistance'"
  [octaves persistance rate & args]
  (let [freqs (take octaves (power-series 2))
        amps (take octaves (power-series persistance))
        freqs-amps (map vector freqs amps)
        total (reduce +
                      (map (fn [[freq amp]]
                             (let [freqed-args (map #(* % freq) args)]
                               (* amp (apply perlin/perlin (cons rate freqed-args)))))
                           freqs-amps))
        maxval (reduce + amps)]
    (/ total maxval)))

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
                         (partial octave-perlin octave persistance)
                         rate]
                        ranges)))
