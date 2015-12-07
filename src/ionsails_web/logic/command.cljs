(ns ionsails-web.logic.command
  (:require [ionsails-web.event :as event :refer-macros [deflistener]]))

(deflistener command-line-command ::command-enter
  [world {:keys [text]}]
  (prn "Recieved command: " text))
