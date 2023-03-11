(ns repl-sessions.poke
  (:require [lambdaisland.uri :as uri]
            [lambdaisland.uri.normalize :as norm]))

(:fragment (norm/normalize (uri/map->URI {:fragment "'#'schön"})))
"'%23'sch%C3%B6n"

(norm/normalize-fragment "schön")
;; => "sch%C3%B6n"


(uri/map->query-string {:foo "bar"});; => "foo=bar"
(uri/map->query-string {:foo/baz "bar"});; => "foo/baz=bar"
(uri/query-string->map "foo=bar");; => {:foo "bar"}
(uri/query-string->map "%3Afoo=bar" {:keywordize? false});; => {":foo" "bar"}

;; => #::foo{:baz "bar"}

;; => #:foo{:baz "bar"}

(let [{:keys [scheme host port]} (uri/uri "https://foo.bar:8080")]
  )

(:query (uri/uri "http://example.com?hello"))

(uri/map->query-string {:foo "", :bar nil})
;; => "foo="
