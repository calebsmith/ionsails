(ns ionsails-web.logic.command
  (:require [clojure.string :as s]
            [cljs.core.match :refer-macros [match]]
            [amalloy.ring-buffer :as buff]
            [brute.entity :as ent]
            [ionsails-web.data.components :as c]
            [ionsails-web.data.entities :as e]
            [ionsails-web.event :as event :refer-macros [deflistener]]))

(declare commands)

(defn match-keyword
  [sys entities c]
  (let [args (rest (s/split c " "))
        query-item-name (first args)
        query-item-number (or (int (second args)) 1)
        ent-pairs (map (fn [e] [e (set (:keywords (ent/get-component sys e c/Keywords)))])
                       entities)
        ent-pair-candidates (vec (filter (fn [[id kws]]
                                           (contains? kws query-item-name))
                                         ent-pairs))
        query-item-number (if (< query-item-number (count ent-pair-candidates))
                            (if (< query-item-number 0) 0 query-item-number)
                            (dec (count ent-pair-candidates)))
        [item-id item-kw] (get ent-pair-candidates query-item-number)]
    item-id))

(defn parse-get-args
  "N.B. Not incorporated yet"
  [command]
  (let [int? #(not (js/isNaN (js/parseInt %)))
        nint? #(js/isNaN (js/parseInt %))]
    (let [args (vec (rest (s/split command " ")))]
      (match [(mapv int? args) args]
             [[false] [kw]] [1 kw 1 nil]
             [[true false] [q kw]] [q kw  1 nil]
             [[false true]  [kw kw-index]] [1 kw kw-index nil]
             [[true false true] [q kw kw-index]] [q kw kw-index nil]
             [[false _ _] [kw "from" container]] [1 kw 1 container]
             [[true false _ _] [q kw "from" container]] [q kw  1 container]
             [[false true _ _]  [kw kw-index "from" container]] [1 kw kw-index container]
             [[true false true _ _] [q kw kw-index "from" container]] [q kw kw-index container]
             :else :no-match
             ))))

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
        room-name (:name (ent/get-component sys loc c/Ident))
        room-desc (:description (ent/get-component sys loc c/Description))
        room-inventory (:items (ent/get-component sys loc c/ItemBag))
        exit-items (:items (ent/get-component sys loc c/CoorRefMap))
        item-descs (map #(let [item-desc (:description (ent/get-component sys % c/Description))]
                           {:category :item :text item-desc}) room-inventory)
        exit-descs (for [[k v] exit-items]
                     (let [exit-room-name (:name (ent/get-component sys v c/Ident))
                           formatted-name (str (name k) " - " exit-room-name)]
                       {:category :exit :text formatted-name}))
        msg-body (concat [{:category :title :text room-name}
                          {:category :info :text room-desc}]
                         (when ((complement empty?) item-descs)
                           [{:category :item :text "Items:"}])
                         item-descs
                         (when ((complement empty?) exit-descs)
                           [{:category :exit :text "Exits:"}]
                           exit-descs))]
    (event/send :console {:multi msg-body})))

(defn handle-inventory
  [world c]
  (let [sys (:system @world)
        player (:player-id @world)
        items (:items (ent/get-component sys player c/ItemBag))
        item-names (map #(let [item-name (:name (ent/get-component sys % c/Ident))]
                           {:category :item :text item-name}) items)
        msg-body (if (empty? item-names)
                   [{:category :echo :text "Your inventory is empty"}]
                   (concat [{:category :echo :text "You are holding: "}] item-names))]
    (event/send :console {:multi msg-body})))

(defn handle-move
  [world target]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (ent/get-component sys player c/CoorRef))
        loc-components (ent/get-all-components-on-entity sys loc)
        exit-items (:items (ent/get-component sys loc c/CoorRefMap))
        target-loc (target exit-items)]
    (if target-loc
      (do
        (swap! world assoc :system
               (e/change-loc sys player target-loc))
        (handle-look world ""))
      (event/send :console {:category :echo :text "No exit in that direction"}))))

(defn handle-get
  [world c]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (ent/get-component sys player c/CoorRef))
        loc-items (:items (ent/get-component sys loc c/ItemBag))
        item-id (match-keyword sys loc-items c)]
    (if item-id
      (do
        (event/send :console {:category :echo :text "You pick it up"})
        (swap! world assoc :system (e/move-item sys loc player item-id)))
      (event/send :console {:category :echo :text "That item isn't here"}))))

(defn handle-drop
  [world c]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (ent/get-component sys player c/CoorRef))
        player-items (:items (ent/get-component sys player c/ItemBag))
        item-id (match-keyword sys player-items c)]
    (if item-id
      (do
        (event/send :console {:category :echo :text "You drop it"})
        (swap! world assoc :system (e/move-item sys player loc item-id)))
      (event/send :console {:category :echo :text "You aren't holding that item"}))))

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
   "inventory" handle-inventory
   "get" handle-get
   "drop" handle-drop
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
  (let [command (-> command s/trim s/lower-case)
        command-top (first (s/split command " "))
        handler (get command-lookup command-top handle-nop)]
    (handler world command)
    (when (not= handler handle-nop)
      (swap! world update-in [:ui :command.history] conj command))))
