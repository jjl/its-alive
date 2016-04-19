
(ns tiltontec.its-alive.cells-test
  (:require [clojure.test :refer :all]
            [tiltontec.its-alive.utility :refer :all] 
            [tiltontec.its-alive.cell-types :refer :all :as cty]
            [tiltontec.its-alive.globals :refer :all]
            [tiltontec.its-alive.observer :refer :all]
            [tiltontec.its-alive.evaluate :refer :all]
            [tiltontec.its-alive.cells :refer :all]
            ))

(set! *print-level* 3)

(deftest test-input
  (let [c (make-cell 
             :slot :mol
             :value 42)]
    (is (isa? ia-types (type @c) ::cty/cell))
    (is (= (c-value c) 42))
    (is (= (c-value-state c) :valid))
    (is (= #{} (c-callers c)))
    (is (c-input? c))
    (is (nil? (c-model c)))
    (is (= :mol (c-slot c)))
    ))

(deftest test-c-in
  (let [c (c-in 42)] 
    (is (isa? ia-types (type @c) ::cty/cell))
    (is (= (c-value c) 42))
    (is (= (c-value-state c) :valid))
    (is (= #{} (c-callers c)))
    (is (c-input? c))
    (is (c-valid? c))
    (is (nil? (c-model c)))
    ))

(deftest test-c-in+
  (let [c (c-in 42 :slot :cool)] 
    (is (isa? ia-types (type @c) ::cty/cell))
    (is (c-ref? c))
    (is (= (c-value c) 42))
    (is (= (c-value-state c) :valid))
    (is (= #{} (c-callers c)))
    (is (c-input? c))
    (is (nil? (c-model c)))
    (is (= :cool (c-slot c)(c-slot-name c)))
    ))

(deftest test-c-formula
  (let [c (c? (+ 40 2))] 
    (is (isa? ia-types (type @c) ::cty/c-formula))
    (is (fn? (c-rule c)))
    (is (= (c-value c) unevaluated))
    (is (= (c-value-state c) :unevaluated))
    (is (= #{} (c-callers c)))
    (is (= #{} (c-useds c)))
    (is (not (c-input? c)))
    (is (nil? (c-model c)))
    ))

(deftest t-c?+
  (let [c (c?+ (:optimize false :slot :bingo)
               (println :cool)
               (+ 40 2))]
    (is (isa? ia-types (type @c) ::cty/c-formula))
    (is (c-ref? c))
    (is (fn? (c-rule c)))
    (is (= (c-value c) unevaluated))
    (is (= (c-value-state c) :unevaluated))
    (is (= #{} (c-callers c)))
    (is (= #{} (c-useds c)))
    (is (not (c-input? c)))
    (is (nil? (c-model c)))
    (is (not (c-optimize c)))
    (is (= :bingo (c-slot c)(c-slot-name c)))))


(deftest t-eph-1
  (cells-init)
  (let [boct (atom 0)
        b (c-in nil
                :slot :b
                :obs (fn-obs (swap! boct inc))
                :ephemeral? true)
        crun (atom 0)
        cobs (atom 0)
        c (c?+ [:slot :c 
                :obs (fn-obs (swap! cobs inc))]
               (println :bingo)
               (swap! crun inc)
               (prog1
                (str "Hi " (cell-read b))
                (trx :cellread!! @b)))]
    (assert (c-rule c) "Early no rule")
    (is (nil? (c-value b)))
    (println :valstate (c-value-state b))
    (is (= :valid (c-value-state b)))
    (is (c-valid? b))
    (println b)
    (println @b)
    (is (c-valid? b))
    (is (= "Hi " (cell-read c)))
    (is (= 1 @boct))
    (is (= 1 @crun @cobs))
    (is (nil? (:value @b)))

    (do
      (trx :first-b-reset!!!!!!!!!!!)
      (c-reset! b "Mom")
      (is (= "Hi Mom" (cell-read c)))
      (is (= 2 @boct))
      (is (= 2 @crun @cobs))
      (is (nil? (c-value b)))
      (is (nil? (:value @b))))

    (do
      (trx :second-b-reset!!!!!!!!!!!)
      (c-reset! b "Mom")
      (is (= "Hi Mom" (cell-read c)))
      (is (= 3 @boct))
      (is (= 3 @crun))
      (is (= 2 @cobs))
      (is (nil? (c-value b)))
      (is (nil? (:value @b))))
    ))



