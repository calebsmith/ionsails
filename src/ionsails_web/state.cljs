(ns ionsails-web.state
  (:require [amalloy.ring-buffer :as buff]
            [ionsails-web.util.trie :as trie]
            [ionsails-web.data.core :as datacore]))

(defn initial-state
  []
  (let [sys (datacore/build-initial-system)]
    {:console/messages-main (buff/ring-buffer 150)
     ;; Hard- coded for now
     :command-completions (trie/build-trie ["look" "go" "where" "when"])
     :system sys
     :player-id (datacore/get-player-id sys)
     }))
