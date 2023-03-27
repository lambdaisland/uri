(ns lambdaisland.uri-test
  (:require [clojure.test :as t :refer [are deftest is testing]]
            [lambdaisland.uri :as uri]
            [lambdaisland.uri.normalize :as norm]
            [lambdaisland.uri.platform :as platform]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :as tc]
            [clojure.string :as str])
  #?(:clj (:import lambdaisland.uri.URI)))

(deftest parsing
  (testing "happy path"
    (are [x y] (= y (uri/parse x))
      "http://user:password@example.com:8080/path?query=value#fragment"
      (uri/URI. "http" "user" "password" "example.com" "8080" "/path" "query=value" "fragment")

      "/happy/path"
      (uri/URI. nil nil nil nil nil "/happy/path" nil nil)

      "relative/path"
      (uri/URI. nil nil nil nil nil "relative/path" nil nil)

      "http://example.com"
      (uri/URI. "http" nil nil "example.com" nil nil nil nil)
      )))

(deftest joining
  (are [x y] (= (uri/parse y) (apply uri/join (map uri/parse x)))
    ["http://foo.bar"              "https://baz.com"]   "https://baz.com"
    ["http://example.com"          "/a/path"]           "http://example.com/a/path"
    ["http://example.com/foo/bar"  "/a/path"]           "http://example.com/a/path"
    ["http://example.com/foo/bar"  "a/relative/path"]   "http://example.com/foo/a/relative/path"
    ["http://example.com/foo/bar/" "a/relative/path"]   "http://example.com/foo/bar/a/relative/path"
    ["/foo/bar/"                   "a/relative/path"]   "/foo/bar/a/relative/path"
    ["http://example.com"          "a/relative/path"]   "http://example.com/a/relative/path"
    ["http://example.com/a/b/c/d/" "../../x/y"]         "http://example.com/a/b/x/y")

  (testing "https://www.w3.org/2004/04/uri-rel-test.html"
    (are [x y] (= y (str (uri/join (uri/parse "http://a/b/c/d;p?q") (uri/parse x))))
      "g" "http://a/b/c/g"
      "./g" "http://a/b/c/g"
      "g/" "http://a/b/c/g/"
      "/g" "http://a/g"
      "//g" "http://g"
      "?y" "http://a/b/c/d;p?y"
      "g?y" "http://a/b/c/g?y"
      "#s" "http://a/b/c/d;p?q#s"
      "g#s" "http://a/b/c/g#s"
      "g?y#s" "http://a/b/c/g?y#s"
      ";x" "http://a/b/c/;x"
      "g;x" "http://a/b/c/g;x"
      "g;x?y#s" "http://a/b/c/g;x?y#s"
      "" "http://a/b/c/d;p?q"
      "." "http://a/b/c/"
      "./" "http://a/b/c/"
      ".." "http://a/b/"
      "../" "http://a/b/"
      "../g" "http://a/b/g"
      "../.." "http://a/"
      "../../" "http://a/"
      "../../g" "http://a/g"
      "../../../g" "http://a/g"
      "../../../../g" "http://a/g"
      "/./g" "http://a/g"
      "/g" "http://a/g"
      "g." "http://a/b/c/g."
      ".g" "http://a/b/c/.g"
      "g.." "http://a/b/c/g.."
      "..g" "http://a/b/c/..g"
      "./../g" "http://a/b/g"
      "./g/" "http://a/b/c/g/"
      "g/h" "http://a/b/c/g/h"
      "h" "http://a/b/c/h"
      "g;x=1/./y" "http://a/b/c/g;x=1/y"
      "g;x=1/../y" "http://a/b/c/y"
      "g?y/./x" "http://a/b/c/g?y/./x"
      "g?y/../x" "http://a/b/c/g?y/../x"
      "g#s/./x" "http://a/b/c/g#s/./x"
      "g#s/../x" "http://a/b/c/g#s/../x"
      "http:g" "http:g"))

  (testing "coerces its arguments"
    (is (= (uri/join "http://x/y/z" "/a/b/c") (uri/parse "http://x/a/b/c")))
    #?(:clj
       (is (= (uri/join (java.net.URI. "http://x/y/z") "/a/b/c") (uri/parse "http://x/a/b/c"))))))

(deftest lambdaisland-uri-URI
  (let [example "http://usr:pwd@example.com:8080/path?query=value#fragment"
        parsed (uri/uri example)]
    (testing "it allows keyword based access"
      (is (= (:scheme parsed) "http"))
      (is (= (:user parsed) "usr"))
      (is (= (:password parsed) "pwd"))
      (is (= (:host parsed) "example.com"))
      (is (= (:port parsed) "8080"))
      (is (= (:path parsed) "/path"))
      (is (= (:query parsed) "query=value"))
      (is (= (:fragment parsed) "fragment")))
    #?(:bb nil
       :default
       (testing "it allows map-style access"
         (is (= (parsed :scheme) "http"))
         (is (= (parsed :user) "usr"))
         (is (= (parsed :password) "pwd"))
         (is (= (parsed :host) "example.com"))
         (is (= (parsed :port) "8080"))
         (is (= (parsed :path) "/path"))
         (is (= (parsed :query) "query=value"))
         (is (= (parsed :fragment) "fragment"))))
    (testing "it converts correctly to string"
      (is (= (str parsed) example)))))

(deftest lambdaisland-uri-relative?
  (are [x] (uri/relative? (uri/parse x))
    "//example.com"
    "/some/path"
    "?only=a-query"
    "#only-a-fragment"
    "//example.com:8080/foo/bar?baz#baq")
  (are [x] (uri/absolute? (uri/parse x))
    "http://example.com"
    "https://example.com:8080/foo/bar?baz#baq"))

(deftest query-map-test
  (is (= {:foo "bar", :aaa "bbb"}
         (uri/query-map "http://example.com?foo=bar&aaa=bbb")))

  (is (= {"foo" "bar", "aaa" "bbb"}
         (uri/query-map "http://example.com?foo=bar&aaa=bbb" {:keywordize? false})))

  (is (= {:id ["1" "2"]}
         (uri/query-map "?id=1&id=2")))

  (is (= {:id "2"}
         (uri/query-map "?id=1&id=2" {:multikeys :never})))

  (is (= {:foo ["bar"], :id ["2"]}
         (uri/query-map "?foo=bar&id=2" {:multikeys :always})))

  (is (= {:foo " +&xxx=123"}
         (uri/query-map "?foo=%20%2B%26xxx%3D123"))))

(deftest assoc-query-test
  (is (= (uri/uri "http://example.com?foo=baq&aaa=bbb&hello=world")
         (uri/assoc-query "http://example.com?foo=bar&aaa=bbb"
                          :foo "baq"
                          :hello "world")))

  (is (= (uri/uri "http://example.com?foo=baq&aaa=bbb&hello=world")
         (uri/assoc-query* "http://example.com?foo=bar&aaa=bbb"
                           {:foo "baq"
                            :hello "world"})))

  (is (= (uri/uri "?id=1&id=2")
         (uri/assoc-query* "" (uri/query-map "?id=1&id=2"))))

  (is (= (uri/uri "?id=1")
         (uri/assoc-query "?id=1&name=jack" :name nil)))

  (is (= (uri/uri "?foo=+%2B%26%3D")
         (uri/assoc-query "" :foo " +&=")))

  (is (= "a=a+b&b=b+c"
         (-> "/foo"
             (uri/assoc-query* {:a "a b"})
             (uri/assoc-query* {:b "b c"})
             :query)))

  (is (= {:a "a b"}
         (-> "/foo"
             (uri/assoc-query* {:a "a b"})
             uri/query-map))))

(deftest uri-predicate-test
  (is (true? (uri/uri? (uri/uri "/foo")))))

(def query-map-gen
  (gen/map (gen/such-that #(not= ":/" (str %)) gen/keyword)
           gen/string))

(tc/defspec query-string-round-trips 100
  (prop/for-all [q query-map-gen]
                (let [res (-> q
                              uri/map->query-string
                              uri/query-string->map)]
                  (or (and (empty? q) (empty? res)) ;; (= nil {})
                      (= q res)))))

(deftest backslash-in-authority-test
  ;; A backslash is not technically a valid character in a URI (see RFC 3986
  ;; section 2), and so should always be percent encoded. The problem is that
  ;; user-facing software (e.g. browsers) rarely if ever rejects invalid
  ;; URIs/URLs, leading to ad-hoc rules about how to map the set of invalid URIs
  ;; to valid URIs. All modern browsers now interpret a backslash as a forward
  ;; slash, which changes the interpretation of the URI. For this test (and
  ;; accompanying patch) we only care about the specific case of a backslash
  ;; appearing inside the authority section, since this authority or _origin_ is
  ;; regularly used to inform security policies, e.g. to check if code served
  ;; from a certain origin has access to resources with the same origin. In this
  ;; case we partially mimic what browsers do, by treating the backslash as a
  ;; delimiter which starts the path section, even though we don't replace it
  ;; with a forward slash, but leave it as-is in the parsed result.
  (let [{:keys [host path user]}
        (uri/uri "https://example.com\\@gaiwan.co")]
    (is (= "example.com" host))
    (is (= nil user))
    (is (= "\\@gaiwan.co" path))))
