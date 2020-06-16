(ns lambdaisland.uri.platform) ;; clj

(defn string->byte-seq [^String s]
  (map #(if (< % 0) (+ % 256) %) (.getBytes s "UTF8")))

(defn byte-seq->string [arr]
  (String. (byte-array arr) "UTF8"))

(defn hex->byte [hex]
  (Integer/parseInt hex 16))


(defn byte->hex [byte]
  {:pre [(<= 0 byte 255)]}
  (format "%02X" byte))

(defn char-code-at [^String str pos]
  (long ^Character (.charAt str pos)))

(defn str-len [^String s]
  (.length s))
