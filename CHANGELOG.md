# Unreleased

## Added

## Fixed

## Changed

# 1.13.95 (2022-01-28 / a9cbeff)

## Fixed

- Fix a stack overflow in `normalize/char-seq` for really large query parameter
  values

# 1.12.89 (2021-11-29 / 2118a75)

## Changed

- Support `toString` on Babashka (requires recent `bb`)

# 1.11.86 (2021-10-28 / 22c27af)

## Fixed

- Fixed an issue in `lambdaisland.uri.normalize/normalize-query` which did
not take into account utf-16 encoding.

# 1.10.79 (2021-10-12 / d90c6a8)

## Changed

- `lambdaisland.uri.normalize/normalize` now also normalizes the fragment.

# 1.4.74 (2021-09-06 / e07f9fd)

## Added

- `uri-str` as an explicit `lambdaisland.uri.URI` to string conversion

## Fixed

- Fixed compatibility with Babashka/SCI. Note that on babashka we can't
  implement IFn or toString, so converting a `URI` back to a string needs to be
  done explicitly with `uri-str`, and it is not possible to use a URI as a
  function. (`(:path uri)` is ok, `(uri :path)` is not).

# 1.4.70 (2021-05-31 / 76999dc)

## Added

- Add `uri?` predicate.

# 1.4.54 (2020-06-16 / 05a8a19)

## Fixed

- Make query decoding handle `+` as space, so the conversion between maps and
  query strings correctly round trips.
- Handle percent encoding of control characters (codepoints < 16)
- make `lambdaisland.uri.platform/string->byte-seq` return unsigned bytes on
  both plaforms (clj/cljs)

# 1.4.49 (2020-06-11 / ee48e58)

## Changed

- Make `assoc-query` / `query-encode` encode spaces as "+" rather than "%20",
  which brings it in line to how most languages/libraries do it.

# 1.3.45 (2020-05-01 / a04368b)

## Added

- Added function for dealing with query strings as maps: `query-string->map`,
  `map->query-string`, `query-map`, `query-encode`, `assoc-query`,
  `assoc-query*`.

## Fixed

- Fix query string normalization, for delimiter characters like `=` and `+`
  there is a semantic difference between the encoded and decoded form, when they
  are encoded in the input normalization should not decode them and vice versa

# 1.2.1 (2020-02-23 / a992787)

## Changed

- Remove dependencies on ClojureScript and data.json.

# 1.2.0 (2020-02-17 / c0e1f1a)

## Added

- `lambdaisland.uri.normalize/normalize`, for normalizing URI instances.

## Changed

- Added type hints to avoid reflection (thanks @totakke!)

# 1.1.0 (2017-04-25)

## Added

- Predicate functions `absolute?` and `relative?`

# 1.0.0 (2017-02-23)

## Added

- Initial release, public vars: `uri`, `join`, `coerce`, `parse`, `edn-readers`