(ns ionsails-web.core
  (:require [ionsails-web.state :as state]
            [ionsails-web.event :as event :refer-macros [deflistener]]
            [ionsails-web.render :as render]))

(enable-console-print!)

(defonce world
  (atom (state/initial-state)))

(defn ^:export main
  "Application entry point"
  []
  (swap! world assoc :started true)
  (event/initialize world)
  (render/render world (.getElementById js/document "app")))

;; Initial call to main
(when-not (get @world :started) (main))

;; Handle reload events
(defn on-js-reload []
  (event/reset-listeners world))
