(ns repl-sessions.walkthrough
  (:require [lambdaisland.uri :as uri]
            [lambdaisland.uri.normalize :as normalize]))

(def u (uri/uri "https://lambdaisland.com/episodes/all"))

(type u)
(record? u)

(str u)

(into {} (uri/uri "https://lambdaisland.com/episodes/all"))
;; => {:scheme "https",
;;     :user nil,
;;     :password nil,
;;     :host "lambdaisland.com",
;;     :port nil,
;;     :path "/episodes/all",
;;     :query nil,
;;     :fragment nil}

(:host u)
(:path u)
(assoc u :fragment "hello")
(assoc u :path "")

(str u)

(uri/uri u)
(uri/uri (java.net.URI. "http://example.com"))

(def base "http://example.com/api/v1/")

(uri/join base "hello/world")
(uri/join base "/hello/world")
(uri/join base "./hello/../../world")

(uri/query-map "http://example.com/?foo=bar&hello=world")

(-> "http://example.com/?foo=bar&hello=world"
    (uri/assoc-query :foo 123))

(-> "http://example.com/?foo=bar&hello=world"
    (uri/assoc-query :foo [123 456]))

(-> "http://example.com/?foo=bar&hello=world"
    (uri/assoc-query :foo "foo bar"))

(normalize/normalize (uri/uri "http://example.com/🙈🙉"))
(normalize/normalize (uri/uri "http://example.com/%61%62%63"))
