(ns tic-tac-toe.subs
  (:require
   [re-frame.core :as rf]
   [clojure.string :as string]))

;; LAYER 2: direct access to DB
(rf/reg-sub
 ::name
 (fn [db _]
   (:name db)))

(rf/reg-sub
 ::main-board
 (fn [db _]
   (:main-board db)))

(rf/reg-sub
 ::winner
 (fn [db _]
   (:winner db)))

(rf/reg-sub
 ::turn
 (fn [db _]
   (:turn db)))

(rf/reg-sub
 ::player
 (fn [db _]
   (:player db)))

(rf/reg-sub
 ::app-state
 (fn [db _] db))

;; LAYER 3: Derived data for views
;; -------------------------------------------------------
;; EXAMPLE:
;; (reg-sub
;;  ::sub-id
;;  (fn signal-fn [[_ x y z :as query-vec]]
;;    (subscribe [::other-subscription x y z]))
;;  (fn computation-fn [signal-output query-vec]
;;    (do-something signal-output query-vec)))
;; -------------------------------------------------------
;; N.B. the query vec is what defines a node in the graph, not the
;; reg-sub template for the node. The query vec is the same for both
;; Sugar:
;; :<- [::signal-id]
;; Desugared: (?)
;; (fn signal [[_ optional query params]]
;;    (subscribe [::signal-id optional query params]))

(rf/reg-sub
 ::sub-board
 :<- [::main-board]
 ;; (fn [_ x y] (rf/subscribe [::main-board]))
 (fn
   [main-board [_ x y]]
   (get-in main-board [x y :sub-board])))

(rf/reg-sub
 ::board-status
 (fn
   [[_ x y]]
   (rf/subscribe [::main-board]))
 (fn
   [main-board [_ x y]]
   (get-in main-board [x y :status])))



;; -- Square-by-square subscriptions: ----------
(rf/reg-sub
 ::square-value
 ;; Doesn't work: :<- [::sub-board] ;; NB: Can't use sugar for this chain
 (fn [[_ x y]] ;; doesn't need to be [_ x y _ _], though that'd probably be clearer
   (rf/subscribe [::sub-board x y]))
 ;; NOTE: needs to indicate which positional args are used, since the assumption is
 ;; that the two vectors are element-wise identical wherever they both have elements
 (fn [sub-board [_ x y row col]]
   (get-in sub-board [row col])))

(rf/reg-sub
 ::square-disabled?
 (fn
   [[_ x y row col]]
   [(rf/subscribe [::board-status x y])
    (rf/subscribe [::square-value x y row col])])
 (fn
   [[board-status value] _]
   (or (#{:x :o} value)
       (not (#{:enabled} board-status)))))

(rf/reg-sub
 ::square-label
 (fn
   [[_ x y row col]]
   (rf/subscribe [::square-value x y row col]))
 (fn
   [value _]
   (if value
     (-> value name string/upper-case)
     "N")))

(rf/reg-sub
 ::square-style
 (fn
   [[_ x y row col]]
   [(rf/subscribe [::square-label x y row col])
    (rf/subscribe [::square-disabled? x y row col])])
 (fn
   [[label disabled?] _]
   (let [background (if disabled? "#bfbfbf" "#f2f2f2")]
     {:color            (case label
                          "X" "#e60000"
                          "O" "#3333ff"
                          "N" background)
      :background-color background})))

(rf/reg-sub
 ::turn-display-text
 :<- [::turn]
 (fn [turn _]
   (-> turn
       name
       string/upper-case
       (str "'s turn"))))

(rf/reg-sub
 ::turn-display-style
 :<- [::turn]
 (fn [turn _]
   {:color     (case turn
                 :x "#e60000"
                 :o "#3333ff")
    :font-size "32px"}))

(rf/reg-sub
 ::winner-display-text
 :<- [::winner]
 (fn [winner _]
   (-> winner
       name
       string/upper-case
       (str " wins!"))))

(rf/reg-sub
 ::winner-display-style
 :<- [::winner]
 (fn [winner _]
   {:color     (case winner
                 :x "#e60000"
                 :o "#3333ff")
    :font-size "40px"}))
(rf/reg-sub
 ::x-score
 :<- [::main-board]
 (fn [main-board _]
   (count (filter #{:x} (for [row       main-board
                              sub-board row]
                          (:status sub-board))))))

(rf/reg-sub
 ::o-score
 :<- [::main-board]
 (fn [main-board _]
   (count (filter #{:o} (for [row       main-board
                              sub-board row]
                          (:status sub-board))))))
