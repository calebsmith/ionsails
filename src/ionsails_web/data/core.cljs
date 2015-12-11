(ns ionsails-web.data.core
  (:require [brute.entity :as ent]
            [ionsails-web.data.entities :as e]
            [ionsails-web.data.components :as c]))

;; N.B. Not much to do with a system just yet, just build a bare one here with some hard-coded data

(defn build-initial-system
  []
  (let [sys (-> (ent/create-system)
                (e/create-area "main" 0 1 2)
                (e/create-location "bar" "A dive bar" 1 2 1)
                (e/create-location "garage" "A spaceport garage" 1 4 1))
        loc (first (ent/get-all-entities-with-component sys c/CoorContainer))
        loc2 (second (ent/get-all-entities-with-component sys c/CoorContainer))
        area (first (ent/get-all-entities-with-component sys c/CoorBag))
        sys (e/put-coor-in-bag sys loc area)
        sys (e/add-link-in-coor sys loc loc2 :left)
        sys (e/add-link-in-coor sys loc2 loc :right)
        sys-player (e/create-player sys "Caleb" "space pirate" loc)]
    sys-player))

(defn get-player-id
  [sys]
  (first (ent/get-all-entities-with-component sys c/Control)))

(comment
  ;;troubleshooting

  (def sys (build-initial-system))

  (def loc  (first (ent/get-all-entities-with-component sys c/CoorContainer)))
  (def area  (first (ent/get-all-entities-with-component sys c/CoorBag)))
  (def player  (first (ent/get-all-entities-with-component sys c/Control)))

  )
