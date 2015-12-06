(ns ionsails-web.ui.components.graph
  (:require [quiescent.core :as q :include-macros true]
            [quiescent.dom :as d]
            [cljsjs.vis :as vis]))

(declare test-network)

(q/defcomponent Graph
  :on-render (fn [_ world] (test-network world))
  [world]
  (d/div {:className "network-graph" :id "graph1"}))

(def ^:private network-options
  {:physics {:enabled false}
   :edges {:arrows {:to {:enabled true}}}
   :interaction {:dragView false
                 :multiselect true
                 :zoomView false}})

(defn build-network
  [elm-id nodes edges]
  (let [nodes (js/vis.DataSet. (clj->js nodes))
        edges (js/vis.DataSet. (clj->js edges))
        container (.getElementById js/document elm-id)]
    (js/vis.Network. container #js {:nodes nodes
                                    :edges edges}
                     (clj->js network-options))))

(defn- nodes-map
  [nodes]
  (for [[k v] nodes] {:id k :label (str v)}))

(defn- edges-map
  [edges]
  (for [[from to] edges] {:from from :to to :label "B" :title "tip"}))

(defn test-network
  ;; FIXME: For now just hard-codes node/edge data
  [world]
  (let [nodes (nodes-map {1 "A", 2 2, 3 4, 4 4}) 
        edges (edges-map [[1 2] [2 1] [2 3]])]
    (prn "HI")
    (build-network "graph1" nodes edges)))

