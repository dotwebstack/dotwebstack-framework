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

Each operation response should have a reference to the return type using `content.<mediaType>.schema.$ref`. The following example specifies that the OK response (200) returns the `Breweries` type:

```yaml
responses:
  200:
    description: OK
    content:
    application/hal+json:
      schema:
        $ref: '#/components/schemas/Breweries'
```

Currently, exactly one MediaType per response is supported and it should match `application/.*json`.

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

# 1.1.5 Request body
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

# 1.1.6 Type mapping
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

# 1.1.7 Envelope type
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

# 1.1.8 Response properties template
By using a response property template, it is possible to return properties that are derived from one or several GraphQL fields and environmental variables. A template can be assigned to a property by adding the extension field `x-dws-template` to a property of type `string`:
```yaml
properties:
  identifier:
    type: string
  link:
    type: string
    x-dws-template: '`${env.dotwebstack.base_url}/breweries/${fields._parent.name}/beers/${fields.identifier}`'
```
The content of `x-dws-template` should be a valid [JEXL](http://commons.apache.org/proper/commons-jexl/) expression. The expression is evaluated while translating the GraphQL response to the REST response and supports the following variables:
* `env`: The Spring environment variables. The most straightforward way to use an environment variable is to add it to the `application.yml`.
* `fields.<property>`: A scalar field of the object containing the `x-dws-template` property.
* `fields._parent.<property>`: Same as above, but using the parent of the object. This construction can be used recursively to access parents of parents: `fields._parent._parent.<property>`.
