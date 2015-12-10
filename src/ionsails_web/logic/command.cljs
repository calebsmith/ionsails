(ns ionsails-web.logic.command
  (:require [brute.entity :as ent]
            [ionsails-web.data.components :as c]
            [ionsails-web.event :as event :refer-macros [deflistener]]))

(defn handle-nop
  [world c]
  (event/send :console {:category :echo :text (str c " is not a valid command")}))

(defn handle-look
  [world c]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (first (filter #(= (type %) c/CoorRef) (ent/get-all-components-on-entity sys player))))
        desc (:description (first (filter #(= (type %) c/Description) (ent/get-all-components-on-entity sys loc))))]
    (event/send :console {:category :info :text desc})))

(deflistener handle-command :command
  [world {:keys [command]}]
  (let [handler (condp = command
                  "look" handle-look
                  handle-nop)]
    (handler world command)))
