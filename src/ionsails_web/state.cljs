(ns ionsails-web.state
  (:require [amalloy.ring-buffer :as buff]
            [ionsails-web.util.trie :as trie]))

(defn initial-state
  []
  {:console/messages-main (buff/ring-buffer 150)
   ;; Hard- coded for now
   :command-completions (trie/build-trie ["look" "go" "where" "when"])})
