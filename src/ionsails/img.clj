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
    (int (* 2.25 (+ (/ factor 2) (Math/round (* factor (noise/perlin (/ x factor) (/ y factor)))))))))

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


(defn draw-to-img []
  (let [height 200
        width 200
        img (make-img width height)]
    (doall
      (map (fn [y]
             (doall (map (fn [x]
                           (let [color (bound (get-noise2 x y) 0 255)]
                             (pset img x y color color color)))
                         (range width))))
           (range height)))
    img))

(defn go []
  (makeitso (draw-to-img) "resources/test.png"))

(defn go-1d []
  (makeitso (draw-to-img-1d) "resources/test.png"))






(comment
  "
  public double OctavePerlin(double x, double y, double z, int octaves, double persistence) {
      double total = 0;
      double frequency = 1;
      double amplitude = 1;
      double maxValue = 0;  // Used for normalizing result to 0.0 - 1.0
      for(int i=0;i<octaves;i++) {
          total += perlin(x * frequency, y * frequency, z * frequency) * amplitude;
          maxValue += amplitude;
          amplitude *= persistence;
          frequency *= 2;
      }
      return total/maxValue;
  }
  "
)
