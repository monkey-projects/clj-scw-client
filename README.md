# Clojure Scaleway API Client

This is a [Clojure](https://clojure.org) library that provides client functionality
to access the [Scaleway API](https://www.scaleway.com/en/developers/api/).
[Scaleway](https://www.scaleway.com) is a Europe-based public cloud provider.

This library uses [Martian]*(https://github.com/oliyh/martian) to fetch the
OpenAPI specifications and to provide access points to use the API.

## Usage

Include the library in your project:
```clojure
# deps.edn
{com.monkeyprojects/clj-scw-client {:mvn/version "<VERSION>"}}
```

Or with [Leiningen](https://leiningen.org):
```clojure
[com.monkeyprojects/clj-scw-client "<VERSION>"]
```

The require the `monkey.scw.core` and the `martian.core` namespaces, and you're
ready to invoke the API.

```clojure
(require '[martian.core :as mc])
(require '[monkey.scw.core :as c])

;; Create a context, for example to access containers
;; Notice that you need to specify a secret key for authentication.
(def ctx (c/containers-ctx {:secret-key "my-very-secret-key"}))

;; Explore the api, see which operations are available
(mc/explore ctx) ; => [[:list-namespaces "Lists namespaces"] ...]

;; Or explore a specific endpoint
(mc/explore ctx :list-namespaces) ; => Displays required parameters, etc.

;; Invoke an endpoint
@(mc/response-for ctx :list-namespaces {:region "fr-par"})
;; => Returns a `deref`able value that will hold the response, including headers, body...
```

Since we're using [Aleph](https://aleph.io), all responses are asynchronous.  This
allows you to send multiple requests in parallel.  You do need to `deref` each response
in order to get to the result.

## Available API's

Currently, these api's are available.  In order to use them, invoke the associated function
to create a context that accesses the endpoint.

|---|---|
|API|Context function|
|---|---|
|Instances|`instance-ctx`|
|Serverless containers|`containers-ctx`|
|Serverless functions|`functions-ctx`|
|Serverless jobs|`jobs-ctx`|
|Container registry|`registry-ctx`|
|NATS messaging|`nats-ctx`|
|Secrets|`secrets-ctx`|
|Domains and DNS|`domain-ctx`|
|---|---|

### Object Storage

A special case is object storage, which exposes an AWS S3 compatible API.  We don't provide
functionality for that here, as [several](https://github.com/weavejester/clj-aws-s3)
[libraries](https://github.com/mcohen01/amazonica) already exist that provide that functionality.

## License

[MIT License](LICENSE)

Copyright (c) 2025 by [Monkey Projects BV](https://www.monkey-projects.be).