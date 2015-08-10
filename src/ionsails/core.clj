(ns ionsails.core
  (:require [lanterna.screen :as s]
            [ionsails.ui :as ui])
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

(def default-game-board [
 [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
 [1 1 1 1 1 1 1 1 1 2 2 1 1 2 1 1 1 1 1 1]
 [1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 2 1 1 1]
 [1 1 1 1 2 2 2 2 2 2 4 2 2 2 2 2 2 2 1 1]
 [1 1 1 2 2 2 2 2 2 2 4 4 2 2 2 2 2 2 1 1]
 [1 1 2 2 2 2 2 2 2 2 4 2 2 6 2 2 2 1 1 1]
 [1 1 1 1 2 2 2 2 4 4 2 4 6 6 2 2 2 1 1 1]
 [1 1 2 2 2 2 2 2 1 4 4 4 6 2 2 2 1 1 1 1]
 [1 1 1 1 2 2 2 2 2 4 2 2 2 2 2 2 2 2 1 1]
 [1 1 1 2 2 2 2 2 2 2 2 2 2 2 2 2 2 1 1 1]
 [1 1 1 1 1 2 2 2 3 3 3 2 2 2 2 1 1 1 1 1]
 [1 1 1 1 1 1 1 2 2 3 3 2 2 1 1 1 1 1 1 1]
 [1 1 1 1 1 1 1 1 2 1 2 2 1 1 1 1 1 1 1 1]
])

(defn -main [& args]
  (let [scr (s/get-screen)]
    (s/in-screen scr
      (ui/render scr default-game-board default-tileset)
      (s/get-key-blocking scr))))
