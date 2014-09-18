(ns omir.gui
  (:use [overtone.music.time :only [now]])
  (:require [omir.persi :as op]
            [quil.core  :as q]))

;; ======================================================================
;; keyboard events via a Quil window.  Had to do this because
;; interacting with unbuffered keybaord events inside emacs is
;; difficult/impossible.
;;
;; also allows us to draw keyboard events

(defonce record-keyboard-keycount (atom 0))

(defn- record-keyboard-setup
  []
  (reset! record-keyboard-keycount 0)
  (q/background 255))

(defn- record-keyboard-draw
  "draws grey when a key is down"
  []
  (if (> @record-keyboard-keycount 0)
    (q/background 128)
    (q/background 255)))

(defn- record-keyboard-key-pressed
  "records the keycode and time in µs"
  []
  (swap! record-keyboard-keycount inc)
  (op/append! {:command   :key-pressed
            :keycode   (q/key-code)
            :timestamp (* 1000 (now))}))

(defn- record-keyboard-key-released
  "records the keycode and time in µs"
  []
  (swap! record-keyboard-keycount dec)
  (op/append! {:command   :key-released
            :keycode   (q/key-code)
            :timestamp (* 1000 (now))}))

(defn start
  "bring up quil window that allows for recording keyboard events"
  []
  (q/defsketch omir-sketch
    :title        "Omir"
    :setup        record-keyboard-setup
    :draw         record-keyboard-draw
    :key-pressed  record-keyboard-key-pressed
    :key-released record-keyboard-key-released
    :size         [128 128]
    :features     [:keep-on-top]))
