(ns ionsails-web.util.uuid
  (:require [clojure.string :as string]
            [goog.string.StringBuffer]))


(defn ^:private rand16
  []
  (.toString (rand-int 16) 16))

(defn ^:private masked-rand15
  []
  (.toString  (bit-or 0x8 (bit-and 0x3 (rand-int 15))) 16))

(defn make-uuid []
  (let [f rand16
        g masked-rand15]
                                        ; Using a StringBuffer and calling f without repeat is somewhat ugly but roughly 10x faster
    (UUID. (.toString
            (goog.string.StringBuffer.
             (f) (f) (f) (f) (f) (f) (f) (f)
             "-"
             (f) (f) (f) (f)
             "-4"
             (f) (f) (f)
             "-"
             (g) (f) (f) (f)
             "-"
             (f) (f) (f) (f) (f) (f) (f) (f) (f) (f) (f) (f))) nil)))
