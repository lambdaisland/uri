(ns lambdaisland.uri
  (:require [clojure.string :as str])
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
  already. Supports String, java.net.URI."
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
   (defmethod print-method URI [this writer]
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
