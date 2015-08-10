(ns ionsails.core-test
  (:require [clojure.test :refer :all]
            [ionsails.core :refer :all]))

(def test-tileset [
  [" " :black :black]
  [" " :black :blue]
  ["." :yellow :green]
  ["." :black :green]
  ["#" :yellow :green]
  ["@" :yellow :green]
  ["^" :yellow :green]
])

(def test-game-board [
 [1 2 1 1 1 1 1 1]
 [1 2 2 2 2 1 1 1]
 [1 2 2 2 2 2 1 1]
 [1 2 2 2 2 1 1 1]
 [1 2 2 2 1 1 1 1]
 [1 1 1 1 1 1 1 1]
])


(deftest tile-formatter-test
  (testing "Failed"
    (is (=
         (tile-formatter (get test-tileset 1))
         [" " {:fg :black, :bg :blue}]))))

(deftest row->put-cmds-test
  (testing "Failed"
    (is (=
         (row->put-cmds test-game-board test-tileset 1)
         '((0 1 " " {:fg :black, :bg :blue})
           (1 1 "." {:fg :yellow, :bg :green})
           (2 1 "." {:fg :yellow, :bg :green})
           (3 1 "." {:fg :yellow, :bg :green})
           (4 1 "." {:fg :yellow, :bg :green})
           (5 1 " " {:fg :black, :bg :blue})
           (6 1 " " {:fg :black, :bg :blue})
           (7 1 " " {:fg :black, :bg :blue}))))))

(deftest game-board->cmds-test
  (testing "Failed"
    (is (=
         (game-board->cmds [
                            [1 2 2 1]
                            [1 2 1 1]
                            [1 2 2 1]])
         '((0 0 " " {:fg :black, :bg :blue})
           (1 0 "." {:fg :yellow, :bg :green})
           (2 0 "." {:fg :yellow, :bg :green})
           (3 0 " " {:fg :black, :bg :blue})
           (0 1 " " {:fg :black, :bg :blue})
           (1 1 "." {:fg :yellow, :bg :green})
           (2 1 " " {:fg :black, :bg :blue})
           (3 1 " " {:fg :black, :bg :blue})
           (0 2 " " {:fg :black, :bg :blue})
           (1 2 "." {:fg :yellow, :bg :green})
           (2 2 "." {:fg :yellow, :bg :green})
           (3 2 " " {:fg :black, :bg :blue}))))))

(deftest game-board-put-test
  (testing "Failed"
    (is (=
         (game-board-put '(
           (1 2 "." {:fg :yellow, :bg :green})
           (2 2 "." {:fg :yellow, :bg :green})) list)
         'nil))))
