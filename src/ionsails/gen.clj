(ns ionsails.gen
  (:require [ionsails.noise.gen :as gen_noise]))


(defn get-noise [x]
  (let [amp 256]
    (Math/round (* amp (gen_noise/octave-perlin 6 0.5 512 x)))))


(defn get-noise2d [width height]
  (gen_noise/octave-perlin-over-ranges 8 0.25 (/ 1 8) width height))

(defn get-values [width height]
  (map (fn [[x y val]]
         [x y (int (Math/round (* 256 (/ (inc val) 2))))])
       (get-noise2d width height)))
