(ns ionsails-web.render
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [ionsails-web.ui.components.root :as root]))

(def root-components
  {:root root/RootComponent})

(defn render
  "Initiate rendering of the application"
  [world dom-root comp-kw]
  (do
    (q/render ((comp-kw root-components) @world world) dom-root)
    (.requestAnimationFrame js/window #(render world dom-root comp-kw))))
