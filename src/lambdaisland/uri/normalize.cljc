(ns lambdaisland.uri.normalize
  (:require [clojure.string :as str]
            [lambdaisland.uri.platform :refer [byte-seq->string
                                               string->byte-seq
                                               byte->hex hex->byte
                                               char-code-at]]))

;; TODO we might be better off having these just be sets
(def
  ^{:doc
    "Which characters should be percent-encoded depends on which section
    of the URI is being normalized. This map contains regexes that for each
    case match the characters that need encoding."}
  character-classes
  (let [alpha      "a-zA-Z"
        digit      "0-9"
        gen-delims ":\\/\\?#\\[\\]@"
        sub-delims "!\\$&'\\(\\)\\*\\+,;="
        reserved   (str gen-delims sub-delims)
        unreserved (str alpha digit "\\-\\._~")
        pchar      (str unreserved sub-delims ":@")
        scheme     (str alpha digit "\\-\\+\\.")
        host       (str unreserved sub-delims "\\[:\\]")
        authority  pchar
        path       (str pchar "\\/")
        query      (str pchar "\\/\\?")
        fragment   (str pchar "\\/\\?")]
    {:alpha      (re-pattern (str "[^" alpha "]"))
     :digit      (re-pattern (str "[^" digit "]"))
     :gen-delims (re-pattern (str "[^" gen-delims "]"))
     :sub-delims (re-pattern (str "[^" sub-delims "]"))
     :reserved   (re-pattern (str "[^" reserved "]"))
     :unreserved (re-pattern (str "[^" unreserved "]"))
     :pchar      (re-pattern (str "[^" pchar "]"))
     :scheme     (re-pattern (str "[^" scheme "]"))
     :host       (re-pattern (str "[^" host "]"))
     :authority  (re-pattern (str "[^" authority "]"))
     :path       (re-pattern (str "[^" path "]"))
     :query      (re-pattern (str "[^" query "]"))
     :fragment   (re-pattern (str "[^" fragment "]"))}))

(defn char-seq
  "Return a seq of the characters in a string, making sure not to split up
  UCS-2 (or is it UTF-16?) surrogate pairs. Because JavaScript. And Java."
  ([str]
   (char-seq str 0))
  ([str offset]
   (if (>= offset (count str))
     ()
     (let [code (char-code-at str offset)
           width (if (<= 0xD800 code 0xDBFF) 2 1)] ; "high surrogate"
       (cons (subs str offset (+ offset width))
             (char-seq str (+ offset width)))))))

(defn percent-encode
  "Convert characters in their percent encoded form. e.g.
   `(percent_encode \"a\") #_=> \"%61\"`. When given a second argument, then
   only characters of the given character class are encoded,
   see `character-class`.

   Characters are encoded as UTF-8. To use a different encoding. re-bind
   `*character-encoding*`"
  ([component]
   (->> (string->byte-seq component)
        (map #(str "%" (byte->hex %)))
        (apply str)))
  ([component type]
   (let [char-class (get character-classes type)
         encode-char #(cond-> % (re-find char-class %) percent-encode)]
     (->> (char-seq component)
          (map encode-char)
          (apply str)))))

(defn percent-decode
  "The inverse of `percent-encode`, convert any %XX sequences in a string to
   characters. Byte sequences are interpreted as UTF-8. To use a different
   encoding. re-bind `*character-encoding*`"
  [s]
  (str/replace s #"(%[0-9A-Fa-f]{2})+"
               (fn [[x _]]
                 (byte-seq->string
                  (->> (str/split x #"%")
                       (drop 1)
                       (map hex->byte))))))

(defn normalize-path [path]
  (when-not (nil? path)
    (percent-encode (percent-decode path) :path)))

(defn normalize-query [query]
  (when-not (nil? query)
    (percent-encode (percent-decode query) :query)))

(defn normalize
  "Normalize a lambdaisland.uri.URI."
  [uri]
  (-> uri
      (update :path normalize-path)
      (update :query normalize-query)))
