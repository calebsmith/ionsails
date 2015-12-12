(ns ionsails-web.ui.components.command
  (:require [ionsails-web.event :as event :refer-macros [deflistener]]
            [ionsails-web.util.trie :as trie]
            [ionsails-web.logic.command :as cmd]
            [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [quiescent.dom.uncontrolled :as du]))

(defn handle-enter
  [world elm inp-elm evt val]
  (swap! world assoc-in [:ui :command.history-line] -1)
  (when (not= val "")
    (event/send :console {:category :echo :text (str "=> " val)})
    (event/send :command {:command val})
    (set! (.-value inp-elm) "")))

(defn handle-tab
  [world elm inp-elm evt val]
  (do 
    (.preventDefault evt)
    (when-let [hints (trie/lookup (-> @world :ui :command.completions) val)]
      (when (= (count hints) 1)
        (set! (.-value inp-elm) (first hints))))))

(defn handle-up
  [world elm inp-elm evt val]
  (let [history (vec (reverse (-> @world :ui :command.history)))
        history-line (inc (get-in @world [:ui :command.history-line] -1))
        hist-value (get history history-line)]
    (when (and (< history-line (count history)) hist-value)
      (swap! world assoc-in [:ui :command.history-line] history-line)
      (set! (.-value inp-elm) hist-value))))

(defn handle-down
  [world elm inp-elm evt val]
  (let [history (vec (reverse (-> @world :ui :command.history)))
        history-line (dec (get-in @world [:ui :command.history-line] 0))
        hist-value (get history history-line "")]
    (when (>= history-line -1)
      (swap! world assoc-in [:ui :command.history-line] history-line)
      (set! (.-value inp-elm) hist-value))))

(def handler-lookup
  {9 handle-tab
   13 handle-enter
   38 handle-up
   40 handle-down})

(defn key-down-listener
  [world div-elm inp-elm evt]
  (let [keycode (.-keyCode evt)
        key-handler (get handler-lookup keycode)]
    (when key-handler
      (key-handler world div-elm inp-elm evt (.-value (.-target evt))))))

(defn event-grab
  [world elm-id-num]
  (let [div-elm-id (str "command-line-" elm-id-num)
        inp-elm-id (str "command-line-input-" elm-id-num)
        div-elm (.getElementById js/document div-elm-id)
        inp-elm (.getElementById js/document inp-elm-id)]
    (.addEventListener div-elm "keydown" #(key-down-listener world div-elm inp-elm %) false)))

(q/defcomponent CommandLine
  :on-mount (fn [_ _ world elm-id-num]
              (event-grab world elm-id-num))
  [_ world elm-id-num]
  (d/div {:id (str "command-line-" elm-id-num) :className "command-line"}
         (du/input {:id (str "command-line-input-" elm-id-num) :autoFocus true})))
