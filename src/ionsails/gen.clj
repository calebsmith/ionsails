(ns ionsails.gen
  (:require [ionsails.noise :as noise]))


(defn bound [val lower upper]
  (let [temp (if (< val lower) lower val)
   result (if (> temp upper) upper temp)]
    result))

(defn get-noise [x]
  (let [factor 128]
    (Math/round (* factor (noise/perlin (/ x factor) 0 0 0 )))))


(defn get-noise2 [x y]
  (let [factor 128]
    (inc (int (* 1 (+ (/ factor 2) (* factor (noise/perlin (/ x factor) (/ y factor) 255))))))))

(defn get-values [width height]
  (map (fn [vec] (let [x (first vec)
                       y (second vec)]
                   {:x x :y y :value (bound (get-noise2 x y) 0 255)}))
       (for [y (range height)
             x (range width)]
         (vector x y))))
