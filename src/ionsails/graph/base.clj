(ns ionsails.graph.base
  (:require [loom.graph :as lg]
            [loom.alg :as la]
            [loom.gen :as lgen]
            [loom.alg-generic :as lag]))


(comment

  (def g
    (lg/graph 1 2 3 4 5 6 7 8 {1 [2 3] 3 [1 2]} {4 [5 7 6] 6 [5 7]} [2 4] [5 7]))

  g

  (def wg (lg/weighted-graph g))

  (la/bf-path g 1 6)


  (la/dijkstra-path (lg/weighted-graph g) 1 5)

  (la/loners g)

  (first (reverse  (sort-by count
                            (la/maximal-cliques g))))

  (la/shortest-path g 1 7)

  (la/scc (lg/digraph g))

  (lag/trace-paths g)


  (lgen/gen-rand (lg/graph) 9 7 )

  )

