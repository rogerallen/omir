(ns omir.synth
  (:require [overtone.live               :as o]
            [overtone.synth.stringed     :as oss]
            [overtone.inst.synth         :as osynth]
            [overtone.inst.sampled-piano :as piano]))

(def mpp-piano (partial piano/sampled-piano
                        :gate 1 :play-buf-action o/NO-ACTION))
(defn play-piano
  [t note dur]
  (let [s (o/at t (piano/sampled-piano note))]
    (o/at (+ t dur) (o/ctl s :gate 0))))

(def mpp-ektara (partial oss/ektara :gate 1))
(defn play-ektara
  [t note dur]
  (let [s (o/at t (oss/ektara note 1))]
    (o/at (+ t dur) (o/ctl s :gate 0))))

(defn play-overpad
  [t note dur]
  (let [s (o/at t (osynth/overpad note))]
    (o/at (+ t dur) (o/ctl s :gate 0))))

(def click (o/sample (o/freesound-path 87731)))
(defn play-click
  [t]
  (click :amp 0.25))
