(ns lambdaisland.uri.platform)

(defn string->byte-seq [s]
  (.getBytes s "UTF8"))

(defn byte-seq->string [arr]
  (String. (byte-array arr) "UTF8"))

(defn hex->byte [hex]
  (Integer/parseInt hex 16))

(defn byte->hex [byte]
  (format "%X" byte))

(defn char-code-at [str pos]
  (long (.charAt str pos)))
