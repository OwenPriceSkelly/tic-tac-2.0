(ns tic-tac-toe.db)




(defn- init-sub-board []
  {:status    :enabled ;; alternatively, :disabled, :x or :o
   :sub-board (vec (repeat 3 (vec (repeat 3 nil))))})

(defn- init-game-board []
  (vec (for [_ (range 3)]
         (vec (for [_ (range 3)]
                (init-sub-board))))))

(def default-db
  {:name       "Tic-Tac-2.0"
   :main-board (init-game-board)
   :turn       :x
   :winner     nil})
