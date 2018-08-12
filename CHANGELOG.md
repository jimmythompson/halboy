## 4.0.0 (August 12th, 2018)

BACKWARDS INCOMPATIBILITIES / NOTES:

* Prior to this version, the query parameter map passed to the underlying HTTP
client received a map with `String` keys and values of the type passed to the
`navigator` function. In this release, all values are converted to `String`s or
lists of `String`s to better support URI templating. If tests of code that use
halboy make use of `http-kit.fake` they may need to be updated to match the 
new query parameter format.

IMPROVEMENTS:

* `navigator` now fully supports URI templates as defined in 
[RFC6570](https://tools.ietf.org/html/rfc6570) (level 4).

## 3.1.2 (August 9th, 2018)

IMPROVEMENTS:

* `navigator` now allows multiple templated query parameters in templated hrefs.

## 3.1.1 (August 2nd, 2018)

IMPROVEMENTS:

* `navigator` now supports HTTP HEAD requests.

## 3.1.0 (June 11th, 2018)

IMPROVEMENTS:

* You can now pass a `String` when creating a resource and the full self link 
will be created for you.
* You can now create resources with multiple hrefs for the same link name. 

## 3.0.0 (February 14th, 2018)

IMPROVEMENTS:

* You can now specify your own JSON HTTP client.
* You can now pass global request options through to the embedded HTTP client.
* `resource/get-links` has been renamed to `resource/links`.
* `navigator/options` has been renamed to `navigator/settings`.

