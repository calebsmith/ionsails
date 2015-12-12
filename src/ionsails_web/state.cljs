(ns ionsails-web.state
  (:require [amalloy.ring-buffer :as buff]
            [ionsails-web.logic.command :as cmd]
            [ionsails-web.util.trie :as trie]
            [ionsails-web.data.core :as datacore]))

(defn initial-ui
  []
  {:console.messages (buff/ring-buffer 250)
   :command.completions (trie/build-trie cmd/commands)
   :command.history (buff/ring-buffer 50)})

(defn initial-state
  []
  (let [sys (datacore/build-initial-system)]
    {:ui (initial-ui)
     :system sys
     :player-id (datacore/get-player-id sys)}))
