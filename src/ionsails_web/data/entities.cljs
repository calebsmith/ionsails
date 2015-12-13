(ns ionsails-web.data.entities
  (:require [brute.entity :as ent]
            [ionsails-web.data.components :as c]))

(defn create-area
  [sys name keywords x y z]
  (let [area (ent/create-entity)
        sys (-> sys
                (ent/add-entity area)
                (ent/add-component area (c/->Coor x y z))
                (ent/add-component area (c/->CoorBag #{}))
                (ent/add-component area (c/->Keywords keywords))
                (ent/add-component area (c/->Ident name)))]
    [sys area]))

(defn create-location
  [sys name desc x y z]
  (let [loc (ent/create-entity)
        sys (-> sys (ent/add-entity loc)
                (ent/add-component loc (c/->Coor x y z))
                (ent/add-component loc (c/->Description desc))
                (ent/add-component loc (c/->CoorContainer nil))
                (ent/add-component loc (c/->CoorRefMap {}))
                (ent/add-component loc (c/->ItemBag #{}))
                (ent/add-component loc (c/->Ident name)))]
    [sys loc]))

(defn create-player
  [sys name desc coor]
  (let [player (ent/create-entity)
        sys (-> sys (ent/add-entity player)
                (ent/add-component player (c/->Control true))
                (ent/add-component player (c/->CoorRef coor))
                (ent/add-component player (c/->ItemBag #{}))
                (ent/add-component player (c/->Description desc))
                (ent/add-component player (c/->Ident name)))]
    [sys player]))

(defn create-item
  [sys name keywords desc]
  (let [item (ent/create-entity)
        sys (-> sys (ent/add-entity item)
                (ent/add-component item (c/->ItemContainer nil))
                (ent/add-component item (c/->Description desc))
                (ent/add-component item (c/->Keywords keywords))
                (ent/add-component item (c/->Ident name)))]
    [sys item]))

(defn- add-coor-to-container
  [bag coor]
  (c/->CoorBag (conj (:items bag) coor)))

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

(defn change-loc
  [sys ent coor]
  (ent/update-component sys ent c/CoorRef
                        (fn [coor-ref coor]
                          (c/->CoorRef coor)) coor))

(defn add-link-in-coor
  [sys coor-a coor-b kw]
  (ent/update-component sys coor-a c/CoorRefMap add-coor-to-ref-map kw coor-b))

(defn- add-item-to-ent
  [bag item]
  (c/->ItemBag (conj (:items bag) item)))

(defn- add-container-to-item
  [item bag]
  (c/->ItemContainer bag))

(defn- remove-item-from-ent
  [bag item]
  (c/->ItemBag (disj (:items bag) item)))

(defn place-item
  [sys item place]
  (-> sys
      (ent/update-component place c/ItemBag add-item-to-ent item)
      (ent/update-component item c/ItemContainer add-container-to-item place)))

(defn move-item
  [sys src target item]
  (-> sys
      (ent/update-component src c/ItemBag remove-item-from-ent item)
      (ent/update-component target c/ItemBag add-item-to-ent item)
      (ent/update-component item c/ItemContainer add-container-to-item target)))
