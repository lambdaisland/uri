(ns lambdaisland.uri.platform ;; cljs
  (:require [goog.crypt :as c]))

(defn string->byte-seq [s]
  (c/stringToUtf8ByteArray s))

(defn byte-seq->string [arr]
  (c/utf8ByteArrayToString (apply array arr)))

(defn hex->byte [hex]
  (js/parseInt hex 16))

(def hex-digit {0 "0" 1 "1" 2 "2" 3 "3"
                4 "4" 5 "5" 6 "6" 7 "7"
                8 "8" 9 "9" 10 "A" 11 "B"
                12 "C" 13 "D" 14 "E" 15 "F"})

(defn byte->hex [byte]
  (let [byte (bit-and 0xFF byte)
        low-nibble (bit-and 0xF byte)
        high-nibble (bit-shift-right byte 4)]
    (str (hex-digit high-nibble) (hex-digit low-nibble))))

(defn char-code-at [str pos]
  (.charCodeAt str pos))

(defn str-len [s]
  (.-length s))
