(ns repl-sessions.walkthrough
  "Walkthrough of the main features of lambdaisland.uri"
  ;; The public API is spread across these two namespaces
  (:require [lambdaisland.uri :as uri]
            [lambdaisland.uri.normalize :as normalize]))

;; tl;dr : CLJC (Clojure+ClojureScript) library for parsing, creating,
;; manipulating URIs, with an API that leans into simple `clojure.core`
;; functions.

;; The `uri/uri` constructor returns a `lambdaisland.uri.URI`, which is a
;; Clojure record

(def u (uri/uri "https://lambdaisland.com/episodes/all"))

(type u)
(record? u)

;; Since it's a record, it acts like a map, so you can access and manipulate the
;; individual parts of the URL using plain Clojure map functions, like `get`,
;; `assoc`, etc.

;; Use `into` to convert to a plain map, so you can see what's inside
(into {} (uri/uri "https://lambdaisland.com/episodes/all"))
(into {} (uri/uri "https://arne:secret@lambdaisland.com:123/episodes/all?hello#boo"))
;; => {:scheme "https",
;;     :user nil,
;;     :password nil,
;;     :host "lambdaisland.com",
;;     :port nil,
;;     :path "/episodes/all",
;;     :query nil,
;;     :fragment nil}

;; This provides for a very natural API, just use clojure.core
(:host u)
(:path u)
(assoc u :fragment "hello")
(assoc u :path "")

;; lambdaisland.uri.URI implements Object#toString, so converting it to a String
;; reconstitutes the URI
(str u)
(-> u
    (assoc :path "/about")
    (assoc :query "page=1")
    str)

;; Calls to `uri/uri` are idempotent
(uri/uri u)

;; We coerce any argument to string first, which means many other
;; representations "just work" when passed to `uri/uri`
(uri/uri (java.net.URI. "http://example.com"))

;; We implement the RFC3986 algorithm for joining (resolving) URI based on a
;; base URI. E.g. this is what a browser does when you follow a relative link.
(def base "http://example.com/api/v1/")

(uri/join base "hello/world")
(uri/join base "/hello/world")
(uri/join base "./hello/../../world")

;; `uri/uri` considers the "query" part of a URL as one thing, because really
;; you can shove anything in there

(:query (uri/uri "http://example.com/?xyz123"))

;; But it's common to treat it as key-value pairs, we have helpers to do just
;; that.
(uri/query-map "http://example.com/?foo=bar&hello=world")

;; Note that these automatically coerce their first argument if necessary
(-> "http://example.com/?foo=bar&hello=world"
    (uri/assoc-query :foo 123))

(-> "http://example.com/?foo=bar&hello=world"
    (uri/assoc-query :foo [123 456]))

(-> "http://example.com/?foo=bar&hello=world"
    (uri/assoc-query :foo "foo bar"))

;; This probably should've been called `merge-query`, oh well.
(-> "http://example.com/?foo=bar&hello=world"
    (uri/assoc-query* {:foo "foo bar"}))

;; When "parsing" a URI we don't do any percent decoding, we assume that your
;; URI is valid, i.e. that any invalid characters are percent-encoded
(uri/uri "http://example.com/%61%62%63")

;; `normalize` will encode what needs encoding, and decode what needs decoding.
;; This correctly handles "surrogate pairs" in Java/JavaScript
;; strings (characters that have a codepoint that doesn't fit in 16 bits).
(normalize/normalize (uri/uri "http://example.com/🙈🙉"))
(normalize/normalize (uri/uri "http://example.com/%61%62%63"))

;; Note that percent-encoding is dependent on the part of the URI you're dealing
;; with.
(normalize/normalize (uri/uri "http://example.com/foo = bar"))
(normalize/normalize (uri/assoc-query "http://example.com/" :foo " =bar= "))

;; And that sometimes there's a semantic difference between percent and not
;; percent encoded

;; In a Java context you sometimes find "nested" URIs, with pseudo-schemes like
;; `jdbc` or `jar`, here you might have to re-parse the `:path` of the outer URI.
(:scheme
 (uri/uri
  (:path
   (uri/uri "jdbc:postgresql://localhost:5432/metabase?user=metabase&password=metabase"))))

(str
 (assoc
   (uri/uri
    (:path
     (uri/uri "jdbc:postgresql://localhost:5432/metabase?user=metabase&password=metabase")))
   :user "user"
   :password "pwd"))
