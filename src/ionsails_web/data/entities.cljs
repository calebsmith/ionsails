(ns ionsails-web.data.entities
  (:require [brute.entity :as ent]
            [ionsails-web.data.components :as c]))

(defn create-area
  [sys name x y z]
  (let [area (ent/create-entity)]
    (-> sys
        (ent/add-entity area)
        (ent/add-component area (c/->Coor x y z))
        (ent/add-component area (c/->CoorBag #{}))
        (ent/add-component area (c/->Ident name name)))))

(defn create-location
  [sys name desc x y z]
  (let [loc (ent/create-entity)
        sys (ent/add-entity sys loc)]
    (-> sys
        (ent/add-component loc (c/->Coor x y z))
        (ent/add-component loc (c/->Description desc))
        (ent/add-component loc (c/->CoorContainer nil))
        (ent/add-component loc (c/->Ident name name)))))

(defn create-player
  [sys name desc coor]
  (let [player (ent/create-entity)
        sys (ent/add-entity sys player)]
    (-> sys
        (ent/add-component player (c/->Control true))
        (ent/add-component player (c/->CoorRef coor))
        (ent/add-component player (c/->Description desc))
        (ent/add-component player (c/->Ident name name)))))

(defn- add-item-to-container
  [bag item]
  (c/->CoorBag (conj (:items bag) item)))

(defn- add-container-to-item
  [item bag]
  (c/->CoorContainer bag))

(defn put-item-in
  [sys item bag]
  (-> sys
      (ent/update-component bag c/CoorBag add-item-to-container item)
      (ent/update-component item c/CoorContainer add-container-to-item bag)))
