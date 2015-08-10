(ns ionsails.core
  (:gen-class))

(require '[lanterna.screen :as s])


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

(defn tile-formatter [tile-val]
    [(first tile-val) {:fg (nth tile-val 1) :bg (nth tile-val 2)}])

(defn row->put-cmds
    "Given a board, tileset, and a row number, return the list of arguments
    to put-string in order to display that row"
    [game-board tileset row-index]
    (map-indexed (fn [index value]
                     (concat (list index row-index)
                             (tile-formatter (get tileset value))))
                 (nth game-board row-index)))

(defn game-board->cmds [game-board]
    (mapcat (fn [row-index]
                (row->put-cmds game-board default-tileset row-index))
            (range (count game-board))))

(defn game-board-put [commands command-func]
    (dorun (map (fn [cmd]
                    (apply command-func cmd))
                commands)))


(def scr (s/get-screen))

(defn render [game-board]
    (s/clear scr)
    (game-board-put (game-board->cmds game-board) (partial s/put-string scr))
    (s/move-cursor scr 0 0)
    (s/redraw scr))

(defn -main [& args]
  (s/in-screen scr
    (render default-game-board)
    (s/get-key-blocking scr)))
