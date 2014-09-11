(ns omir.live
  (:require [overtone.live :as o]
            [omir.synth    :as os]
            [omir.persi    :as op]))

(defn mspb
  "milliseconds per beat"
  [tempo]
  (* 1000 (/ 60.0 tempo)))

(defn seq-synth
  "play a sequence of notes starting at t0 through the synth"
  ([synth notes]
     (seq-synth synth (o/now) notes))
  ([synth t0 notes]
     (when-not (empty? notes)
       (synth (+ t0 (:timestamp (first notes)))
              (:note (first notes))
              (:duration (first notes)))
       (when-not (empty? (rest notes))
         (o/apply-by (+ t0 (:timestamp (first (rest notes))))
                     #'seq-synth [synth t0 (rest notes)])))))

;; FIXME - return a structure for controlling this individually
(defonce do-loop (atom false))
(defn loop-synth
  "repeatedly play a sequence of notes through the synth"
  ([synth notes Δt]
     (reset! do-loop true)
     (loop-synth synth (o/now) notes Δt))
  ([synth t0 notes Δt]
     (loop-synth synth t0 notes Δt notes))
  ([synth t0 orig-notes Δt notes]
     (when @do-loop
       (let [t1 (+ t0 Δt)]
         (if-not (empty? notes)
           (do
             (synth (+ t0 (:timestamp (first notes)))
                    (:note (first notes))
                    (:duration (first notes)))
             (if-not (empty? (rest notes))
               (o/apply-by (+ t0 (:timestamp (first (rest notes))))
                           #'loop-synth [synth t0 orig-notes Δt (rest notes)])
               (o/apply-by (+ t0 Δt)
                           #'loop-synth [synth (+ t0 Δt) orig-notes Δt orig-notes])))
           (o/apply-by (+ t0 Δt)
                       #'loop-synth [synth (+ t0 Δt) orig-notes Δt orig-notes]))))))

;; FIXME - return a structure for controlling this
(defonce do-click (atom false))
(defn click-track
  "repeatedly play a click track"
  ([tempo]
     (reset! do-click true)
     (click-track false (o/now) tempo))
  ([active t tempo]
     (let [Δt (mspb tempo)]
       (if active
         (os/play-click t))
       (when @do-click
         (o/apply-by (+ t Δt) #'click-track [true (+ t Δt) tempo])))))
