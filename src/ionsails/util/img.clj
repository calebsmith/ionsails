(ns ionsails.util.img
  (:require [ionsails.gen :as gen]))

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

(defn draw-to-img-1d [width height noise-func]
  (let [img (make-img width height)]
    (doall
      (map (fn [x]
               (pset img x (mod (+ (noise-func x) (/ height 2)) height) Color/BLACK))
           (range width)))
    img))



(defn draw-to-img [width height noise-func]
  (let [img (make-img width height)]
    (doall
      (map (fn [[x y color]]
             (pset img x y color color color))
           (noise-func width height)))
    img))


;test scripts

(def draw-to-img-1d-example (partial draw-to-img-1d 800 400 gen/get-noise))

(def draw-to-img-example (partial draw-to-img 500 500 gen/get-values))

(defn go []
  (makeitso (draw-to-img-example) "resources/test.png"))

(defn go-1d []
  (makeitso (draw-to-img-1d-example) "resources/test.png"))
