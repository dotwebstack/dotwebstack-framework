# 1 openapi
This service can be used to expose the internal GraphQL service as an [OpenAPI](https://github.com/OAI/OpenAPI-Specification) service.
The service can be configured by providing an `openapi.yaml` specification in the resource path.

The OpenAPI service can be included in a Spring Boot project with the following dependency:

```xml
<dependency>
  <groupId>org.dotwebstack.framework</groupId>
  <artifactId>service-openapi</artifactId>
</dependency>
```

# 1.1 Specification file
The OpenAPI service looks for the OpenAPI specification in the classpath resource `config/openapi.yaml`.
Path operations and types used in the specification should map to the GraphQL service.

# 1.1.1 Operation mapping
Operations in the OpenAPI specification are mapped to GraphQL queries using the value of the `x-dws-query` specification extension. For example, the following `get` operation in the `/breweries` path:

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

Each OK operation response (2xx) should have a reference to the return type using `content.<mediaType>.schema.$ref`. The following example specifies that the OK response (200) returns the `Breweries` type:

```yaml
responses:
  200:
    description: OK
    content:
    application/hal+json:
      schema:
        $ref: '#/components/schemas/Breweries'
```

A `Redirect` operation response (3xx) does not have a content but must have a `Location` header. The value of a the `Location` must be specified in the
 in a `x-dws-expr` and should be a valid [JEXL](http://commons.apache.org/proper/commons-jexl/) expression. See [Response properties expression](#118-response-properties-expression) for more information over `x-dws-expr`.

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

Foreach unique operation path you are capable to fire a preflight request which will return a empty response body and a 'Allow' response header which contains all allowed httpMethods.

# 1.1.2 Operation parameters
The use of operation parameters is supported for path variables, query string variables and HTTP header variables. The following OAS example defines a `path` parameter of type `string` for the `get` operation:

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

All `Query` and `Path` parameters provided in a request should exist in the OpenApi schema. If this not the case for a given request,
the application will return a `400` response with and a message stating which of the given parameters are not allowed.  

# 1.1.3 Sort parameter
The parameter for providing sort information is modelled with a vendor extension `x-dws-type: sort`. Parameters with this extension should have an array type schema where the array contains the fields on which to sort.
**Ordering:** A field preceded by `-` is mapped to DESC order and a field without a prefix to ASC order.
**Default:** A default value may be specified which will be used if there is no input from the request.
The following parameter will sort on ascending name and descending description and specifies the default value `['name']`:

```yaml
     parameters:
        - name: sort
          in: header
          x-dws-type: sort
          schema:
            type: array
            default: ['name']
            items:
              type: string
              enum: ['name', '-description']
```

# 1.1.4 Expand parameter
By default only GraphQL fields with the `ID` type and the fields that are marked as `required` in the OpenApi response 
are returned. If a required field in OpenApi is of type `object` in GraphQL, only the child fields of this type with the `ID` 
type are returned by default. 

It is possible to expand a query with fields that are not returned by default by adding a parameter with 
`x-dws-type: expand`. This parameter can be of type `string` or `array` and the values should refer to a field in GraphQL: 

```yaml
name: expand  
  x-dws-type: expand
  in: query
  schema:
    type: array
    default: ['beers']
    items:
      type: string
      enum: ['beers', 'beers.ingredients', 'beers.supplements']
```

In the example the expand parameter is used to include `beers` in the response by default. Since `beers` refers to an 
object field in GraphQL, it means that the fields within `beers` with an `ID` type are returned as well, all other fields 
are not by default. In order to expand the fields that do no have this `ID` type, the user has to provide an expand 
parameter with value `beers.ingredients`. It is possible to expand lower level fields with a dotted notation, without explicitly 
expanding the parent objects. Parent objects are added to the query automatically. This means that when you expand the query 
with `beers.ingredients` it is not necessary to provide a separate expand value for `beers`. However, when you add the 
`beers` value too, it is only added once.  

In the example you can see usage of the `default` and `enum` flags. It is possible to use these to expand the query by 
default with one or more values and to restrict which values can be expanded.  

# 1.1.5 Required fields
In some cases fields are only used within an x-dws-expr. The `requiredField` parameter of the `x-dws-query' extension 
can be used to list fields that are not part of the response data but are required to evaluate the expression:

```yaml
x-dws-query:
  field: brewery
  requiredFields:
    - postalCode
```

# 1.1.6 Request body
In addition to request parameters, it is possible to use the HTTP request body to provide input with the `requestBody` element of an operation:
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
The `requestBody` only supports the `application/json` MediaType as content and should have a schema of type `object` with exactly 1 property. The name of the property is used to map the request body to the GraphQL argument of the corresponding query.

# 1.1.7 Type mapping
Type definitions in the schema are mapped to GraphQL types based on their name. For example, the following OpenAPI type 

```yaml
components:
  schemas:
    Beer:
      type: object
```

will be mapped to the `Beer` type defined in `schema.graphqls`:

Similarly, properties defined in the OpenAPI type are mapped to GraphQL type fields based on their name.
When defining an openAPI type, properties are restricted to a subset of the fields of the corresponding GraphQL type.

# 1.1.8 Envelope type
It is also possible to add fields to an OpenApi response that are not in the GraphQL response. This is useful if you want 
to enrich your response, for example in case of a `hal+json` response. The `_links` or `_embedded` objects you create are
not part of the GraphQL response, but you want them to be part of the rest response. An example can be seen in
the following response that contains an `_embedded` brewery:

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

The GraphQL response contains a list of breweries (with a size of 1). This response is embedded in the `_embedded` field.
The following example shows how this can be configured:

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

# 1.1.9 Response properties expression
By using a response property expression, it is possible to return properties that are derived from one or several GraphQL fields and environmental variables. An expression can be assigned to a property by adding the extension field `x-dws-expr` to a property of type `string`:
```yaml
properties:
  identifier:
    type: string
  link:
    type: string
    x-dws-expr: '`${env.dotwebstack.base_url}/breweries/${fields._parent.name}/beers/${fields.identifier}`'
```
The content of `x-dws-expr` should be a valid [JEXL](http://commons.apache.org/proper/commons-jexl/) expression. The expression is evaluated while translating the GraphQL response to the REST response and supports the following variables:
* `env`: The Spring environment variables. The most straightforward way to use an environment variable is to add it to the `application.yml`.
* `fields.<property>`: A scalar field of the object containing the `x-dws-expr` property.
* `fields._parent.<property>`: Same as above, but using the parent of the object. This construction can be used recursively to access parents of parents: `fields._parent._parent.<property>`.
* `args.<inputName>`: An input parameter mapped to the current container field. Currently, all input parameters are mapped to the root/query field because mapping of OAS parameters to GraphQL arguments is restricted to the query field.
* `args._parent.<inputName>`: Same as above, but using the parent of the object.

In some cases the fields you try to access in an `x-dws-expr` are not always present. For this reason it is possible to specify
a `fallback` for an `x-dws-expr`:

```yaml
x-dws-expr: 
  value: '`${env.dotwebstack.base_url}/breweries/${fields._parent.name}/beers/${fields.identifier}`'
  fallback: null
```

when both the expression defined in the `value` and the `fallback` field result in an error or null, dotwebstack falls back to the
default value defined in the parent schema. If no default is defined, `null` is the default.   

# 1.1.10 AllOf
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

The response of brewery contains the combined set of required properties of both schema's defined under the `allOf` property. 
Currently `anyOf` and `oneOf` are not supported.  

# 1.1.11 Response headers
It is possible to return response headers in a DotWebStack response. Their configuration is similar to response properties: 
```graphql
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
This configuration adds the `X-Pagination-Page` header to the response. Its value is set using an `x-dws-expr`, similar to response properties.

# 1.1.12 Content negotiation
It is possible to configure (multiple) contents to allow different response types:
```graphql
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
This configuration allows Accept headers for `application/json` and `application/xml`. When no Accept header is provided, the default will be used.
The default is set by using `x-dws-default: true` on a content configuration. 

# 1.2 OpenApi specification on basepath
The OpenApi specification, without the dotwebstack vendor extensions is exposed on the basepath of your API. This way, 
anyone with access to your API can lookup specification used to generate the API.

# 1.3 Dateformats
You can specify `dateproperties` under the `openapi` section in the `application.yml` file. These properties specify
the format and timezone in which dates and datetimes are shown in your response: 

```application.yml
dateproperties:
  dateformat: dd-MM-yyyy
  datetimeformat: yyyy-MM-dd'T'HH:mm:ss.SSSxxx
  timezone: Europe/Amsterdam
```

# 1.4 Templating
In order to use templating, include the pebble templating module or create your own. For configuring a template for a response,
configure a response like the following:
```graphql
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
The template file you are referring to, should be configured in ```config/templates/```. For more information on how to use pebble, see https://pebbletemplates.io.