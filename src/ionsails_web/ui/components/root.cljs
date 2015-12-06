(ns ionsails-web.ui.components.root
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [ionsails-web.ui.components.graph :as graph]))


(q/defcomponent RootComponent
  "The root of the application UI"
  [world]
  (graph/Graph world))
