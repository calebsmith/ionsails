(ns ionsails.core
  (:require [lanterna.screen :as s]
            [ionsails.ui :as ui]
            [ionsails.noise.gen :as gen_noise])
  (:gen-class))


(def default-tileset [
  [" " :black :black]
  [" " :black :blue]
  ["." :yellow :green]
  ["." :black :green]
  ["#" :yellow :green]
  ["@" :yellow :green]
  ["^" :yellow :green]
])

(def board-width 80)
(def board-height 40)

(defn get-tile-values [width height]
  (map (fn [[x y val]]
         (int (Math/round (* 5 (/ (inc val) 2)))))
       (gen_noise/octave-perlin-over-ranges 6 0.25 (/ 1 8) width height)))

(def default-game-board (partition-all board-width (get-tile-values board-width board-height)))

(defn -main [& args]
  (let [scr (s/get-screen)]
    (s/in-screen scr
      (ui/render scr default-game-board default-tileset)
      (s/get-key-blocking scr))))
