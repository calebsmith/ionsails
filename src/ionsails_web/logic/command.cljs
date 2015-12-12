(ns ionsails-web.logic.command
  (:require [clojure.string :as s]
            [brute.entity :as ent]
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
        loc-components (ent/get-all-components-on-entity sys loc)
        room-name (:name (first (filter #(= (type %) c/Ident) loc-components)))
        room-desc (:description (first (filter #(= (type %) c/Description) loc-components)))
        exit-items (:items (first (filter #(= (type %) c/CoorRefMap) loc-components)))
        exit-descs (for [[k v] exit-items] {:category :exit :text (str "An exit to the " (name k))})]
    (event/send :console {:multi (concat [{:category :echo :text "You are in:"}
                                          {:category :title :text room-name}
                                          {:category :info :text room-desc}
                                          {:category :exit :text "Exits:"}]
                                         exit-descs)})))

(deflistener handle-command :command
  [world {:keys [command]}]
  (let [command (s/lower-case command)
        handler (condp = command
                  "look" handle-look
                  handle-nop)]
    (handler world command)
    (when (not= handler handle-nop)
      (swap! world update :ui.command.history conj command))))
