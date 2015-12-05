(ns ionsails-web.event
  #?@(:cljs [(:require-macros [cljs.core.async.macros :refer [go go-loop]]
                              [ionsails-web.event])
             (:require [cljs.core.async :refer [chan pub sub >! <! put! unsub-all]])]
      :clj [(:require [cljs.core.async.macros :refer [go go-loop]])]))

#?(:cljs (def ^:private global-listeners (atom {})))

#?(:cljs (defonce ^:private event-chans (atom {})))

#?(:cljs
   (defn- add-global-listener
     "Add a global listener for a particular event topic. Handler must
     be a function of three arguments: the application state atom, the
    application update channel, and the incoming message."
     [key topic handler]
     (swap! global-listeners
            (fn [ls]
              (do
                (when (key ls)
                  (println (str "Replacing listener key " key " for topic " topic)))
                (assoc ls key [topic handler]))))))

#?(:clj
   (defmacro deflistener
     "Add a global listener for a particular event topic."
     [listener-name topic arglist & body]
     `(ionsails-web.event/add-global-listener (quote ~listener-name) ~topic
                                              (fn ~arglist ~@body))))


#?(:cljs
   (defn- register-listeners
     "Subscribes all the globally registered listeners to the
  event pub channel"
     [world event-chans]
     (let [update-pub (get @event-chans :event-pub)
           update-ch (get @event-chans :event-chan)]
       (doseq [[topic handler] (vals @global-listeners)]
         (let [c (chan)]
           (sub update-pub topic c)
           (go-loop []
             (handler world (dissoc (<! c) :topic))
             (recur)))))))

#?(:cljs
   (defn reset-listeners
     [world]
     (let [update-pub (get @event-chans :event-pub)]
       (unsub-all update-pub)
       (register-listeners world event-chans))))

#?(:cljs
   (defn send
     "Send a message to the given topic. Returns the message (mostly
      so it can return a truthful object when used as the body of an event
      handler)"
     [topic message]
     (let [update-ch (get @event-chans :event-chan)]
       (put! update-ch (assoc message :topic topic))
       message)))

#?(:cljs
   (defn initialize
     [world]
     (let [update-ch (chan)
           update-pub (pub update-ch :topic)]
       (swap! event-chans assoc :event-chan update-ch)
       (swap! event-chans assoc :event-pub update-pub))
     (register-listeners world event-chans)))
