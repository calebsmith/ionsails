(ns ionsails.ui
  (:require [lanterna.screen :as s])
  (:gen-class))


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

(defn game-board->cmds [game-board tileset]
    (mapcat (fn [row-index]
                (row->put-cmds game-board tileset row-index))
            (range (count game-board))))

(defn game-board-put [commands command-func]
    (dorun (map (fn [cmd]
                    (apply command-func cmd))
                commands)))


(defn render [scr game-board tileset]
    (s/clear scr)
    (game-board-put (game-board->cmds game-board tileset) (partial s/put-string scr))
    (s/move-cursor scr 0 0)
    (s/redraw scr))
