(ns ionsails-web.ui.components.root
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [ionsails-web.ui.components.graph :as graph]
            [ionsails-web.ui.components.console :as console]))


(q/defcomponent RootComponent
  "The root of the application UI"
  [state world]
  (console/Console state 1))
