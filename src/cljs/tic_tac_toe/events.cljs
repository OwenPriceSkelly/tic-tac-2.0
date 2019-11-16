(ns tic-tac-toe.events
  (:require
   [re-frame.core :as rf]
   [tic-tac-toe.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))


(rf/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))


(rf/reg-event-db
 ::mark-square
 (fn-traced [db [_ x y row col]]
            (assoc-in db [:main-board x y :sub-board row col]
                      (:turn db))))

(rf/reg-event-db
 ::rotate-players
 (fn-traced [db _]
            (assoc db :turn (if (= (:turn db) :x) :o :x))))

(defn- set-unclaimed-boards [main-board new-status]
  (reduce (fn [m row]
            (conj m (reduce (fn [r sub-board]
                              (conj r (update sub-board
                                              :status
                                              #(if (#{:x :o} %)
                                                 %
                                                 new-status))))
                            (empty row)
                            row)))
          (empty main-board)
          main-board))

(defn- enable-one-board [main-board row col]
  (-> main-board
      (set-unclaimed-boards :disabled)
      (assoc-in [row col :status] :enabled)))

(rf/reg-event-db
 ::set-active-boards
 (fn-traced [db [_ row col]]
            (cond
              (:winner db)                                           (update db :main-board set-unclaimed-boards :disabled)
              (#{:x :o} (:status (get-in db [:main-board row col]))) (update db :main-board set-unclaimed-boards :enabled)
              :else                                                  (update db :main-board enable-one-board row col))))


(defn- extract-statuses [main-board]
  (reduce (fn [b row]
            (conj b (reduce (fn [r sub-board]
                              (conj r (:status sub-board)))
                            (empty row)
                            row)))
          (empty main-board)
          main-board))


(defn- three-in-a-row? [board]
  (let [rows  board
        cols  (apply map vector rows)
        diags (for [indexer [#(vector % %) #(vector % (- 2 %))]]
                (vec (for [i (range 3)]
                       (get-in rows (indexer i)))))]
    (->> (concat rows cols diags)
         (filter (fn [row] (apply = row)))
         (map first)
         (filter #{:x :o})
         first)))

(rf/reg-event-db
 ::claim-sub-board
 (fn-traced [db [_ x y winner]]
            (assoc-in db [:main-board x y :status] winner)))

(rf/reg-event-db
 ::check-game-over
 (fn-traced [db _]
            (->> (:main-board db)
                 extract-statuses
                 three-in-a-row?
                 (assoc db :winner))))

;; ------------------------------------------------------------
;; layer: complex events (reg-fx) with multiple effects

(rf/reg-event-fx
 ::check-win
 (fn-traced [cofx [_ x y row col]]
            (if-let [winner (three-in-a-row? (get-in cofx [:db :main-board x y :sub-board]))]
              {:dispatch-n [[::claim-sub-board x y winner]
                            [::check-game-over]
                            [::set-active-boards row col]]}

              {:dispatch [::set-active-boards row col]})))

(rf/reg-event-fx
 ::make-move
 (fn-traced [_ [_ x y row col]]
            {:dispatch-n [[::mark-square x y row col]
                          [::rotate-players]
                          [::check-win x y row col]]}))
