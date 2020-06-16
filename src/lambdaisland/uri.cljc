(ns lambdaisland.uri
  (:require [clojure.string :as str]
            [lambdaisland.uri.normalize :as normalize])
  #?(:clj (:import clojure.lang.IFn)))


(def uri-regex #?(:clj #"\A(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)?(\?([^#]*))?(#(.*))?\z"
                  :cljs #"^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)?(\?([^#]*))?(#(.*))?$"))
(def authority-regex #?(:clj #"\A(([^:]*)(:(.*))?@)?([^:]*)(:(\d*))?\z"
                        :cljs #"^(([^:]*)(:(.*))?@)?([^:]*)(:(\d*))?$"))

(defrecord URI [scheme user password host port path query fragment]
  IFn
  (#?(:clj invoke :cljs -invoke) [this kw]
    (get this kw))
  Object
  (toString [this]
    (let [authority-string (fn [user password host port]
                             (when host
                               (cond-> user
                                 (and user password) (str ":" password)
                                 user                (str "@")
                                 true                (str host)
                                 port                (str ":" port))))
          authority (authority-string user password host port)]
      (cond-> ""
        scheme    (str scheme ":")
        authority (str "//" authority)
        true      (str path)
        query     (str "?" query)
        fragment  (str "#" fragment)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; parse

(defn- match-uri [uri]
  (let [matches (re-matches uri-regex uri)
        [_ _ scheme _ authority path _ query _ fragment] matches]
    [scheme authority (when (seq path) path) query fragment]))

(defn- match-authority [authority]
  (let [matches (re-matches authority-regex authority)
        [_ _ user _ password host _ port] matches]
    [user password host port]))

(defn parse
  "Parse a URI string into a lambadisland.uri.URI record."
  [uri]
  (let [[scheme authority path query fragment] (match-uri uri)]
    (if authority
      (let [[user password host port] (match-authority authority)]
        (URI. scheme user password host port path query fragment))
      (URI. scheme nil nil nil nil path query fragment))))

(defn uri
  "Turn the given value into a lambdaisland.uri.URI record, if it isn't one
  already. Supports String, java.net.URI, and other URI-like objects that return
  a valid URI string with `str`."
  [uri-like]
  (if (instance? URI uri-like)
    uri-like
    (parse (str uri-like))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; join / resolve

;; This section is based on RFC 3986

(defn- absolute-path? [path]
  (= (first path) \/))

(defn- remove-dot-segments
  "As per RFC 3986 section 5.2.4"
  [path]
  (when path
    (loop [in (str/split path #"(?=/)")
           out []]
      (case (first in)
        "/." (if (next in)
               (recur (next in) out)
               (recur nil (conj out "/")))
        "/.." (if (next in)
                (recur (next in) (vec (butlast out)))
                (recur nil (conj (vec (butlast out)) "/")))
        nil (str/join out)
        (recur (next in) (conj out (first in)))))))

(defn- merge-paths [a b]
  (if (some #{\/} a)
    (str (re-find #?(:clj #"\A.*/"
                     :cljs #"^.*/") a) b)
    (if (absolute-path? b)
      b
      (str "/" b))))

(defn join*
  "Join two URI records as per RFC 3986. Handles relative URIs."
  [base ref]
  (if (:scheme ref)
    (update ref :path remove-dot-segments)
    (-> (if (:host ref)
          (assoc ref
                 :scheme (:scheme base)
                 :query  (:query ref))
          (if (nil? (:path ref))
            (assoc base :query (some :query [ref base]))
            (assoc base :path
                   (remove-dot-segments
                    (if (absolute-path? (:path ref))
                      (:path ref)
                      (merge-paths (:path base) (:path ref))))
                   :query (:query ref))))
        (assoc :fragment (:fragment ref)))))

(defn join
  "Joins any number of URIs as per RFC3986. Arguments can be strings, they will
  be coerced to URI records."
  [& uris]
  (reduce join* (map uri uris)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query strings

(defn- decode-param-pair [param]
  (let [[k v] (str/split param #"=")]
    [(if k (normalize/percent-decode k) "")
     (if v (normalize/percent-decode (str/replace v #"\+" " ")) "")]))

(defn query-string->map
  "Parse a query string, consisting of key=value pairs, separated by \"&\". Takes
  the following options:

  - `:keywordize?` whether to turn return keys as keywords. Defaults to `true`.
  - `:multikeys` how to handle the same key occuring multiple times, defaults to
    `:duplicates`

  The possible values for `:multikeys` are

  - `:never` always return a single value for a key. The rightmost value
    \"wins\"
  - `:always` return a map with vectors as values, with successive
    values of the same key in order
  - `:duplicates` return a vector for keys that occur multiple times, or a
    string otherwise"
  ([q]
   (query-string->map q nil))
  ([q {:keys [multikeys keywordize?]
       :or {multikeys :duplicates
            keywordize? true}}]
   (when (not (str/blank? q))
     (->> (str/split q #"&")
          (map decode-param-pair)
          (reduce
           (fn [m [k v]]
             (let [k (if keywordize? (keyword k) k)]
               (case multikeys
                 :never
                 (assoc m k v)
                 :always
                 (if (contains? m k)
                   (update m k conj v)
                   (assoc m k [v]))
                 :duplicates
                 (if (contains? m k)
                   (if (vector? (m k))
                     (update m k conj v)
                     (assoc m k [(m k) v]))
                   (assoc m k v)))))
           {})))))

(defn query-map
  "Return the query section of a URI as a map. Will coerce its argument
  with [[uri]]. Takes an options map, see [[query-string->map]] for options."
  ([uri]
   (query-map uri nil))
  ([u opts]
   (query-string->map (:query (uri u)) opts)))

(defn query-encode
  "Percent encoding for query strings. Will percent-encode values that are
  reserved in query strings only. Encodes spaces as +."
  [s]
  (let [encode-char #(cond
                       (= " " %)
                       "+"
                       (re-find #"[^a-zA-Z0-9\-\._~@\/]" %)
                       (normalize/percent-encode %)
                       :else
                       %)]
    (->> (normalize/char-seq s)
         (map encode-char)
         (apply str))))

(defn- encode-param-pair [k v]
  (str (query-encode
        (cond
          (simple-ident? k)
          (name k)
          (qualified-ident? k)
          (str (namespace k) "/" (name k))
          :else (str k)))
       "="
       (query-encode (str v))))

(defn map->query-string
  "Convert a map into a query string, consisting of key=value pairs separated by
  `&`. The result is percent-encoded so it is always safe to use. Keys can be
  strings or keywords. If values are collections then this results in multiple
  entries for the same key. `nil` values are ignored. Values are stringified."
  [m]
  (when (seq m)
    (->> m
         (mapcat (fn [[k v]]
                   (cond
                     (nil? v)
                     []
                     (coll? v)
                     (map (partial encode-param-pair k) v)
                     :else
                     [(encode-param-pair k v)])))
         (interpose "&")
         (apply str))))

(defn assoc-query*
  "Add additional query parameters to a URI. Takes a URI (or coercible to URI) and
  a map of query params."
  [u m]
  (let [u (uri u)]
    (assoc u :query (map->query-string (merge (query-map u) m)))))

(defn assoc-query
  "Add additional query parameters to a URI. Takes a URI (or coercible to URI)
  followed key value pairs.

  (assoc-query \"http://example.com?id=1&name=John\" :name \"Jack\" :style \"goth\")
  ;;=> #lambdaisland/uri \"http://example.com?id=1&name=Jack&style=goth\" "
  [u & {:as kvs}]
  (assoc-query* u kvs))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Predicates

(defn relative?
  "Is the URI relative? Returns true if the URI does not have a scheme (protocol)."
  [uri]
  (nil? (:scheme uri)))

(def
  ^{:doc
    "Is the URI absolute? Returns true if the URI has a scheme (protocol), and hence also an origin."}
  absolute? (complement relative?))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; EDN

(def edn-tag 'lambdaisland/uri)

#?(:clj
   (defmethod print-method URI [^URI this ^java.io.Writer writer]
     (.write writer "#")
     (.write writer (str edn-tag))
     (.write writer " ")
     (.write writer (prn-str (.toString this))))

   :cljs
   (extend-type URI
     IPrintWithWriter
     (-pr-writer [this writer _opts]
       (write-all writer "#" (str edn-tag) " " (prn-str (.toString this))))))

(def
  ^{:doc
    "A map that can be passed to clojure.edn/read, so tagged URI literals are
     read back correctly."}
  edn-readers {edn-tag parse})
