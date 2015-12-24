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
  [sys entities kw-query kw-query-index]
  (let [ent-triples (map (fn [e] [e
                                  (set (:keywords (ent/get-component sys e c/Keywords)))
                                  (:name (ent/get-component sys e c/Ident))])
                         entities)
        ent-triples-candidates (filter (fn [[id kws name]]
                                         (contains? kws kw-query))
                                       ent-triples)
        ent-triples-candidates (vec (sort-by #(nth % 2) ent-triples-candidates))
        kw-query-index (if (< kw-query-index (count ent-triples-candidates))
                         (if (< kw-query-index 0) 0 (dec kw-query-index))
                         (dec (count ent-triples-candidates)))
        [item-id item-kw item-name] (get ent-triples-candidates kw-query-index)]
    item-id))

(defn ->int
  [n]
  (js/parseInt n))

(defn int?
  [n]
  (-> n ->int js/isNaN not))

(defn parse-q-kw-container
  [c-args]
  (match [(mapv int? c-args) c-args]
         [[false] [kw]] [1 kw 1 nil]
         [[true false] [q kw]] [(->int q) kw 1 nil]
         [[false true]  [kw kw-index]] [1 kw (->int kw-index) nil]
         [[true false true] [q kw kw-index]] [(->int q) kw (->int kw-index) nil]
         [[false _ & _] [kw (:or "in" "from") & container]] [1 kw 1 container]
         [[true false _ & _] [q kw (:or "in" "from") & container]] [(->int q) kw 1 container]
         [[false true _ & _]  [kw kw-index (:or "in" "from") & container]] [1 kw (->int kw-index) container]
         [[true false true _ & _] [q kw kw-index (:or "in" "from") & container]] [(->int q) kw (->int kw-index) container]
         :else [0 "" 1 nil]))

(defn parse-kw-container
  [c-args]
  (match [(mapv int? c-args) c-args]
         [[false] [kw]] [1 kw 1 nil]
         [[false true]  [kw kw-index]] [1 kw (->int kw-index) nil]
         [[false _ & _] [kw (:or "in" "from") & container]] [1 kw 1 container]
         [[false true _ & _]  [kw kw-index (:or "in" "from") & container]] [1 kw (->int kw-index) container]
         :else [0 "" 1 nil]))

(defn parse-kw-or-container
  [c-args]
  (match [(mapv int? c-args) c-args]
         [[false] [kw]] [1 kw 1 nil]
         [[false true]  [kw kw-index]] [1 kw (->int kw-index) nil]
         [[_ & _] [(:or "in" "from") & container]] [1 "" 1 container]
         :else [0 "" 1 nil]))

(defn find-keywords-in
  [sys candidates [q kw kw-index]]
  (if (= kw "all")
    candidates
    (remove nil?
            (loop [candidates candidates
                   result-ids []
                   i 0]
              (if (< i q)
                (let [res-id (match-keyword sys candidates kw kw-index)]
                  (recur
                   (remove #(= % res-id) candidates)
                   (conj result-ids res-id)
                   (inc i)))
                result-ids)))))

(defn find-best-keywords-in
  [sys candidates query]
  (first (find-keywords-in sys candidates query)))

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

(defn handle-look-object
  [world sys items parsed-args]
  (let [item-ent (find-best-keywords-in sys items parsed-args)
        item-desc (:description (ent/get-component sys item-ent c/Description))
        item-text (if item-desc
                    (str "You look closely at " (s/lower-case item-desc) "... Nothing interesting beyond the surface")
                    "You see nothing like that here.")]
    (event/send :console {:category :item :text item-text})))

(defn handle-look-in
  [world sys items container-args]
  (let [parsed-container-args (parse-kw-or-container container-args)
        container-ent (find-best-keywords-in sys items parsed-container-args)
        container-desc (:description (ent/get-component sys container-ent c/Description))
        items (:items (ent/get-component sys container-ent c/ItemBag))
        item-descs (map (fn [item-ent]
                          (:description (ent/get-component sys item-ent c/Description))) items)]
    (if (empty? item-descs)
      (cond
        (nil? container-ent) (event/send :console {:category :info :text "You don't have a container like that"})
        (nil? items) (event/send :console {:category :info :text (str container-desc " can not hold anything.")})
        :else (event/send :console {:category :info :text (str container-desc " is empty.")}))
      (event/send :console {:multi (concat [{:category :info :text (str container-desc " holds: ")}]
                                           (mapv (fn [v] {:category :item :text v}) item-descs))}))))

(defn handle-look-w-args
  [world c-args]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (ent/get-component sys player c/CoorRef))
        loc-items (:items (ent/get-component sys loc c/ItemBag))
        inv-items (:items (ent/get-component sys player c/ItemBag))
        all-items (concat loc-items inv-items)
        parsed (parse-kw-or-container c-args)
        container (last parsed)]
    (if container
      (handle-look-in world sys all-items container)
      (handle-look-object world sys all-items (butlast parsed)))))

(defn handle-look-room
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

(defn handle-look-direction
  [world c-args]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (ent/get-component sys player c/CoorRef))
        direction (first c-args)
        exit-items (:items (ent/get-component sys loc c/CoorRefMap))
        exit-v (get exit-items (keyword direction))
        exit-room-name (:name (ent/get-component sys exit-v c/Ident))]
    (if exit-room-name
      (event/send :console {:category :exit :text (str "To the " direction " is " (s/lower-case exit-room-name))})
      (event/send :console {:category :info :text "There is no exit in that direction."}))))

(defn handle-look
  [world c]
  (let [c-args (vec (rest (s/split c " ")))]
    (if (= (count c-args) 0)
      (handle-look-room world c)
      (if (contains? #{"left" "right" "up" "down" "forward" "backward"} (first c-args))
        (handle-look-direction world c-args)
        (handle-look-w-args world c-args)))))

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
        parsed-args (parse-q-kw-container (vec (rest (s/split c " "))))
        container (last parsed-args)
        container-ent (when container
                        (find-best-keywords-in sys
                                               (:items (ent/get-component sys player c/ItemBag))
                                               (parse-kw-container container)))
        source-items (if container-ent
                       (:items (ent/get-component sys container-ent c/ItemBag))
                       loc-items)
        source (if container-ent container-ent loc)
        item-ids (find-keywords-in sys source-items (butlast parsed-args))
        message (if (= 1 (count item-ids)) "You pick it up" "You pick them up")]
    (if (seq item-ids)
      (do
        (swap! world assoc :system (e/move-items sys source player item-ids))
        (event/send :console {:category :echo :text message}))
      (event/send :console {:category :echo :text "That item isn't here"}))))

(defn handle-put
  [world c]
  (let [sys (:system @world)
        player (:player-id @world)
        player-items (:items (ent/get-component sys player c/ItemBag))
        parsed-args (parse-q-kw-container (vec (rest (s/split c " "))))
        container (last parsed-args)]
    (if (not container)
      (event/send :console {:category :echo :text "Must specify the item and what you are putting it in"})
      (let [container-ent (find-best-keywords-in sys player-items (parse-kw-container container))
            unsafe-item-ids (find-keywords-in sys player-items (butlast parsed-args))
            item-ids (remove #{container-ent} unsafe-item-ids)
            message (if (= 1 (count item-ids)) "You put it in there" "You put them in there")]
        (if (seq item-ids)
          (do
            (swap! world assoc :system (e/move-items sys player container-ent item-ids))
            (event/send :console {:category :echo :text message}))
          (if (not= item-ids unsafe-item-ids)
            (event/send :console {:category :echo :text "You can't put something inside of itself"})
            (event/send :console {:category :echo :text "You don't have anything like that"})))))))

(defn handle-drop
  [world c]
  (let [sys (:system @world)
        player (:player-id @world)
        loc (:id (ent/get-component sys player c/CoorRef))
        target-items (:items (ent/get-component sys player c/ItemBag))
        [q kw kw-index container] (parse-q-kw-container (vec (rest (s/split c " "))))
        item-ids (find-keywords-in sys target-items [q kw kw-index])
        message (if (= 1 (count item-ids)) "You drop it" "You drop them")]
    (if (seq item-ids)
      (do
        (swap! world assoc :system (e/move-items sys player loc item-ids))
        (event/send :console {:category :echo :text message}))
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
   "put" handle-put
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
