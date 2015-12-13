(ns ionsails-web.data.core
  (:require [brute.entity :as ent]
            [ionsails-web.data.entities :as e]
            [ionsails-web.data.components :as c]))

;; N.B. Not much to do with a system just yet, just build a bare one here with some hard-coded data

(defn build-initial-system
  []
  (let [sys (ent/create-system)
        [sys area] (e/create-area sys "main" 0 1 2)
        [sys loc1] (e/create-location sys "A spaceport dive bar" "" 1 2 1)
        [sys loc2] (e/create-location sys "A spaceport garage" "A vast steel room, evidently for docking and repairing ships." 1 4 1)
        [sys item] (e/create-item sys "wrench" "A monkey wrench")
        [sys player] (e/create-player sys "Caleb" "space pirate" loc1)
        sys (-> sys
                (e/put-coor-in-bag loc1 area)
                (e/put-coor-in-bag loc2 area)
                (e/place-item item loc2)
                (e/add-link-in-coor loc1 loc2 :left)
                (e/add-link-in-coor loc2 loc1 :right))]
    [sys player]))

(comment
  ;;troubleshooting

  (def sys-player (build-initial-system))

  (def sys (first sys-player))

  (def player (second sys-player))

  (def loc1 (:id (ent/get-component sys player c/CoorRef)))

  (def area (:id (ent/get-component sys loc1 c/CoorContainer)))

  loc1

  (def loc2
    (second (:items  (ent/get-component sys area c/CoorBag))))

  )
