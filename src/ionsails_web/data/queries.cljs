(ns ionsails-web.data.queries
  (:require [clojure.string :as s]
            [cljs.core.match :refer-macros [match]]
            [brute.entity :as ent]
            [ionsails-web.data.components :as c]))

(defn match-keyword
  [sys entities kw-query kw-query-index]
  (let [ent-triples (map (fn [e] [e
                                  (set (:keywords (ent/get-component sys e c/Keywords)))
                                  (:name (ent/get-component sys e c/Ident))])
                         entities)
        ent-triples-candidates (filter (fn [[id kws name]]
                                         (contains? kws kw-query))
                                       ent-triples)
        ent-triples-candidates (vec (sort-by #(nth % 2) ent-triples-candidates))
        kw-query-index (if (< kw-query-index (count ent-triples-candidates))
                         (if (< kw-query-index 0) 0 (dec kw-query-index))
                         (dec (count ent-triples-candidates)))
        [item-id item-kw item-name] (get ent-triples-candidates kw-query-index)]
    item-id))

(defn ->int
  [n]
  (js/parseInt n))

(defn int?
  [n]
  (-> n ->int js/isNaN not))

(defn parse-q-kw-container
  [c-args]
  (match [(mapv int? c-args) c-args]
         [[false] [kw]] [1 kw 1 nil]
         [[true false] [q kw]] [(->int q) kw 1 nil]
         [[false true]  [kw kw-index]] [1 kw (->int kw-index) nil]
         [[true false true] [q kw kw-index]] [(->int q) kw (->int kw-index) nil]
         [[false _ & _] [kw (:or "in" "from") & container]] [1 kw 1 container]
         [[true false _ & _] [q kw (:or "in" "from") & container]] [(->int q) kw 1 container]
         [[false true _ & _]  [kw kw-index (:or "in" "from") & container]] [1 kw (->int kw-index) container]
         [[true false true _ & _] [q kw kw-index (:or "in" "from") & container]] [(->int q) kw (->int kw-index) container]
         :else [0 "" 1 nil]))

(defn parse-kw-container
  [c-args]
  (match [(mapv int? c-args) c-args]
         [[false] [kw]] [1 kw 1 nil]
         [[false true]  [kw kw-index]] [1 kw (->int kw-index) nil]
         [[false _ & _] [kw (:or "in" "from") & container]] [1 kw 1 container]
         [[false true _ & _]  [kw kw-index (:or "in" "from") & container]] [1 kw (->int kw-index) container]
         :else [0 "" 1 nil]))

(defn parse-kw-or-container
  [c-args]
  (match [(mapv int? c-args) c-args]
         [[false] [kw]] [1 kw 1 nil]
         [[false true]  [kw kw-index]] [1 kw (->int kw-index) nil]
         [[_ & _] [(:or "in" "from") & container]] [1 "" 1 container]
         :else [0 "" 1 nil]))

(defn find-keywords-in
  [sys candidates [q kw kw-index]]
  (if (= kw "all")
    candidates
    (remove nil?
            (loop [candidates candidates
                   result-ids []
                   i 0]
              (if (< i q)
                (let [res-id (match-keyword sys candidates kw kw-index)]
                  (recur
                   (remove #(= % res-id) candidates)
                   (conj result-ids res-id)
                   (inc i)))
                result-ids)))))

(defn find-best-keywords-in
  [sys candidates query]
  (first (find-keywords-in sys candidates query)))
