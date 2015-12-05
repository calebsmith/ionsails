(ns ionsails-web.render
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]))

(q/defcomponent Root
  "The root of the application"
  [world]
  (d/div {}
         (d/h2 {} "Hi")))

(defn render
  "Initiate rendering of the application"
  [world dom-root]
  (do
    (q/render (Root nil) dom-root)
    (.requestAnimationFrame js/window #(render world dom-root))))
