(ns ionsails-web.ui.components.command
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [quiescent.dom.uncontrolled :as du]
            [ionsails-web.event :as event :refer-macros [deflistener]]))


(defn handle-enter
  [completions elm inp-elm evt val]
  (set! (.-value inp-elm) ""))

(defn handle-tab
  [completions elm inp-elm evt val]
  (.preventDefault evt)) 

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
  (swap! ionsails-web.core/world assoc :command-completions ["look"])


  )
