(ns tiltontec.its-alive.cell-types
  (:require [tiltontec.its-alive.utility :refer :all :as ut]
            [tiltontec.its-alive.globals :refer :all :as ns]))

(comment
  (defstruct (cell (:conc-name c-))
    model
    slot-name
    value
    input?
    synaptic
    (caller-store (make-fifo-queue) :type cons) ;; (C3) notify callers FIFO
    (state :nascent :type symbol) ;; :nascent, :awake, :optimized-away
    (value-state :unbound :type symbol) ;; :unbound | :unevaluated | :uncurrent | :valid
    ;; uncurrent (aka dirty) new for 06-10-15. we need this so
    ;; c-quiesce can force a caller to update when asked
    ;; in case the owner of the quiesced cell goes out of existence
    ;; in a way the caller will not see via any kids dependency. Saw
    ;; this one coming a long time ago: depending on cell X implies
    ;; a dependency on the existence of instance owning X
    (pulse 0 :type fixnum)
    (pulse-last-changed 0 :type fixnum) ;; lazys can miss changes by missing
    ;; change of X followed by unchange of X in subsequent DP

    (pulse-observed 0 :type fixnum)
    lazy
    (optimize t)
    debug
    md-info))

(derive ::cell ::object)
(derive ::c-formula ::cell)

(defn c-model [rc]
  (:me @rc))

(defn c-slot [rc]
  (:slot @rc))

(defn c-slot-name [rc]
  (:slot @rc))

(defn c-state [rc]
  (:state @rc))

(defn c-input? [rc]
  (:input? @rc))

(defn c-rule [rc]
  (:rule @rc))

(defn c-pulse-last-changed [rc]
  (:pulse-last-changed @rc))

(defn c-pulse-observed [rc]
  (:pulse-observed @rc))

(defn c-pulse [rc]
  (:pulse @rc))

(defn c-useds [rc]
  (:useds @rc))

(defn c-optimize [rc]
  (:optimize @rc))

(defn c-value [rc]
  (:value @rc))

(defn ephemeral? [c]
  (:ephemeral @c))

(defn c-optimized-away? [c]
  (= :optimized-away (c-state c)))

(defn c-value-state [rc]
  (case (c-value rc)
    unbound :unbound
    unevaluated :unevaluated
    uncurrent :uncurrent
    :valid))

(defn c-unbound? [rc]
  (= :unbound (c-value-state rc)))

(defn c-valid? [rc]
  (= :valid (c-value-state @rc)))

(defn c-callers [rc]
  (:callers @rc))

(defn caller-ensure [used new-caller]
  (alter used assoc :callers (conj (c-callers used) new-caller)))

(defn caller-drop [used caller]
  (alter used assoc :callers (disj (c-callers used) caller)))


(defn cache-state-bound-p [value-state]
  (or (= value-state :valid)
    (= value-state :uncurrent)))

(defn cache-bound-p [c]
  (cache-state-bound-p (c-value-state c)))


; --- model awareness ---------------------------------

(defn mdead?-dispatch [me]
  (assert (md-ref? me))
  [(type me)])

(defmulti mdead? mdead?-dispatch)

(defmethod mdead? :default [me]
  false)

(defn md-slot-value-assume-dispatch [me]
  (assert (md-ref? me))
  [(type me)])

(defmulti md-slot-value-assume md-slot-value-assume-dispatch)

(defmethod md-slot-value-assume :default [me])




; -----------------------------------------------------
(comment
  (defstruct (c-ruled
              (:include cell)
              (:conc-name cr-))
    (code nil :type list) ;; /// feature this out on production build
    rule))


;---------------------------

(comment
  (defstruct (c-dependent
              (:include c-ruled)
              (:conc-name cd-))
    ;; chop (synapses nil :type list)
    (useds nil :type list)
    (usage (blank-usage-mask)))

  (defstruct (c-drifter
              (:include c-dependent)))

  (defstruct (c-drifter-absolute
              (:include c-drifter))))


(comment
  (defmethod c-print-value ((c c-ruled) stream)
    (format stream "%s" (cond ((c-valid? c) (cons (c-value c) "<vld>"))
                              ((c-unbound? c) "<unb>")
                              ((not (c-current? c)) "dirty")
                              (t "<err>"))))

  (defmethod c-print-value (c stream)
    (declare (ignore c stream))))

:cell-types-ok