# Core: `GraphQl proxy`

By default, the core is configured to initialize and make use of a local graphQl service layer connected to a backend.
It is also possible to configure DWS to connect to a remote graphQl HTTP web service, which will forward graphQl queries to the proxy service over HTTP.

## Proxy settings
A proxy may be configured using `settings` in the `dotwebstack.yaml` configuration.
```yaml
settings:
  graphql:
    proxy: aProxy
```
This configuration indicates that the graphQl service will make use of a proxy named `aProxy`.
Connection settings for the proxy will be resolved from properties within `dotwebstack.graphql.proxies.<proxyname>`.

For example, in `application.yaml`:
```yaml
dotwebstack:
  graphql:
    proxies:
      aProxy:
        uri: "http://graphql.remoteservice.com:8080/"
```
Proxy properties are:
* `uri`: the connection uri for the remote service.

## Limitations
It is not possible to start DWS with both a proxy and a native graphQl / backend configuration.