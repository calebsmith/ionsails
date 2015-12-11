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
        (ent/add-component loc (c/->CoorRefMap {}))
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

(defn- add-coor-to-container
  [bag coor]
  (c/->CoorBag (conj (:coors bag) coor)))

(defn- add-container-to-coor
  [coor bag]
  (c/->CoorContainer bag))

(defn put-coor-in-bag
  [sys coor bag]
  (-> sys
      (ent/update-component bag c/CoorBag add-coor-to-container coor)
      (ent/update-component coor c/CoorContainer add-container-to-coor bag)))

(defn- add-coor-to-ref-map
  [coor-map kw coor-b]
  (c/->CoorRefMap (merge (:items coor-map) {kw coor-b})))

(defn add-link-in-coor
  [sys coor-a coor-b kw]
  (ent/update-component sys coor-a c/CoorRefMap add-coor-to-ref-map kw coor-b))
