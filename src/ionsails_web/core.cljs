(ns ionsails-web.core
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]))

(enable-console-print!)

(defonce world (atom {}))

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

(defn ^:export main
  "Application entry point"
  []
  (let [dom-root (.getElementById js/document "app")]
    (swap! world :started true)
    (render world dom-root)))

;; Initial call to main
(when-not (get @world :started) (main))

;; Handle reload events
(defn on-js-reload [])
