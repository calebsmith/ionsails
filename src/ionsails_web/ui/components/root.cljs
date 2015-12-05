(ns ionsails-web.ui.components.root
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]))


(q/defcomponent RootComponent
  "The root of the application UI"
  [world]
  (d/div {}
         (d/h2 {} "Hi there")))
