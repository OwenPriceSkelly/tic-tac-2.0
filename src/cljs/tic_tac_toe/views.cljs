(ns tic-tac-toe.views
  (:require
   [re-frame.core :as rf]
   [clojure.string :as string]
   [re-com.core :as rc]
   [tic-tac-toe.subs :as subs]
   [tic-tac-toe.events :as events]))

(defn title []
  (let [name (rf/subscribe [::subs/name])]
    [rc/title
     :label (str @name)
     :level :level1]))

(defn square [x y row col]
  [rc/box
   :child [rc/button
           :disabled? @(rf/subscribe [::subs/square-disabled? x y row col])
           :label     @(rf/subscribe [::subs/square-label x y row col])
           :style     @(rf/subscribe [::subs/square-style x y row col])
           :on-click  #(rf/dispatch  [::events/make-move x y row col])]])

(defn sub-board [x y]
  [rc/v-box
   :gap "2px"
   :children (for [row (range 3)]
               [rc/h-box
                :gap "2px"
                :children (for [col (range 3)]
                            [square x y row col])])])

(defn main-board []
  [rc/v-box
   :style {:font-family "monospace"}
   :gap "16px"
   :children (for [x (range 3)]
               [rc/h-box
                :gap "16px"
                :children (for [y (range 3)]
                            [sub-board x y])])])

(defn turn-or-winner-display []
  (if @(rf/subscribe [::subs/winner])
    [rc/box
     :child @(rf/subscribe [::subs/winner-display-text])
     :style @(rf/subscribe [::subs/winner-display-style])]
    [rc/box
     :child @(rf/subscribe [::subs/turn-display-text])
     :style @(rf/subscribe [::subs/turn-display-style])]))


(defn score-display []
  [rc/v-box
   :children [[rc/box :child (str "X: " @(rf/subscribe [::subs/x-score]) " board(s) claimed")]
              [rc/box :child (str "O: " @(rf/subscribe [::subs/o-score]) " board(s) claimed")]]
   :style {:font-size "16px"}])
(defn reset-button []
  [rc/button
   :label    "Reset"
   :class    "btn-warning"
   :on-click #(rf/dispatch [::events/initialize-db])])

(defn main-panel []
  [rc/v-box
   :align :center
   :children [[rc/h-box
               :children [[title]]]
              ;; [score-display]
              [rc/h-box
               :gap "24px"
               :children [[reset-button]
                          [main-board]
                          [rc/v-box :children [[turn-or-winner-display]]]]]
              #_[rc/box
                 :size "100px"]]])
