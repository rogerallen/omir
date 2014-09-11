(ns omir.core
  (:require [overtone.live :as o]
            [omir.live     :as ol]
            [omir.persi    :as op]
            [omir.synth    :as os]))

;; ======================================================================
;; This file is to be used for experimentation & notes
;; The real code is in other files

;; ======================================================================
;; FIXME/TODO
;; o relate midi timestamp to system timestamp
;; o add per-loop control for start/stop/restart
;; o quantize to beats
;; o mix/match working with beats, too (rather than time)
;; o transposition
;; o merge with leipzig?

;; ======================================================================
;; get ready to interact
(do
  (op/init! "data" true)        ; keep your persi data files in the same place
  (op/new!)                     ; each time you want to put work in a new file
  (op/record)                   ; start recording all midi events
  (op/record-keyboard-events))  ; find tiny pop-up window in center of screen

(comment

  ;; commands you may want to use at some point
  (op/save!)                     ; to save work away
  (op/pause)                     ; pause recording midi events
  (op/summary)                   ; what does the file look like?
  (op/open! "140907_145403.clj") ; load that earlier session

  ;; how to use the keyboard events window
  (op/keyboard-tempo)         ; take the last seq and get a tempo from it

  ;; listen to a metronome while you play
  (ol/click-track 90)
  (reset! ol/do-click false) ;; FIXME

  ;; connect a synth to the midi player
  (def mpp (o/midi-poly-player os/mpp-piano))
  (o/midi-player-stop) ;; stop the midi when you want

  ;; after you have recorded something, partition into sequences &
  ;; play each
  (def el (op/partition-by-timestamp (op/get-list)))
  (count el)        ; partitions
  (count (last el)) ; events in last partition

  ;; convert sequences to notes
  (def nl (map op/make-notes el))
  (count nl)
  (count (last nl))

  (ol/seq-synth os/play-piano (o/now) (last nl))

  ;; make a 8-note 90 bpm recording.  This loops it
  (ol/loop-synth os/play-piano
              (op/make-notes (last (op/partition-by-timestamp (op/get-list))))
              (* 8 (ol/mspb 90)))
  (reset! ol/do-loop false) ;; FIXME

)
