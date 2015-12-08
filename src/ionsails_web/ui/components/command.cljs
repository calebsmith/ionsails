(ns ionsails-web.ui.components.command
  (:require [ionsails-web.event :as event :refer-macros [deflistener]]
            [ionsails-web.util.trie :as trie]
            [ionsails-web.logic.command :as cmd]
            [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [quiescent.dom.uncontrolled :as du]))

(defn handle-enter
  [completions elm inp-elm evt val]
  (when (not= val "")
    (cmd/handle val)
    (event/send :console {:category :echo :text (str "=> " val)})
    (set! (.-value inp-elm) "")))

(defn handle-tab
  [completions elm inp-elm evt val]
  (do 
    (.preventDefault evt)
    (when-let [hints (trie/lookup completions val)]
      (when (= (count hints) 1)
        (set! (.-value inp-elm) (first hints))))))

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
  :on-render (fn [_ completions _ elm-id-num]
               (event-grab completions elm-id-num))
  [completions elm-id-num]
  (d/div {:id (str "command-line-" elm-id-num) :className "command-line"}
         (du/input {:id (str "command-line-input-" elm-id-num)})))
