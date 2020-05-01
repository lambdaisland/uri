(ns encode
  (:require [lambdaisland.uri :as uri]
            [lambdaisland.uri.normalize :as normalize]))

(-> (uri/uri "https://example.com/")
    (assoc :query (str "src=" (normalize/percent-encode "/foo/bar" :query)))
    (uri/assoc-query :frame-id "frame-1d3ba257-4c55-48a5-8011-5d1aba5c240f" :kind "iframe")
    str)
"https://example.com/?src=/foo/bar&kind=iframe&frame-id=frame-1d3ba257-4c55-48a5-8011-5d1aba5c240f"
"https://example.com/?src=/foo/bar&kind=iframe&frame-id=frame-1d3ba257-4c55-48a5-8011-5d1aba5c240f"

(-> (uri/uri "https://example.com/")
    (assoc :query (str "src=" (normalize/percent-encode "/foo/bar" :query)))
    str)
;;=> "https://example.com/?src=/foo/bar"

(str domain "/a/iframe-viewer?src=" (url/url-encode src)
     "&frame-id=" frame-id "&kind=" (name kind))

(-> (uri/uri domain)
    (assoc :path "/a/iframe-viewer")
    (assoc-query :src src :frame-id frame-id :kind (name kind))
    str)

(normalize/normalize (uri/uri "https://dev.nextjournalusercontent.com:8888/a/iframe-viewer?src=%2Fdata%2FQmfFEyfAfGq3siNJyVxMcddyxtHtRjFKqfm81Nw5wiML3V%3Fcontent-type%3Dapplication%252Fvnd.nextjournal.html%252Bhtml&frame-id=frame-1d3ba257-4c55-48a5-8011-5d1aba5c240f&kind=iframe"))

#lambdaisland/uri "https://dev.nextjournalusercontent.com:8888/a/iframe-viewer?src=/data/QmfFEyfAfGq3siNJyVxMcddyxtHtRjFKqfm81Nw5wiML3V?content-type=application%252Fvnd.nextjournal.html%252Bhtml&frame-id=frame-1d3ba257-4c55-48a5-8011-5d1aba5c240f&kind=iframe"



(-> (uri/uri "https://example.com/")
    (uri/assoc-query :xxx "?#")
    (uri/assoc-query :yyy "?#")
    (uri/assoc-query :zzz "?#&=")
    str)

"https://example.com/?src=/foo/bar&kind=iframe&frame-id=frame-1d3ba257-4c55-48a5-8011-5d1aba5c240f"

(-> (uri/uri "https://example.com/")
    (assoc :query (str "src=" (normalize/percent-encode "/foo/bar" :query)))
    str)
"https://example.com/?src=/foo/bar"
"https://example.com/?src=%2Ffoo%2Fbar"


(uri/query-map (uri/uri "?foo=%20%2B%26xxx%3D123"))
;;=> {:foo " +&xxx=123"}
(uri/query-map (normalize/normalize (uri/uri "?foo=%20%2B%26xxx%3D123")))
;;=> {:foo " +", :xxx "123"}

(-> (uri/uri "?foo=%20%2B%26xxx%3D123")
    normalize/normalize)
#lambdaisland/uri "?foo=%20+&xxx=123"


(normalize/percent-encode "+" :query)

;; => {:scheme "https"
;;     :user nil
;;     :password nil
;;     :host "example.com"
;;     :port nil
;;     :path "/"
;;     :query ""
;;     :fragment "hello"}

;; => {:scheme "https"
;;     :user nil
;;     :password nil
;;     :host "example.com"
;;     :port nil
;;     :path "/"
;;     :query "#hello"
;;     :fragment nil}

Parameter id must conform to #"^\d+$", but got "%2Fdata%2FQmfFEyfAfGq3siNJyVxMcddyxtHtRjFKqfm81Nw5wiML3V%3Fcontent-type%3Dapplication%2Fvnd.nextjournal.html%2Bhtml" .
Parameter id must conform to clojure.core/uuid? , but got "%2Fdata%2FQmfFEyfAfGq3siNJyVxMcddyxtHtRjFKqfm81Nw5wiML3V%3Fcontent-type%3Dapplication%2Fvnd.nextjournal.html%2Bhtml" .
Parameter id must conform to com.nextjournal.journal.params/matches-base58? , but got "%2Fdata%2FQmfFEyfAfGq3siNJyVxMcddyxtHtRjFKqfm81Nw5wiML3V%3Fcontent-type%3Dapplication%2Fvnd.nextjournal.html%2Bhtml" .
Parameter id must conform to com.nextjournal.journal.params/matches-uuid? , but got "%2Fdata%2FQmfFEyfAfGq3siNJyVxMcddyxtHtRjFKqfm81Nw5wiML3V%3Fcontent-type%3Dapplication%2Fvnd.nextjournal.html%2Bhtml" .

https://dev.nextjournalusercontent.com:8888/a/

(-> (uri/uri "https://example.com")
    (assoc :path "/a/iframe-viewer")
    (uri/assoc-query :frame-id 123
                     :kind "iframe"
                     :src "/data/QmfFEyfAfGq3siNJyVxMcddyxtHtRjFKqfm81Nw5wiML3V?content-type=application/vnd.nextjournal.html+html")
    )
#lambdaisland/uri "https://example.com/a/iframe-viewer?src=/data/QmfFEyfAfGq3siNJyVxMcddyxtHtRjFKqfm81Nw5wiML3V%3Fcontent-type%3Dapplication/vnd.nextjournal.html+html&kind=iframe&frame-id=123"
