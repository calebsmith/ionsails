(ns ionsails-web.ui.components.command
  (:require [ionsails-web.event :as event :refer-macros [deflistener]]
            [ionsails-web.util.trie :as trie]
            [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [quiescent.dom.uncontrolled :as du]))

(defn handle-enter
  [completions elm inp-elm evt val]
  (event/send :ionsails-web.logic.command/command-enter {:text val})
  (set! (.-value inp-elm) ""))

(defn handle-tab
  [completions elm inp-elm evt val]
  (.preventDefault evt)
  (when-let [hint (first (trie/lookup completions val))]
    (set! (.-value inp-elm) hint)))

(def handler-lookup
  {9 handle-tab
   13 handle-enter})

(defn key-down-listener
  [completions div-elm inp-elm evt]
  (let [keycode (.-keyCode evt)
        key-handler (get handler-lookup keycode)]
    (when key-handler
      (key-handler completions div-elm inp-elm evt (.-value (.-target evt))))))

(defn event-grab
  [completions elm-id-num]
  (let [div-elm-id (str "command-line-" elm-id-num)
        inp-elm-id (str "command-line-input-" elm-id-num)
        div-elm (.getElementById js/document div-elm-id)
        inp-elm (.getElementById js/document inp-elm-id)]
    (.addEventListener div-elm "keydown" #(key-down-listener completions div-elm inp-elm %) false)))

(q/defcomponent CommandLine
  :on-mount (fn [_ completions elm-id-num]
              (event-grab completions elm-id-num))
  [completions elm-id-num]
  (d/div {:id (str "command-line-" elm-id-num) :className "command-line"}
         (du/input {:id (str "command-line-input-" elm-id-num)})))


(comment

  ;; Should be put in state where initial data is set
  (swap! ionsails-web.core/world assoc :command-completions (trie/build-trie ["look" "go" "where" "when"]))

  )
