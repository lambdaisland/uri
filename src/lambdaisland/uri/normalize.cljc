(ns lambdaisland.uri.normalize
  (:require [clojure.string :as str]))

(def
  ^{:dynamic true
    :doc "The encoding used by `percent-encode` and `percent-decode`.
          Unfortunately the RFC does not specify what this should be beyond
          \"generally ... UTF-8 (or some other superset of the US-ASCII
          character encoding)\""}
  *character-encoding* "UTF8")

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

(defn percent-encode
  "Convert characters in their percent encoded form. e.g.
   `(percent_encode \"a\") #_=> \"%61\"`. When given a second argument, then
   only characters of the given character class are encoded,
   see `character-class`.

   Characters are encoded as UTF-8. To use a different encoding. re-bind
   `*character-encoding*`"
  ([component]
   (->> (.getBytes component *character-encoding*)
        (map (comp str/upper-case (partial format "%%%x")))
        (apply str)))
  ([component type]
   (str/replace component (get character-classes type) percent-encode)))

(defn percent-decode
  "The inverse of `percent-encode`, convert any %XX sequences in a string to
   characters. Byte sequences are interpreted as UTF-8. To use a different
   encoding. re-bind `*character-encoding*`"
  [s]
  (str/replace s #"(%[0-9A-Fa-f]{2})+"
               (fn [[x _]]
                 (String.
                  (->> (str/split x #"%")
                       (drop 1)
                       (map #(Integer/parseInt % 16))
                       byte-array)
                  *character-encoding*))))

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
