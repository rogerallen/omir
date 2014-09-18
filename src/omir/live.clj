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
     (let [do-loop (atom true)]
       (seq-synth do-loop synth (o/now) notes)
       do-loop))
  ([do-loop synth t0 notes]
     (when @do-loop
       (when-not (empty? notes)
         (synth (+ t0 (:timestamp (first notes)))
                (:note (first notes))
                (:duration (first notes)))
         (when-not (empty? (rest notes))
           (o/apply-by (+ t0 (:timestamp (first (rest notes))))
                       #'seq-synth [do-loop synth t0 (rest notes)]))))))

(defn loop-synth
  "repeatedly play a sequence of notes through the synth"
  ([synth notes Δt]
     (let [do-loop (atom true)]
       (loop-synth do-loop synth (o/now) notes Δt)
       do-loop))
  ([do-loop synth t0 notes Δt]
     (loop-synth do-loop synth t0 notes Δt notes))
  ([do-loop synth t0 orig-notes Δt notes]
     (when @do-loop
       (let [t1 (+ t0 Δt)]
         (if-not (empty? notes)
           (do
             (synth (+ t0 (:timestamp (first notes)))
                    (:note (first notes))
                    (:duration (first notes)))
             (if-not (empty? (rest notes))
               (o/apply-by (+ t0 (:timestamp (first (rest notes))))
                           #'loop-synth [do-loop synth t0 orig-notes Δt (rest notes)])
               (o/apply-by (+ t0 Δt)
                           #'loop-synth [do-loop synth (+ t0 Δt) orig-notes Δt orig-notes])))
           (o/apply-by (+ t0 Δt)
                       #'loop-synth [do-loop synth (+ t0 Δt) orig-notes Δt orig-notes]))))))

(defn click-track
  "repeatedly play a click track.  Returns an atom to control the clicking."
  ([tempo]
     (let [do-click (atom true)]
       (click-track do-click false (o/now) tempo)
       do-click))
  ([do-click active t tempo]
     (let [Δt (mspb tempo)]
       (if active
         (os/play-click t))
       (when @do-click
         (o/apply-by (+ t Δt) #'click-track [do-click true (+ t Δt) tempo])))))

;; moving this from persi.

(defn make-notes
  "convert a sequence of events into a sequence of notes with
  duration.  NOTE! time is now 0 at the start of the sequence and this
  changes timestamps from µs to ms."
  [event-list0]
  (let [ft (:timestamp (first (drop-while #(not= (:command %) :note-on) event-list0)))]
    (loop [event-list event-list0 first-timestamp ft notes []]
      (let [event-list   (drop-while #(not= (:command %) :note-on) event-list)
            cur-note-on  (first event-list)
            event-list   (rest event-list)
            cur-note-off (first
                          (drop-while
                           #(or (= (:command %) :note-on)
                                (not= (:note %) (:note cur-note-on))) event-list))]
        (if (not (nil? cur-note-on))
          (let [;;_ (println cur-note-on "\n" cur-note-off "\n")
                cur-note {:command   :note
                          :note      (:note cur-note-on)
                          :velocity  (:velocity cur-note-on)
                          :timestamp (/ (- (:timestamp cur-note-on) first-timestamp)
                                        1000.0)
                          :duration  (/ (- (:timestamp cur-note-off)
                                           (:timestamp cur-note-on))
                                        1000.0)}
                notes (conj notes cur-note)]
            (recur event-list first-timestamp notes))
          notes)))))

(defn round
  "round to nearest integer since int truncates."
  [x]
  (int (+ x 0.5)))

(defn quantize
  [bpm quanta t]
  (let [bps (/ bpm 60.0)]
    ;;(swank.core/break)
    (* quanta (round (/ (* bps t) quanta)))))

(defn quantize-notes
  "convert a sequence of notes with duration (in seconds) into a
  sequence of notes with duration in beats.  quantize to the nearest
  quanta of a beat."
  [bpm quanta notes]
  (map #(assoc %
          :timestamp (quantize bpm quanta (/ (:timestamp %) 1000.0))
          ;; don't let duration = 0
          :duration (max quanta (quantize bpm quanta (/ (:duration %) 1000.0))))
         notes))
