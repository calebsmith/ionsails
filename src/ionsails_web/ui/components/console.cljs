(ns ionsails-web.ui.components.console
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [ionsails-web.event :as event :refer-macros [deflistener]]))

(deflistener console-messages-main :console
  [world data]
  (let [multi (:multi data)
        send-data (if multi multi [data])]
    (swap! world update :console/messages-main concat send-data)))

(defn scroll-bottom
  "When console is not in focus, force scroll to the bottom"
  [elm-id]
  (let [elm (.getElementById js/document elm-id)]
    (when-not (.matches elm ":focus")
      (set! (.-scrollTop elm) (.-scrollHeight elm)))))

(q/defcomponent Console
  :on-render (fn [_ state _ elm-id-num]
               (scroll-bottom (str "console-" elm-id-num)))
  [state elm-id-num]
  (let [messages (:console/messages-main state)
        elm-id (str "console-" elm-id-num)]
    (d/div {:id elm-id :className "console"}
           (apply d/ul {}
                  (map #(d/li {:className (:category %)} (:text  %)) messages)))))

(comment

  (event/send :console {:text "Before you is a mailbox" :category "info"})

  (event/send :console {:text "DEATH IS IMMINENT" :category :warning})

  (event/send :console {:multi [
                                {:text "DEATH IS IMMINENT" :category :warning}
                                {:text "All your base" :category :warning}
                                ]})

  )
