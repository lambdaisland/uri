# lambdaisland/uri

<!-- badges -->
[![CircleCI](https://circleci.com/gh/lambdaisland/uri.svg?style=svg)](https://circleci.com/gh/lambdaisland/uri) [![cljdoc badge](https://cljdoc.org/badge/lambdaisland/uri)](https://cljdoc.org/d/lambdaisland/uri) [![Clojars Project](https://img.shields.io/clojars/v/lambdaisland/uri.svg)](https://clojars.org/lambdaisland/uri)
[![bb compatible](https://raw.githubusercontent.com/babashka/babashka/master/logo/badge.svg)](https://babashka.org)
<!-- /badges -->

A pure Clojure/ClojureScript URI library.

Key features

- 100% cross-platform `.cljc`
- RFC compliant joining of URIs
- relative URIs as first class citizens

<!-- opencollective -->
## Lambda Island Open Source

<img align="left" src="https://github.com/lambdaisland/open-source/raw/master/artwork/lighthouse_readme.png">

&nbsp;

uri is part of a growing collection of quality Clojure libraries created and maintained
by the fine folks at [Gaiwan](https://gaiwan.co).

Pay it forward by [becoming a backer on our Open Collective](http://opencollective.com/lambda-island),
so that we may continue to enjoy a thriving Clojure ecosystem.

You can find an overview of our projects at [lambdaisland/open-source](https://github.com/lambdaisland/open-source).

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
lambdaisland/uri {:mvn/version "1.14.120"}
```

project.clj

``` clojure
[lambdaisland/uri "1.14.120"]
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

Everyone has a right to submit patches to uri, and thus become a contributor.

Contributors MUST

- adhere to the [LambdaIsland Clojure Style Guide](https://nextjournal.com/lambdaisland/clojure-style-guide)
- write patches that solve a problem. Start by stating the problem, then supply a minimal solution. `*`
- agree to license their contributions as MPL 2.0.
- not break the contract with downstream consumers. `**`
- not break the tests.

Contributors SHOULD

- update the CHANGELOG and README.
- add tests for new functionality.

If you submit a pull request that adheres to these rules, then it will almost
certainly be merged immediately. However some things may require more
consideration. If you add new dependencies, or significantly increase the API
surface, then we need to decide if these changes are in line with the project's
goals. In this case you can start by [writing a pitch](https://nextjournal.com/lambdaisland/pitch-template),
and collecting feedback on it.

`*` This goes for features too, a feature needs to solve a problem. State the problem it solves, then supply a minimal solution.

`**` As long as this project has not seen a public release (i.e. is not on Clojars)
we may still consider making breaking changes, if there is consensus that the
changes are justified.
<!-- /contributing -->

<!-- license-mpl -->
## License

Copyright &copy; 2017-2021 Arne Brasseur and Contributors

Licensed under the term of the Mozilla Public License 2.0, see LICENSE.
<!-- /license-mpl -->
