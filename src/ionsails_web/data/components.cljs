(ns ionsails-web.data.components)

(defrecord Coor [x y z])
(defrecord CoorBag [items])
(defrecord Ident [name kw])
(defrecord Description [description])
(defrecord CoorContainer [id])
(defrecord CoorRef [id])
(defrecord Control [bool])
