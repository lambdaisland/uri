# Unreleased

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
