(ns ionsails.gen
  (:require [ionsails.noise.perlin :as noise]
            [ionsails.noise.gen :as gen_noise]))


(defn get-noise [x]
  (let [amp 256]
    (Math/round (* amp (noise/octave-perlin 6 0.5 512 x)))))


(defn get-values [width height]
  (map (fn [[x y val]]
         [x y (int (Math/round (* 256 (/ (inc val) 2))))])
       (gen_noise/octave-perlin-over-ranges 8 0.65 (/ 1 8) width height)))
