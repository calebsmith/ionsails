(ns ionsails.img
  (:require [ionsails.noise :as noise]))

(use '[clojure.java.shell :only [sh]])

(import 'java.awt.image.BufferedImage 'javax.imageio.ImageIO 'java.awt.Color 'java.io.File)


(defn make-img [width height]
  (BufferedImage. width height BufferedImage/TYPE_INT_ARGB))

(defn pset
  ([img x y color] (.setRGB img x y (.getRGB color)))
  ([img x y r g b] (.setRGB img x y (.getRGB (Color. r g b)))))


(defn makeitso [img filename]
    (ImageIO/write img "png" (File. filename))
    (sh "open" filename))

(defn get-noise2 [x y]
  (let [factor 32]
    (int (* 8 (+ (/ factor 2) (Math/round (* factor (noise/perlin (/ x factor) (/ y factor) 0))))))))

(defn get-noise [x]
  (let [factor 128]
    (Math/round (* factor (noise/perlin (/ x factor) 0 0 0 )))))


(defn draw-to-img-1d []
  (let [height 400
        width 800
        img (make-img width height)]
    (doall
      (map (fn [x]
               (pset img x (mod (+ (get-noise x) (/ height 2)) height) Color/BLACK))
           (range width)))
    img))


(defn bound [val lower upper]
  (let [temp (if (< val lower) lower val)
   result (if (> temp upper) upper temp)]
    result))


(defn get-values [width height]
  (map (fn [vec] (let [x (first vec)
                       y (second vec)]
                   {:x x :y y :value (bound (get-noise2 x y) 0 255)}))
       (for [y (range height)
             x (range width)]
         (vector x y))))


(defn draw-to-img []
  (let [height 200
        width 200
        img (make-img width height)]
    (doall
      (map (fn [value]
             (let [{x :x y :y color :value} value]
               (pset img x y 0 (int (/ color 4)) color)))
           (get-values width height)))
    img))

; testing scripts

(defn go []
  (makeitso (draw-to-img) "resources/test.png"))

(defn go-1d []
  (makeitso (draw-to-img-1d) "resources/test.png"))
