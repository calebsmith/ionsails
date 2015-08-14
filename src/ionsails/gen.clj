(ns ionsails.gen
  (:require [ionsails.noise :as noise]))


(defn bound [val lower upper]
  (let [temp (if (< val lower) lower val)
   result (if (> temp upper) upper temp)]
    result))



(defn get-noise2 [x y]
  (let [factor 32
        amp 16]
    (int (* 8 (+ (/ factor 2) (* amp (noise/octave-perlin (/ x 256) (/ y 256) (/ x 256) 5 0.5)))))))


(defn get-noise [x]
  (let [factor 128]
    (Math/round (* factor (noise/octave-perlin (/ x factor) 0 0 4 0.5)))))


(defn get-values [width height]
  (map (fn [[x y]]
         [x y (bound (get-noise2 x y) 0 255)])
       (for [y (range height)
             x (range width)]
         (vector x y))))



