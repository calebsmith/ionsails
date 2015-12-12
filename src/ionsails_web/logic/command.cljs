(ns ionsails-web.logic.command
  (:require [clojure.string :as s]
            [amalloy.ring-buffer :as buff]
            [brute.entity :as ent]
            [ionsails-web.data.components :as c]
            [ionsails-web.data.entities :as e]
            [ionsails-web.event :as event :refer-macros [deflistener]]))

(declare commands)

(defn handle-nop
  [world c]
  (event/send :console {:category :echo :text (str c " is not a valid command")}))

(defn handle-help
  [world c]
  (let [help-text-preface ["Type commands in the input box below."
                           "Possible commands are:"]
        formatted-commands (for [c commands] (str "    " c))
        help-text (concat help-text-preface formatted-commands)
        formatted-help-text (for [v help-text] {:category :echo :text v})]
    (event/send :console {:multi formatted-help-text})))

(defn handle-clear
  [world c]
  (swap! world assoc-in [:ui :console.messages] (buff/ring-buffer 250)))

(defn handle-look
  [world c]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (first (filter #(= (type %) c/CoorRef) (ent/get-all-components-on-entity sys player))))
        loc-components (ent/get-all-components-on-entity sys loc)
        room-name (:name (first (filter #(= (type %) c/Ident) loc-components)))
        room-desc (:description (first (filter #(= (type %) c/Description) loc-components)))
        exit-items (:items (first (filter #(= (type %) c/CoorRefMap) loc-components)))
        exit-descs (vec (for [[k v] exit-items]
                          (let [loc-comp (ent/get-all-components-on-entity sys v)
                                exit-room-name (:name (first (filter #(= (type %) c/Ident) loc-comp)))]
                            {:category :exit
                             :text (str (name k) " - " exit-room-name)})))]
    (event/send :console {:multi (concat [{:category :echo :text "You are in:"}
                                          {:category :title :text room-name}
                                          {:category :info :text room-desc}
                                          {:category :exit :text "Exits:"}]
                                         exit-descs)})))

(defn handle-move
  [world target]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (first (filter #(= (type %) c/CoorRef) (ent/get-all-components-on-entity sys player))))
        loc-components (ent/get-all-components-on-entity sys loc)
        exit-items (:items (first (filter #(= (type %) c/CoorRefMap) loc-components)))
        target-loc (target exit-items)]
    (if target-loc
      (do
        (swap! world assoc :system
               (e/change-loc sys player target-loc))
        (handle-look world ""))
      (event/send :console {:category :echo :text "No exit in that direction"}))))

(def handle-left #(handle-move %1 :left))
(def handle-right #(handle-move %1 :right))
(def handle-down #(handle-move %1 :down))
(def handle-up #(handle-move %1 :up))
(def handle-forward #(handle-move %1 :forward))
(def handle-backward #(handle-move %1 :backward))

(defn handle-initial
  [world c]
  (handle-look world c)
  (event/send :console {:category :echo :text "Type \"help\" in the input box below for help"}))

(def command-lookup
  {"look" handle-look
   "left" handle-left
   "right" handle-right
   "up" handle-up
   "down" handle-down
   "forward" handle-forward
   "backward" handle-backward
   "clear" handle-clear
   "help" handle-help})

(def commands (keys command-lookup))

(deflistener handle-command :command
  [world {:keys [command]}]
  (let [command (s/lower-case command)
        handler (get command-lookup command handle-nop)]
    (handler world command)
    (when (not= handler handle-nop)
      (swap! world update-in [:ui :command.history] conj command))))
