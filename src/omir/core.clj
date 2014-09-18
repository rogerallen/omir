(ns omir.core
  (:require [overtone.live :as o]
            [omir.gui      :as og]
            [omir.live     :as ol]
            [omir.persi    :as op]
            [omir.synth    :as os]))

;; ======================================================================
;; This file is to be used for experimentation & notes
;; The real code is in other files

;; ======================================================================
;; FIXME/TODO
;; x add per-loop control for start/stop/restart
;; x quantize to beats
;; o relate midi timestamp to system timestamp
;; o mix/match working with beats, too (rather than time)
;; o transposition
;; o merge with leipzig?

;; ======================================================================
;; get ready to interact
(do
  (op/init! "data" true)         ; keep your persi data files in the same place
  (op/new!)                      ; each time you want to put work in a new file
  (op/record)                    ; start recording all midi events
  (og/start))                    ; find tiny pop-up window in center of screen

(comment

  ;; commands you may want to use at some point
  (op/save!)                     ; to save work away
  (op/pause)                     ; pause recording midi events
  (op/summary)                   ; what does the file look like?
  (op/open! "140907_145403.clj") ; load that earlier session
  (op/set! :comment "just testing")

  ;; how to use the keyboard events window
  (op/keyboard-tempo)            ; take the last seq and get a tempo from it

  ;; listen to a metronome while you play
  (def ct (ol/click-track 90))
  (reset! ct false) ;; turn it off

  ;; connect a synth to the midi player (choose one)
  (def mpp (o/midi-poly-player os/mpp-piano))
  (def mpp (o/midi-poly-player os/mpp-ektara))
  (def mpp (o/midi-poly-player os/mpp-overpad))
  (o/midi-player-stop) ;; stop the midi when you want

  ;; after you have recorded something, partition into sequences &
  ;; play each
  (def el (op/partition-by-timestamp (op/get-list)))
  (count el)        ; partitions
  (count (last el)) ; events in last partition

  ;; convert sequences to notes
  (def nl (map ol/make-notes el))
  (count nl)
  (count (last nl))

  (def ss (ol/seq-synth os/play-piano (nth nl 10)))
  (reset! ss false)

  ;; make a 8-note 90 bpm recording.  This loops it
  (def ls (ol/loop-synth os/play-piano
                         (nth nl 10)
                         (* 8 (ol/mspb 90))))
  (reset! ls false) ;; FIXME


  (def bl (ol/quantize-notes 90 0.25 (nth nl 10)))

  ;; make seq-synth/loop-synth variants for beats

)
