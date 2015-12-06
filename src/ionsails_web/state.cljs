(ns ionsails-web.state
  (:require [amalloy.ring-buffer :as buff]))

(defn initial-state
  []
  {:console/messages-main (buff/ring-buffer 150)})
