(ns ionsails-web.util.trie)

(defn- add-to-trie [trie x]
  (assoc-in trie x (merge (get-in trie x) {:terminal true})))

(defn- flatten-map
  ([form]
   (into {} (flatten-map form nil)))
  ([form pre]
   (mapcat (fn [[k v]]
             (let [prefix (if pre (str pre (name k)) (name k))]
               (if (and (map? v)
                        (not (contains? v :terminal)))
                 (flatten-map v prefix)
                 (if (not= k :terminal)
                   [[prefix v]]
                   []))))
           form)))

(defn in-trie? [trie x]
  "Returns true if the value x exists in the specified trie."
  (contains? (get-in trie (vec x)) :terminal))

(defn lookup [trie prefix]
  "Returns a list of matches with the prefix specified in the trie specified."
  (let [sub-trie (get-in trie (vec prefix))
        suggestions (map #(str prefix %) (keys (flatten-map sub-trie)))]
    (if (in-trie? trie prefix)
      (conj suggestions prefix)
      suggestions)))

(defn build-trie [coll]
  "Builds a trie over the values in the specified seq coll."
  (reduce add-to-trie {} coll))
