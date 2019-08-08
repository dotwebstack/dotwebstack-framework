# 1 openapi
This service can be used to expose the internal GraphQL service as a an [openAPI](https://swagger.io/specification/) service.
The service can be configured by providing a `openapi.yml` specification in addition to the `shapes.trig` and `schema.graphqls` configuration files.

The openapi service can be included in a Spring Boot project with the following dependency:
```xml
<dependency>
  <groupId>org.dotwebstack.framework</groupId>
  <artifactId>service-openapi</artifactId>
</dependency>
```

# 1.1 Specification file
The openapi service looks for the OpenAPI specification in the classpath resource `config/model/openapi.yml` by default.
A custom path can be configured in the `application.yml`:
```yaml
dotwebstack:
  openapi:
    specificationFile: config/model/my_alternative_openapi.yml
```
Both `JSON` and `yaml` file formats are supported.
Path operations and types used in the specification should map to the GraphQL service.

# 1.1.1 Operation mapping
Operations in the OpenAPI specification are mapped to GraphQL queries using the value of the `x-dws-query` specification extension. For example, the 
following `get` operation in the `/breweries` path

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

Each operation response should have a reference to the return type using content.<mediaType>.schema.$ref. The following example
specifies that the OK response (200) returns the `Breweries` type:
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

# 1.1.2 Type mapping
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