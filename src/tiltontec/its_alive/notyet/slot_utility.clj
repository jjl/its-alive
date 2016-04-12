(comment

    Cells -- Automatic Dataflow Managememnt



)


(defn c-setting-debug (self slot-name c new-value)
  (declare (ignorable new-value))
  (cond
   ((null c)
    (format t "c-setting-debug > constant  %s in %s may not be altered..init to (c-in nil)"
      slot-name self)
        
    (c-break "setting-const-cell")
    (error "setting-const-cell"))
   ((c-input? c))
   (t
    (let ((self (c-model c))
          (slot-name (c-slot-name c)))
      ;(trx "c-setting-debug sees" c newvalue self slot-name)
      (when (and c (not (and slot-name self)))
        ;; cv-test handles errors, so don't set +stop+ (c-stop)
        (c-break "unadopted %s for self %s spec %s" c self slot-name)
        (error 'c-unadopted :cell c))
      #+whocares (typecase c
        (c-dependent
         ;(trx "setting c-dependent" c newvalue)
         (format t "c-setting-debug > ruled  %s in %s may not be setf'ed"
           (c-slot-name c) self)
         
         (c-break "setting-ruled-cell")
         (error "setting-ruled-cell"))
        )))))




;----------------------------------------------------------------------

(defn bd-slot-value (self slot-name)
  (slot-value self slot-name))

(defn (setf bd-slot-value) (new-value self slot-name)
  (setf (slot-value self slot-name) new-value))

(defn bd-bound-slot-value (self slot-name caller-id)
  (declare (ignorable caller-id))
  (when (bd-slot-boundp self slot-name)
    (bd-slot-value self slot-name)))

(defn bd-slot-boundp (self slot-name)
  (slot-boundp self slot-name))

(defn bd-slot-makunbound (self slot-name)
  (if slot-name ;; not in def-c-variable
    (slot-makunbound self slot-name)
    (makunbound self)))

(comment sample incf
(defmethod c-value-incf ((base fpoint) delta)
  (declare (ignore model))
  (if delta
    (fp-add base delta)
    base))
)
