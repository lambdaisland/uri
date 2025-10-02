# lambdaisland/uri

<!-- badges -->
[![CircleCI](https://circleci.com/gh/lambdaisland/uri.svg?style=svg)](https://circleci.com/gh/lambdaisland/uri) [![cljdoc badge](https://cljdoc.org/badge/lambdaisland/uri)](https://cljdoc.org/d/lambdaisland/uri) [![Clojars Project](https://img.shields.io/clojars/v/lambdaisland/uri.svg)](https://clojars.org/lambdaisland/uri)
[![bb compatible](https://raw.githubusercontent.com/babashka/babashka/master/logo/badge.svg)](https://book.babashka.org#badges)
<!-- /badges -->

A pure Clojure/ClojureScript URI library.

Key features

- 100% cross-platform `.cljc`
- RFC compliant joining of URIs
- relative URIs as first class citizens

<!-- opencollective -->
## Lambda Island Open Source

Thank you! uri is made possible thanks to our generous backers. [Become a
backer on OpenCollective](https://opencollective.com/lambda-island) so that we
can continue to make uri better.

<a href="https://opencollective.com/lambda-island">
<img src="https://opencollective.com/lambda-island/organizations.svg?avatarHeight=46&width=800&button=false">
<img src="https://opencollective.com/lambda-island/individuals.svg?avatarHeight=46&width=800&button=false">
</a>
<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

uri is part of a growing collection of quality Clojure libraries created and maintained
by the fine folks at [Gaiwan](https://gaiwan.co).

Pay it forward by [becoming a backer on our OpenCollective](http://opencollective.com/lambda-island),
so that we continue to enjoy a thriving Clojure ecosystem.

You can find an overview of all our different projects at [lambdaisland/open-source](https://github.com/lambdaisland/open-source).

&nbsp;

&nbsp;
<!-- /opencollective -->

## Rationale

There are a number of Clojure libraries for working with URI/URLs (see
[Similar projects](#similar-projects) below). They all rely to some degree on
`java.net.URI` or `java.net.URL`. This lib provides a pure-Clojure/ClojureScript
alternative.

See the [announcement blog post](https://lambdaisland.com/blog/27-02-2017-announcing-lambdaisland-uri)

## Installation

To install, add the following dependency to your project or build file:

deps.edn:

``` clojure
lambdaisland/uri {:mvn/version "1.19.155"}
```

project.clj

``` clojure
[lambdaisland/uri "1.19.155"]
```

## Usage

``` clojure
(require '[lambdaisland.uri :refer [uri join]])


;; uri :: String -> lambdaisland.uri.URI
(uri "//example.com/foo/bar")
;;=> #lambdaisland/uri "//example.com/foo/bar"


;; A URI is a record, use assoc to update specific parts
;; Use `str` if you want the URI back as a string
(str
 (assoc (uri "//example.com/foo/bar")
        :scheme "https"
        :user "arne"
        :password "supersecret"
        :host "lambdaisland.com"
        :port "3333"
        :path "/hello/world"
        :query "q=5"
        :fragment "section1"))
;;=> "https://arne:supersecret@lambdaisland.com:3333/hello/world?q=5#section1"


;; RFC compliant joining of relative URIs
(join "//example.com/foo/bar" "./~arne/site/" "../foo.png")
;;=> #lambdaisland/uri "//example.com/foo/~arne/foo.png"


;; Arguments to `join` are coerced, you can pass strings, java.net.URI, or any x
;; for which `(str x)` returns a URI string.
(join (java.net.URI. "http://example.com/foo/bar")  (uri "./~arne/site/") "../foo.png")
;;=> #lambdaisland/uri "http://example.com/foo/~arne/foo.png"


;; URI implements IFn for keyword based lookup, so it's fully
;; interface-compatible with Clojure maps.
(:path (uri "http://example.com/foo/bar"))

;; Provide custom ordering for query-map
;; clj -Sdeps '{:deps {org.flatland/ordered {:mvn/version "1.5.7"}}}'
(require '[lambdaisland.uri :refer [query-map]]
         '[flatland.ordered.map :refer [ordered-map]])
(keys (query-map "http://example.com?a=1&b=2&c=3&d=4&e=5&f=6&g=7&h=8&i=9"
                 {:into (ordered-map)}))
=> (:a :b :c :d :e :f :g :h :i)

;; Instances of URI are printed with a #lambdaisland/uri reader tag. To read
;; them back from EDN, use the provided readers.
(require '[clojure.edn :as edn])

(edn/read-string
 {:readers lambdaisland.uri/edn-readers}
 "#lambdaisland/uri \"http://example.com/foo/~arne/foo.png\"")
```

[Full API docs are on Cljdoc](https://cljdoc.org/d/lambdaisland/uri)

## Babashka-specific caveats (also applies to SCI)

Instances of URI implement the `toString` method, so calling `(str uri)` gives
you the URI back as a string. They also implement the `IFn` interfaces so they
are callable the way maps are.

On babashka implementing interfaces or overriding Object methods is not
supported. As an alternative to `clojure.core/str` you can use
`lambdaisland.uri/uri-str`. As an alternative to using the URI as a function, use the keyword as a function, or use `clojure.core/get`

``` clojure
;; clojure / clojurescript
(str uri) ;; "https://example.com"
(uri :host) ;; "example.com"

;; bb
(str uri) ;; "{:scheme "https", :domain "example.com", :path ...}"
(uri :host) ;; nil

(uri/uri-str uri) ;; "https://example.com"
(:host uri) ;; "example.com"
(get uri :host) ;; "example.com"
```

## Similar projects

* [exploding-fish](https://github.com/wtetzner/exploding-fish)
  I was not aware at the time of creating lambdaisland/uri of exploding fish. It
  is the most mature pure-Clojure URI lib out there. It does not provide
  ClojureScript support.
* [cemerick/url](https://github.com/cemerick/url)
  Cross platform (cljx), Clojure version uses `java.net.URL`.
* [michaelklishin/urly](https://github.com/michaelklishin/urly)
  Based on `java.net.URI`.
* [codonnell/uri](https://github.com/codonnell/uri)
  Based on `java.net.URI`.

## Further reading

[RFC3986 Uniform Resource Identifier (URI): Generic Syntax](https://www.ietf.org/rfc/rfc3986.txt)

This library implements the algorithm specified in [Section 5.2](https://tools.ietf.org/html/rfc3986#section-5.2) of that RFC.

It has been tested against [this list of test cases compiled by the W3C](https://www.w3.org/2004/04/uri-rel-test.html).

<!-- contributing -->
## Contributing

We warmly welcome patches to uri. Please keep in mind the following:

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem 
- start by stating the problem, then supply a minimal solution `*`
- by contributing you agree to license your contributions as MPL 2.0
- don't break the contract with downstream consumers `**`
- don't break the tests

We would very much appreciate it if you also

- update the CHANGELOG and README
- add tests for new functionality

We recommend opening an issue first, before opening a pull request. That way we
can make sure we agree what the problem is, and discuss how best to solve it.
This is especially true if you add new dependencies, or significantly increase
the API surface. In cases like these we need to decide if these changes are in
line with the project's goals.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves first, only then move on to solving it.

`**` Projects that have a version that starts with `0.` may still see breaking changes, although we also consider the level of community adoption. The more widespread a project is, the less likely we're willing to introduce breakage. See [LambdaIsland-flavored Versioning](https://github.com/lambdaisland/open-source#lambdaisland-flavored-versioning) for more info.
<!-- /contributing -->

<!-- license-mpl -->
## License

Copyright &copy; 2017-2025 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
<!-- /license-mpl -->
