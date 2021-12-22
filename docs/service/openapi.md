# Service module: `service-openapi`

This service can be used to expose the internal GraphQL service as
an [OpenAPI](https://github.com/OAI/OpenAPI-Specification) service. The service can be configured by providing
an OpenAPI document named `openapi.yaml` in the resource path.

The OpenAPI service can be included in a Spring Boot project with the following dependency:

```xml

<dependency>
    <groupId>org.dotwebstack.framework</groupId>
    <artifactId>service-openapi</artifactId>
</dependency>
```

## OpenAPI document file

The OpenAPI service looks for the OpenAPI document in the classpath resource `config/openapi.yaml`. Path operations
and types used in the OpenAPI document should map to the GraphQL service.

## Operation mapping

Operations in the OpenAPI specification are mapped to GraphQL queries using the value of the `x-dws-query` specification
extension. For example, the following `get` operation in the `/breweries` path:

```yaml
paths:
  /breweries:
    get:
      x-dws-query: default_breweries
```

maps to the `default_breweries` GraphQL query:

```
default_breweries : [Brewery!]!
```

Operations without `x-dws-query` will not be handled by the framework.

Each OK operation response (2xx) should have a reference to the return type using `content.<mediaType>.schema.$ref`. The
following example specifies that the OK response (200) returns the `Breweries` type:

```yaml
responses:
  200:
    description: OK
    content:
    application/hal+json:
      schema:
        $ref: '#/components/schemas/Breweries'
```

A `Redirect` operation response (3xx) does not have a content but must have a `Location` header. The value of a
the `Location` must be specified in the in a `x-dws-expr` and should be a
valid [JEXL](http://commons.apache.org/proper/commons-jexl/) expression.
See [Response properties expression](#response-properties-expression) for more information about `x-dws-expr`.

```yaml
responses:
  303:
    description: REDIRECT
    headers:
      Location:
        schema:
          type: string
          x-dws-expr: '`${env.dotwebstack.base_url}/brewery/${args.identifier}`'
```

Foreach unique operation path you are capable to fire a preflight request which will return a empty response body and
a 'Allow' response header which contains all allowed httpMethods.

## Operation parameters

The use of operation parameters is supported for path variables, query string variables and HTTP header variables. The
following OAS example defines a `path` parameter of type `string` for the `get` operation:

```yaml
paths:
  /breweries/{name}:
    get:
      x-dws-query: breweries
      parameters:
        - name: name
          in: path
          schema:
            type: string
```

By default, a provided parameter is mapped to the corresponding graphQL field argument. The example above would map the
`name` parameter to its graphQL counterpart:
```
breweries(name: String): [Brewery!]!
```
Exceptions to this are the arguments `sort`, `filter` and `context` which are treated separately as described below.

<!--All `Query` and `Path` parameters provided in a request should exist in the OpenApi schema. If this not the case for
a given request, the application will return a `400` response with and a message stating which of the given parameters
are not allowed.-->

## Expand parameter
The `x-dws-type: expand` configuration may be added to a parameter, with an enum of result object property names that
are 'expandable'. These properties will only be selected when explicitly request with `expand=<property>`.

The following configuration makes the properties `beers`, `beers.ingredients` and `beers.supplements` expandable, for a
response object `Brewery`.

```yaml
x-dws-type: expand
name: expand
in: query
schema:
  type: array
  items:
    type: string
    enum: [ 'beers', 'beers.ingredients', 'beers.supplements' ]
      responses:
        ...
                  $ref: '#/components/schemas/Brewery'
```
A query with `?expand=beers` will indicate that the `beers` property should be retrieved.
Properties listed as 'expandable' in the enum support the dotted notation for nesting.

A property marked as 'expandable' should be nullable or not required.

## Included fields
A graphql query is usually constructed based on the response schema specified in the OpenAPI document. But in some cases
it is necessary to request additional fields not specified in the response schema, e.g. for use in 
[expressions](#response-properties-expression).

To this end it is possible to specify a list of "included fields" using vendor extension `x-dws-include`.

`x-dws-include` may be used on [non-envelope](#envelope-type) object schemas.

An included field may only be a scalar type and will be evaluated on the type
in the GraphQl schema corresponding to the schema object type, and included in the GraphQL query.

For example:
```yaml
  Brewery:
    type: object
    x-dws-include:
      - identifier
      - hiddenField
    required:
      - name
      - countries
    properties:
      name:
        type: String
      countries:
        $ref: '#/components/schemas/Countries'
      self:
        type: String
        x-dws-expr: '`http://dotwebstack.org/api/beers/${data.identifier}`'
```

Will lead to `identifier` and `hiddenField` being added to the selection set of the corresponding query.

> Note: a common use case for `x-dws-include` is including fields that are only used in expressions, as `identifier` is
> in the example above.

## Filters
OpenApi queries may add filter configuration under the vendor extension `x-dws-query`.
The filter configuration is mapped to the graphQL filter specified for that field and can make use of parameter values
with a key `args.<parametername>`:

For instance, `args.name` refers to the `name` parameter that occurs in either the path, query, request body or request 
header.

Filters are configured with an optional map `x-dws-query.filters`. The map contains a filter configuration per GraphQL
query field.
```yaml
    x-dws-query:
      field: breweries
      filters:
        <filterConfig>
```
`<filterConfig>` is a map which can be used to supports any filter structure as described in
[filtering](core/filtering.md).

The following describes a filter on the `breweries` field:
```yaml
    x-dws-query:
      field: breweries
      filters:
        name:
          in: args.name
```
With a value `"Brewery A", "Brewery B"` for the query `name` parameter this will produce the filter
`breweries(filter: { name: {in :["Brewery A", "Brewery B"]}})`.

<!-- #### Required values
A path value may be annotated with a `!`, indicating that the value is required for the parent element in the path. The
following configuration specifies that `$path.name` is required:
```yaml
    x-dws-query:
      field: breweries
      filters:
        breweries:
          type: BreweryFilter
          fields:
            name:
              in: $query.name
              eq: $path.name!
```
If `$path.name` is absent, the `name` element (and thus the entire filter) will remain empty, even if `$query.name` is
provided.-->

## Sorting
OpenApi queries may add sort configuration under the vendor extension `x-dws-query`.
The sort configuration is mapped to the graphQL sort argument specified for that field and can make use of parameter values
with a key `args.<parametername>`:

For instance, `args.name` refers to the `name` parameter that occurs in either the path, query, request body or request
header.

The sort field is configured with an optional property `x-dws-query.sort`.
```yaml
    x-dws-query:
      field: breweries
      sort: args.field1
```

The following describes a sort on the `breweries` field:
```yaml
    x-dws-query:
      field: breweries
      sort: args.sort
    parameters:
    - name: sort
      in: query
      schema:
        type: string
        enum: [ 'name', '-name' ]
```
With a value `"name"` for the query `sort` parameter this will produce the query 
`breweries(sort: "NAME")`.

With a value `"-name"` for the query `sort` parameter this will produce the query
`breweries(sort: "NAME_DESC")`.

## Context
OpenApi queries may add context configuration under the vendor extension `x-dws-query`.
The context configuration is mapped to the context specified for that query and can make use of parameter values
with a key `args.<parametername>`:

For instance, `args.name` refers to the `name` parameter that occurs in either the path, query, request body or request
header.

Context fields are configured with an optional map `x-dws-query.context`.
```yaml
    x-dws-query:
      field: breweries
      context:
        field1: args.field1
```

The following describes a context on the `breweriesInContext` field:
```yaml
    x-dws-query:
      field: breweriesInContext
      context:
        validOn: args.validOn
```
With a value `"2021-01-01"` for the query `validOn` parameter this will produce the query with context
`breweriesInContext(context: { validOn: "2021-01-01"})`.

### Expressions
A value may be resolved from an expression, using the `x-dws-expr` extension. The following simple example will populate
the `name.in` field of the graphQL filter with the uppercase of the`args.name` parameter.
```yaml
    x-dws-query:
      field: breweries
      filters:
        name:
          in:
            x-dws-expr: 'args.name.toUpperCase()'
```

## Request body

In addition to request parameters, it is possible to use the HTTP request body to provide input with the `requestBody`
element of an operation:

```yaml
get:
  x-dws-query: query4
  requestBody:
    required: true
    content:
      application/json:
        schema:
          type: object
          properties:
            object3:
              $ref: '#/components/schemas/Object3'
```

The `requestBody` only supports the `application/json` MediaType as content and should have a schema of type `object`
with exactly 1 property. The name of the property is used to map the request body to the GraphQL argument of the
corresponding query.

## Type mapping

Type definitions in the schema are mapped to GraphQL types based on their name. For example, the following OpenAPI type

```yaml
components:
  schemas:
    Beer:
      type: object
```

will be mapped to the `Beer` type defined in `schema.graphqls`:

Similarly, properties defined in the OpenAPI type are mapped to GraphQL type fields based on their name. When defining
an openAPI type, properties are restricted to a subset of the fields of the corresponding GraphQL type.

## Envelope type

It is also possible to add fields to an OpenApi response that are not in the GraphQL response. This is useful if you
want to enrich your response, for example in case of a `hal+json` response. The `_links` or `_embedded` objects you
create are not part of the GraphQL response, but you want them to be part of the rest response. An example can be seen
in the following response that contains an `_embedded` brewery:

```json
{
  "_embedded": [
    {
      "identifier": "1",
      "name": "1923 Brouwerij"
    }
  ]
}
``` 

The GraphQL response contains a list of breweries (with a size of 1). This response is embedded in the `_embedded`
field. The following example shows how this can be configured:

```yaml
BreweryCollection:
  type: object
  required:
    - _embedded
  properties:
    _embedded:
      x-dws-envelope: true
      type: object
      required:
        - breweries
      properties:
        breweries:
          type: array
          items:
            $ref: '#/components/schemas/Brewery'
``` 

The root response is of `type:object` and contains a required property `_embedded`. This `_embedded` property has
`x-dws-envelope: true`. This way DotWebStack knows that this is a property that is not in the GraphQL response. Since
`_embedded` in its turn consists of a list of `Breweries` the GraphQL response is mapped to the `Brewery` object defined
in the OpenApi specification.

## Response properties expression

By using a response property expression, it is possible to return properties that are derived from one or several
GraphQL fields and environmental variables. An expression can be assigned to a property by adding the extension
field `x-dws-expr` to a property of type `string`:

```yaml
properties:
  identifier:
    type: string
  link:
    type: string
    x-dws-expr: '`${env.dotwebstack.base_url}/breweries/${args.name}/beers/${data.identifier}`'
```

The content of `x-dws-expr` should be a valid [JEXL](http://commons.apache.org/proper/commons-jexl/) expression. The
expression is evaluated while translating the GraphQL response to the REST response and supports the following
variables:

* `env.<environmentVariable>`: The Spring environment variables. The most straightforward way to use an environment 
  variable is to add it to the `application.yml`.
* `request`: The Spring ServerRequest object, containing the request information. 
* `args.<inputName>`: An input parameter mapped to the current container field. Currently, all input parameters are
  mapped to the root/query field because mapping of OAS parameters to GraphQL arguments is restricted to the query
  field.
* `args.requestUri`: The requested URI is available via this argument.
* `args.requestPathAndQuery`: The path and query part of the requested URI is available via this argument.
* `data.<property>`: A scalar field of the object containing the `x-dws-expr` property.
* `data`: The response data object available during field processing.

In some cases the fields you try to access in an `x-dws-expr` are not always present. For this reason it is possible to
specify a `fallback` for an `x-dws-expr`:

```yaml
x-dws-expr:
  value: '`${env.dotwebstack.base_url}/breweries/${args.name}/beers/${data.identifier}`'
  fallback: null
```

when both the expression defined in the `value` and the `fallback` field result in an error or null, dotwebstack falls
back to the default value defined in the parent schema. If no default is defined, `null` is the default.

## Paging
The openapi module uses the `dotwebstack.yaml` config file to determine if [paging](core/paging.md) is enabled for a
GraphQl query field.
When enabled, paging configuration can be added to the `x-dws-query` settings with a `paging` entry.
```
  x-dws-query:
    field: breweries
    paging:
      pageSize: args.pageSize
      page: args.page
```
The entries `pageSize` and `page` map to parameters which will be used to populate the graphpQL 
[paging settings](core/paging.md).
If paging is disabled, the generated GraphQL query will not contain the `nodes` wrapper field for paged collections.

To create page links in responses, JEXL functions are available, which can be used in a `x-dws-expr`, and need to be
passed available arguments using existing
[Response properties expressions](service/openapi?id=response-properties-expression):

- `paging:next(data, args.pageSize, env.your.api.base-url.here, args.requestPathAndQuery)`
  generates a next page link, only if a result set's size matches the requested page size.
- `paging:prev(env.your.api.base-url.here, args.requestPathAndQuery)`
  generates a next page link, only from page 2 and up.

Usage example:

```yaml
_links:
  type: object
  x-dws-envelope: true
  properties:
    next:
      type: object
      x-dws-envelope: true
      required:
        - href
      properties:
        href:
          type: string
          format: uri
          x-dws-expr: "paging:next(data, args.pageSize, env.your.api.base-url.here, args.requestPathAndQuery)"
    prev:
      type: object
      x-dws-envelope: true
      required:
        - href
      properties:
        href:
          type: string
          format: uri
          x-dws-expr: "paging:prev(env.your.api.base-url.here, args.requestPathAndQuery)"
```

## AllOf

It is possible to define an `allOf` property, the resulting property is the combined result of all underlying schema's:

```yaml
brewery:
  type: object
  allOf:
    - $ref: '#/composed/schema/beer'
    - type: object
      required:
        - identifier
      properties:
        identifier:
          type: string
```  

The response of brewery contains the combined set of required properties of both schema's defined under the `allOf`
property. Currently `anyOf` and `oneOf` are not supported.

## Content negotiation

It is possible to configure (multiple) contents to allow different response types:

```yaml
  /breweries:
    get:
      responses:
        200:
          description: OK
          content:
            application/hal+json:
              x-dws-default: true
              schema:
                ...
            application/xml:
              schema:
                ...
```

This configuration allows Accept headers for `application/json` and `application/xml`. When no Accept header is
provided, the default will be used. The default is set by using `x-dws-default: true` on a content configuration.

## Response headers

It is possible to return response headers in a DotWebStack response. Their configuration is similar to response
properties:

```yaml
/breweries:
  get:
    x-dws-query: breweries
    responses:
      200:
        ...
        headers:
          X-Pagination-Page:
            schema:
              type: string
              x-dws-expr: '`args.pageSize`'
```

This configuration adds the `X-Pagination-Page` header to the response. Its value is set using an `x-dws-expr`, similar
to response properties.

## Redirects

Configuring custom redirects reuses the ability to [customize Response headers](#response-headers), combined with a 3XX 
response.

```yaml
/breweries:
  get:
    x-dws-query: breweries
    responses:
      303:
        ...
        headers:
          Location:
            schema:
              type: string
              x-dws-expr: '`${env.baseUrl}/see-other-breweries`'
```

In some cases it is useful to be able to control the resulting `Location` header based on a media type provided in the
`Accept` header of the request.

To achieve this the `req:accepts` JEXL function can be used. Provided with a media type template string it will return
whether this media type is accepted by the client.

Using that information one could alter the location header.

```yaml
/breweries/{identifier}:
  get:
    x-dws-query: breweries
    responses:
      303:
        ...
        headers:
          Location:
            schema:
              type: string
              x-dws-expr: |
                req:accepts("text/html", request) ?
                `${env.dotwebstack.baseUrl}/page/beer/${args.identifier}.html` :
                `${env.dotwebstack.baseUrl}/doc/beer/${args.identifier}`
```

> NOTE: The `req:accepts` does not take into account the media type quality factor

<!-- ## Default values

It's possible to define default (static) values for OAS properties as shown below.

```yaml
Brewery:
  type: object
  required:
    - identifier
    - name
    - countries
  properties:
    identifier:
      type: string
    countries:
      type: array
      x-dws-default: [ 'Netherlands','Belgium' ]
      items:
        type: string
    class:
      type: string
      x-dws-default: 'Brewery'
```
-->
<!-- ## Conditional include response objects

By using the `x-dws-include` extension, it is possible to decide with a condition whether the object needs to be
included in the response. This is useful in the case when you are constructing an object with jexl evaluated properties.
A condition can be assigned to a response object by adding the extension field `x-dws-include` to an object:

```yaml
Brewery:
  type: object
  required:
    - identifier
    - name
    - countries
  x-dws-include: "identifier != null"
  properties:
    identifier:
      type: string
```

The content of `x-dws-include` should be a valid [JEXL](http://commons.apache.org/proper/commons-jexl/) expression and
should return a boolean. The expression is evaluated while translating the GraphQL response to the REST response and
supports the following variables:

- `<property>`: A scalar field of the object containing the `x-dws-include` property.
- `<nestedobject>.<property>`: Same as above, but using the property of an nested object.
-->
## Problem+json

By using `application/problem+json` is it possible to return any error as `application/problem+json` content-type. The
following properties can be used:

- type
- title
- detail
- instance
- status

It is also possible to use a custom property which will be returned in the problem response (key/value). The value must
be defined in an `x-dws-expr`. For _not acceptable_ errors it is possible to use `acceptableMimeTypes` which will return
all acceptable mime types as array.

Example:

```yaml
406:
  description: not acceptable
  content:
    application/problem+json:
      schema:
        type: object
        properties:
          status:
            type: integer
            format: int32
          acceptable:
            type: array
            x-dws-expr: "acceptableMimeTypes"
          customparam:
            type: string
            x-dws-expr: "`foo`"
```

<!-- ## Templating

In order to use templating, include the pebble templating module or create your own. For configuring a template for a
response, configure a response like the following:

```yaml
/breweries:
  get:
    responses:
      200:
        description: OK
        content:
          text/html:
            x-dws-template: your-page.html
            schema:
              ...
          application/xml:
            schema:
            ...
```

The template file you are referring to, should be configured in `config/templates/`. For more information on how to use
pebble, see https://pebbletemplates.io.
-->
## Application properties

### OpenApi document publication

By default, the OpenApi document is exposed on the base path of your API excluding the dotwebstack vendor extensions.
This way, anyone with access to your API can look up the OpenApi document that describes the API.

It is also possible to configure a specific path to expose the OpenApi document on using the `apiDocPublicationPath`
under the `openapi` section in the `application.yml` configuration file. The value of this property must be a string
starting with a `/` followed by a valid path segment according to [RFC-3986](https://tools.ietf.org/html/rfc3986).

For example, the following configuration will expose the api on `{base-path}/openapi.yaml`.

```yaml
dotwebstack:
  openapi:
    apiDocPublicationPath: /openapi.yaml
```

### CORS

CORS (Cross-Origin Resource Sharing) can be enabled (and is disabled by default).

```yaml
dotwebstack:
  openapi:
    cors:
      enabled: true
      allowCredentials: true  # default: false
```

Setting `allowCredentials` to `true` is only needed when your service requires sending client credentials in the
`Authorization`header.

### Date formats

You can specify `dateproperties` under the `openapi` section in the `application.yml` file. These properties specify the
format and timezone in which dates and datetimes are shown in your response:

```yaml
dotwebstack:
  openapi:
    dateproperties:
      dateformat: dd-MM-yyyy
      datetimeformat: yyyy-MM-dd'T'HH:mm:ss.SSSxxx
      timezone: Europe/Amsterdam
```

### Spatial
Optional configuration for populating the `srid` argument of a GraphQL 'Geometry' type with a parameter value may be added under `dotwebstack.openapi.spatial`.
The following configuration specifies the `accept-crs` parameter as `srid` input:
```yaml
dotwebstack:
  openapi:
    spatial:
      sridParameter:
        name: accept-crs
        valueMap:
          '[epsg:28992]': 7415
          '[epsg:4258]': 7931
```
- `sridParameter.name` specifies the name of the parameter. Depending on the presence of `valueMap`, it may either be a `string` or an `integer`.
- `sridParameter.valueMap` is an optional String to Integer map. When present, the parameter value should be a `string` and will be translated using this map. If the map is not present, the parameter value will be passed as-is, and should be an `integer` or a `string` with integer format.

<!-- ### Static Resources

To use static resources, create a folder `assets` in the `config` dir. Place the desired assets in the `assets` folder,
ex: `config/assets/your-image.jpg`. The resource will become available
on `http://{your-dws-service}/assets/your-image.jpg`.
-->
