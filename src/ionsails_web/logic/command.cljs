(ns ionsails-web.logic.command
  (:require [ionsails-web.event :as event :refer-macros [deflistener]]))

(defn handle
  [val]
  (prn "Recieved command: " val))
