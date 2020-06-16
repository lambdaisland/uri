(ns lambdaisland.uri.normalize
  (:require [clojure.string :as str]
            [lambdaisland.uri.platform :refer [byte-seq->string
                                               string->byte-seq
                                               byte->hex hex->byte
                                               char-code-at
                                               str-len]]))

;; TODO we might be better off having these just be sets
(def
  ^{:doc
    "Which characters should be percent-encoded depends on which section
    of the URI is being normalized. This map contains regexes that for each
    case match the characters that need encoding."}
  character-classes
  (let [alpha       "a-zA-Z"
        digit       "0-9"
        gen-delims  ":\\/\\?#\\[\\]@"
        sub-delims  "!\\$&'\\(\\)\\*\\+,;="
        reserved    (str gen-delims sub-delims)
        unreserved  (str alpha digit "\\-\\._~")
        pchar       (str unreserved sub-delims ":@")
        scheme      (str alpha digit "\\-\\+\\.")
        host        (str unreserved sub-delims "\\[:\\]")
        authority   pchar
        path        (str pchar "\\/")
        query       (str unreserved ":@\\/\\?")
        fragment    (str pchar "\\/\\?")]
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
   (if (>= offset (str-len str))
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
  (when s
    (str/replace s #"(%[0-9A-Fa-f]{2})+"
                 (fn [[x _]]
                   (byte-seq->string
                    (->> (str/split x #"%")
                         (drop 1)
                         (map hex->byte)))))))

(defn normalize-path [path]
  (when-not (nil? path)
    (percent-encode (percent-decode path) :path)))

(defn hex-code-point? [cp]
  (or (<= #_(long \0) 48 cp #_(long \9) 57)
      (<= #_(long \A) 65 cp #_(long \F) 70)
      (<= #_(long \a) 97 cp #_(long \f) 102)))

(def sub-delims
  "RFC3986 section 2.2

  The purpose of reserved characters is to provide a set of delimiting
  characters that are distinguishable from other data within a URI.
  URIs that differ in the replacement of a reserved character with its
  corresponding percent-encoded octet are not equivalent.  Percent-
  encoding a reserved character, or decoding a percent-encoded octet
  that corresponds to a reserved character, will change how the URI is
  interpreted by most applications.  Thus, characters in the reserved
  set are protected from normalization and are therefore safe to be
  used by scheme-specific and producer-specific algorithms for
  delimiting data subcomponents within a URI. "
  #{"!"  "$"  "&"  "'"  "("  ")" "*"  "+"  ","  ";"  "="})

(defn normalize-query
  "Normalize the query section of a URI

  - sub-delimiters that are not percent encoded are left unencoded
  - sub-delimiters and other reserved characters are always percent encoded
  - non-reserved characters that are percent-encoded are decoded
  "
  [s]
  (when s
    (let [len (str-len s)]
      (loop [i 0
             res []]
        (cond
          (= i len)
          (apply str res)

          (and (< i (- len 2))
               (= 37 (char-code-at s i))
               (hex-code-point? (char-code-at s (inc i)))
               (hex-code-point? (char-code-at s (+ i 2))))
          (recur (+ i 3)
                 (conj res (percent-encode (percent-decode (subs s i (+ i 3)))
                                           :query)))

          (contains? sub-delims (subs s i (inc i)))
          (recur (inc i)
                 (conj res (subs s i (inc i))))

          :else
          (recur (inc i)
                 (conj res (percent-encode (subs s i (inc i)) :query))))))))

(defn normalize
  "Normalize a lambdaisland.uri.URI."
  [uri]
  (-> uri
      (update :path normalize-path)
      (update :query normalize-query)))
