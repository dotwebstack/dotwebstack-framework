# Service module: `service-openapi`

This service can be used to expose the internal GraphQL service as
an [OpenAPI](https://github.com/OAI/OpenAPI-Specification) service. The service can be configured by providing
an `openapi.yaml` specification in the resource path.

The OpenAPI service can be included in a Spring Boot project with the following dependency:

```xml

<dependency>
    <groupId>org.dotwebstack.framework</groupId>
    <artifactId>service-openapi</artifactId>
</dependency>
```

## Specification file

The OpenAPI service looks for the OpenAPI specification in the classpath resource `config/openapi.yaml`. Path operations
and types used in the specification should map to the GraphQL service.

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
@sparql(
  repository: "local"
)
```

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
See [Response properties expression](#118-response-properties-expression) for more information over `x-dws-expr`.

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

Use `x-dws-operation` to define whether the OAS operation needs to be handled by the DWS openapi service. If you want
DWS to ignore this operation set to `false`. Default value is `true`

```yaml
paths:
  /breweries:
    get:
      x-dws-operation: false
```

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

All parameter names in the OAS spec should correspond to existing GraphQL query arguments:

```
breweries(name: String): [Brewery!]!
```

All `Query` and `Path` parameters provided in a request should exist in the OpenApi schema. If this not the case for a
given request, the application will return a `400` response with and a message stating which of the given parameters are
not allowed.

## Sort parameter

The parameter for providing sort information is modelled with a vendor extension `x-dws-type: sort`. Parameters with
this extension should have an array type schema where the array contains the fields on which to sort.
**Ordering:** A field preceded by `-` is mapped to DESC order and a field without a prefix to ASC order.
**Default:** A default value may be specified which will be used if there is no input from the request. The following
parameter will sort on ascending name and descending description and specifies the default value `['name']`:

```yaml
parameters:
  - name: sort
    in: header
    x-dws-type: sort
    schema:
      type: array
      default: [ 'name' ]
      items:
        type: string
        enum: [ 'name', '-description' ]
```

## Expand parameter

By default, only GraphQL fields with the `ID` type and the fields that are marked as `required` in the OpenApi response
are returned. If a required field in OpenApi is of type `object` in GraphQL, only the child fields of this type with
the `ID`
type are returned by default.

It is possible to expand a query with fields that are not returned by default by adding a parameter with
`x-dws-type: expand`. This parameter can be of type `string` or `array` and the values should refer to a field in
GraphQL:

```yaml
x-dws-type: expand
name: expand
in: query
schema:
  type: array
  default: [ 'beers' ]
  items:
    type: string
    enum: [ 'beers', 'beers.ingredients', 'beers.supplements' ]
```

In the example the expand parameter is used to include `beers` in the response by default. Since `beers` refers to an
object field in GraphQL, it means that the fields within `beers` with an `ID` type are returned as well, all other
fields are not by default. In order to expand the fields that do no have this `ID` type, the user has to provide an
expand parameter with value `beers.ingredients`. It is possible to expand lower level fields with a dotted notation,
without explicitly expanding the parent objects. Parent objects are added to the query automatically. This means that
when you expand the query with `beers.ingredients` it is not necessary to provide a separate expand value for `beers`.
However, when you add the
`beers` value too, it is only added once.

In the example you can see usage of the `default` and `enum` flags. It is possible to use these to expand the query by
default with one or more values and to restrict which values can be expanded.

## Keys and filters
OpenApi queries may have vendor extensions under `x-dws-query` to configure key and filter information for the GraphqQL backend.
Keys and filters specify their value by referencing input parameters with `$<type>.<parametername>` where `type` may be:
* `path`
* `body`
* `header`
* `query`
For instance, `$path.name` refers to the `name` parameter that occurs in the path.

### Keys
Keys are configured with an optional map `x-dws-query.keys`, specifying the GraphQL ID field name and an input parameter. If the parameter value is provided, the key will be added to the GraphQL query.
The following will add an `identifier` key field to the GraphQL query and populate it with the `identifier` path variable if provided:
```yaml
    x-dws-query:
      field: brewery
      keys:
        identifier: $path.identifier
```
The key supportes the `.` notation for nested nodes.

### Filters
Filters are configured with an optional map `x-dws-query.filters`. The map contains a filter configuration per GraphQL query field.
```yaml
    x-dws-query:
      field: breweries
      filters:
        <fieldPath1>: <filterConfig>
        <fieldPath2>: <filterConfig>
```
A `fieldPath` key supports the `.` notation for nested filters and map to a `<filterConfig>` with the following structure:
```yaml
    type: <GraphQLFilterType>
    fields:
      <field1>: <fieldFilter>
      <field2>: <fieldFilter>
```
Each `field` maps to a `fieldFilter` map that supports any filter structure as described in [filtering](../core/filtering.md).
Like keys, filters are only added if the corresponding input parameter is provided.

The following describes a `BreweryFilter` type filter on the `breweries` node on the `name` field:
```yaml
    x-dws-query:
      field: breweries
      filters:
        breweries:
          type: BreweryFilter
          fields:
            name:
              in: $query.name
```
With a value `"Brewery A", "Brewery B"` for the query `name` parameter this will produce the filter `breweries(filter: { name: {in :["Brewery A", "Brewery B"]}})`.

#### Required values
A path value may be annotated with a `!`, indicating that the value is required for the parent element in the path. The following configuration specifies that `$path.name` is required:
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
If `$path.name` is absent, the `name` element (and thus the entire filter) will remain empty, even if `$query.name` is provided.

#### Expressions
A value may be resolved from an expression, using the `x-dws-expr` extension. The following simple example will populate the `name` field of the graphQL filter with the uppercase of the`$body.name` parameter.
```yaml
    x-dws-query:
      field: breweries
      filters:
        breweries:
          type: BreweryFilter
          fields:
            name:
              x-dws-expr: '$body.name.toUpperCase()'
```

## Paging
The openapi module uses the `dotwebstack.yaml` config file to determine if paging is enabled.
```yaml
features:
  - paging
```
When enabled, paging configuration can be added to the `x-dws-query` settings with a `paging` entry.
```
  x-dws-query:
    field: breweries
    paging:
      first: $query.pageSize
      offset: $query.page
```
The entries `first` and `offset` map to parameters which will be used to populate the graphpQL [paging settings](../core/paging.md).
If paging is disabled, the generated GraphQL query will not contain the `nodes` wrapper field for paged collections.

## Required fields

In some cases fields are only used within an x-dws-expr. The `requiredField` parameter of the `x-dws-query' extension
can be used to list fields that are not part of the response data but are required to evaluate the expression:

```yaml
x-dws-query:
  field: brewery
  requiredFields:
    - postalCode
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
    x-dws-expr: '`${env.dotwebstack.base_url}/breweries/${fields._parent.name}/beers/${fields.identifier}`'
```

The content of `x-dws-expr` should be a valid [JEXL](http://commons.apache.org/proper/commons-jexl/) expression. The
expression is evaluated while translating the GraphQL response to the REST response and supports the following
variables:

* `env`: The Spring environment variables. The most straightforward way to use an environment variable is to add it to
  the `application.yml`.
* `fields.<property>`: A scalar field of the object containing the `x-dws-expr` property.
* `fields._parent.<property>`: Same as above, but using the parent of the object. This construction can be used
  recursively to access parents of parents: `fields._parent._parent.<property>`.
* `args.<inputName>`: An input parameter mapped to the current container field. Currently, all input parameters are
  mapped to the root/query field because mapping of OAS parameters to GraphQL arguments is restricted to the query
  field.
* `args._parent.<inputName>`: Same as above, but using the parent of the object.
* `args.request_uri`: The requested URI is available via this argument.

In some cases the fields you try to access in an `x-dws-expr` are not always present. For this reason it is possible to
specify a `fallback` for an `x-dws-expr`:

```yaml
x-dws-expr:
  value: '`${env.dotwebstack.base_url}/breweries/${fields._parent.name}/beers/${fields.identifier}`'
  fallback: null
```

when both the expression defined in the `value` and the `fallback` field result in an error or null, dotwebstack falls
back to the default value defined in the parent schema. If no default is defined, `null` is the default.

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

## Default values

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

## Conditional include response objects

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

## Templating

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

### Serialization of null fields

`dotwebstack.openapi.serializeNull` is an optional property that can be set to true/false to include/exclude null fields
in the openAPI response. By default, i.e. if this property is not set, null fields will be serialized.

### Static Resources

To use static resources, create a folder `assets` in the `config` dir. Place the desired assets in the `assets` folder,
ex: `config/assets/your-image.jpg`. The resource will become available
on `http://{your-dws-service}/assets/your-image.jpg`.
