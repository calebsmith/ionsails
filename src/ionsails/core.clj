(ns ionsails.core)

(require '[lanterna.screen :as s])

(def scr (s/get-screen))

(def start-screen (partial s/start scr))
(def stop-screen (partial s/stop scr))
(def redraw-screen (partial s/redraw scr))
(def put-screen (partial s/put-string scr))

(def default-tileset {
  :0 [ " " :black :black]
  :1 [ " " :black :blue]
  :2 [ "." :yellow :green]
  :3 [ "." :black :green]
  :4 [ "#" :yellow :green]
  :5 [ "@" :yellow :green]
  :6 [ "^" :yellow :green]
})

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

(defn tileset-val-formatter [tileset-val]
  (list (first tileset-val) {:fg (nth tileset-val 1) :bg (nth tileset-val 2)}))

(defn row->put-cmds [game-board tileset row-index]
  (map-indexed (fn [index value]
                 (concat (list index row-index)
                         (tileset-val-formatter (get tileset (keyword (str value))))))
                 (nth game-board row-index)))

(defn game-board->cmds [game-board]
  (map (fn [row-index]
         (row->put-cmds game-board default-tileset row-index))
       (range (count game-board))))

(defn game-board-put [cmd-2d]
  (doall (map (fn [cmd-lst]
         (dorun (map (fn [cmd]
                (apply put-screen cmd))
         cmd-lst)))
       cmd-2d)))


(defn render [game-board]
  (s/clear scr)
  (game-board-put (game-board->cmds game-board))
  (s/move-cursor scr 0 0)
  (redraw-screen)
  )



; testing
(start-screen)

(render default-game-board)

;(s/get-key-blocking scr)
;(stop-screen)
