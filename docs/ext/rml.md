# Extension module: `ext-rml`

This extension can be used to create RDF responses for URI dereferencing via HTTP.

It extends the [OpenApi](service/openapi.md) service module, to add support for providing [RML](https://rml.io/)
mappings which map a GraphQL response to RDF.

The RML extension can be included in a Spring Boot project with the following dependencies:

```xml

<dependency>
  <groupId>org.dotwebstack.framework</groupId>
  <artifactId>service-openapi</artifactId>
</dependency>
<dependency>
  <groupId>org.dotwebstack.framework</groupId>
  <artifactId>ext-rml</artifactId>
</dependency>
```

## Supported RDF formats

The currently supported RDF formats are

| Format                                               | Media type                                                                                  | File extension |
|------------------------------------------------------|---------------------------------------------------------------------------------------------|----------------|
| [JSON-LD](https://json-ld.org/)                      | [application/ld+json](https://www.iana.org/assignments/media-types/application/ld+json)     | .jsonld        |
| [Notation3](https://w3c.github.io/N3/spec/)          | [text/n3](https://www.iana.org/assignments/media-types/text/n3)                             | .n3            |
| [N-Quads](https://www.w3.org/TR/n-quads/)            | [application/n-quads](https://www.iana.org/assignments/media-types/application/n-quads)     | .nq            |
| [N-Triples](https://www.w3.org/TR/n-triples/)        | [application/n-triples](https://www.iana.org/assignments/media-types/application/n-triples) | .nt            |
| [RDF/XML](https://www.w3.org/TR/rdf-syntax-grammar/) | [application/rdf+xml](https://www.iana.org/assignments/media-types/application/rdf+xml)     | .xml           |
| [TriG](https://www.w3.org/TR/trig/)                  | [application/trig](https://www.iana.org/assignments/media-types/application/trig)           | .trig          |
| [Turtle](https://www.w3.org/TR/turtle/)              | [text/turtle](https://www.iana.org/assignments/media-types/text/turtle)                     | .ttl           |

## Configuration

To configure the mapping of responses to RDF, you need an:

* an [OpenAPI](ext/rml?id=openapi) file describing your operations,
* [RML mapping](ext/rml?id=rml-mappings) files to use on operations,
* and, optionally, [namespace prefix declarations](ext/rml?id=configuring-namespace-prefixes) to apply when serializing 
responses.

### OpenApi

To configure operations you need to create een OpenAPI document located at the classpath resources `config/openapi.yaml`
.

#### Operation mapping

Operation requests can be configured as described in [OpenApi](service/openapi.md), except for response mapping
configuration. To create a valid GraphQL query
a [GraphQL selection set](https://spec.graphql.org/draft/#sec-Selection-Sets) must be provided. This can be configured
as a `string` value of `x-dws-query.selectionSet`:

```yaml
paths:
  /doc/beer/{id}:
    get:
      x-dws-query:
        field: beer
        keys:
          identifier: $path.id
        selectionSet: |
          {
            identifier
            name
            abv
            soldPerYear
            retired
            brewery {
              identifier
            }
          }
```

Unlike [regular OpenAPI services](service/openapi?id=operation-mapping) the response schema is not provided. Instead, the
RDF responses will be mapped to RDF using RML mappings. To configure the RML mappings for an operation, we
use `x-dws-rml-mapping` to provide one or more references to RDF files containing the RML mappings. this can be provided
as a string value for a single mapping file name

```
paths:
  /doc/beer/{id}:
    get:
      x-dws-rml-mapping: beer.rml.ttl
```

or a list of strings for multiple mapping file names.

```
paths:
  /doc/beer/{id}:
    get:
      x-dws-rml-mapping:
        - beer.rml.ttl
        - brewery.rml.ttl
```

> Note: When multiple files are provided, they will be merged into a single RDF model, before being interpreted.

#### Content negotiation

It is possible to configure (multiple) `contents` to allow
different [supported RDF response media types](ext/rml?id=supported-rdf-formats):

```yaml
paths:
  /doc/beer/{id}:
    get:
      responses:
        200:
          description: OK
          content:
            text/turtle:
              x-dws-default: true
            application/ld+json: { }
            text/n3: { }
            application/n-quads: { }
            application/n-triples: { }
            application/rdf+xml: { }
            application/trig: { }
```

> Note that it is also possible to use 303 redirects to employ common Linked Data dereferencing strategies.

#### Example operation

```yaml
paths:
  /doc/beer/{id}:
    get:
      parameters:
        - name: id
          description: Identifier
          in: path
          required: true
          schema:
            type: string
      x-dws-query:
        field: beer
        keys:
          identifier: $path.id
        selectionSet: |
          {
            identifier
            name
            abv
            soldPerYear
            retired
            brewery {
              identifier
            }
          }
      x-dws-rml-mapping: beer.rml.ttl
      responses:
        200:
          description: OK
          content:
            text/turtle:
              x-dws-default: true
            application/ld+json: { }
            text/n3: { }
            application/n-quads: { }
            application/n-triples: { }
            application/rdf+xml: { }
            application/trig: { }
```

### RML Mappings

The RML extension looks for [RML mapping](https://rml.io/) files in the classpath resource directory `config/rml/`. RML
mapping files can be provided in the [supported formats](ext/rml?id=supported-rdf-formats), and the format will be
automatically detected based on the [file extension](ext/rml?id=supported-rdf-formats).

The mappings are executed using the [CARML](https://github.com/carml/carml) library.

#### RML logical sources

The mappings will be executed on the [`data` entry](https://spec.graphql.org/draft/#sec-Data) in the GraphQL response.

An RML mapping requires a [logical source](https://rml.io/specs/rml/#logical-source) to describe access to the data
Because the data is always a JSON document, currently, the only
supported [`referenceFormulation`](https://rml.io/specs/rml/#referenceFormulation) is JSONPath.

An `rml:source` is required by RML, but is ignored in this extension, since the source is always the JSON
response `data` entry.

Example logical source:

```yaml
:Beer_LogicalSource
  rml:source "" ;                        # Required by RML, but ignored
  rml:referenceFormulation ql:JSONPath ; # Required
  rml:iterator "$" ;                     # JSON Path iterator expression
.
```

### Configuring namespace prefixes

Several RDF formats support namespace prefixes for more readable RDF serialization.

By default, the following prefixes are applied:

| prefix  | name                                        |
|---------|---------------------------------------------|
| rdf     | http://www.w3.org/1999/02/22-rdf-syntax-ns# |
| rdfs    | http://www.w3.org/2000/01/rdf-schema#       |
| owl     | http://www.w3.org/2002/07/owl#              |
| xsd     | http://www.w3.org/2001/XMLSchema#           |
| dcterms | http://purl.org/dc/terms/                   |

Custom namespace prefixes can be configured by specifying a dictionary as the value of `rml.namespacePrefixes` in
the `dotwebstack.yaml` config file.

```yaml
rml:
  namespacePrefixes:
    beer: http://dotwebstack.org/def/beer#
    foaf: http://xmlns.com/foaf/0.1/
    gsp: http://www.opengis.net/ont/geosparql#
```

Custom prefixes will be merged with the default prefixes, and applied to supporting response formats.

> If a custom prefix is the same as a default prefix, the custom prefix overrides the default prefix. However, this is discouraged.

## Customizing RML mapping behavior

CARML supports customization which van be configured through its `RdfRmlMapper.Builder` class.

To be able to provide custom options we provide the `org.dotwebstack.framework.ext.rml.mapping.RmlMapperConfigurer`
interface with a single method:

```java
void configureMapper(RdfRmlMapper.Builder builder);
```

This interface can be implemented in your own Spring `@Component` by which it will be picked up and applied when
building your application.

### Example - RML mapping functions

A useful RML mapping customization is the ability to integrate
custom [function executions in your RML mapping](https://fno.io/rml/).

To configure this in CARML we can use [CARML's function extension](https://github.com/carml/carml#function-extension).

As an example, we could create a class `RmlFunctions`

```java
public class RmlFunctions {

  @FnoFunction("http://example.org/sumFunction")
  public int sumFunction(
      @FnoParam("http://example.org/intParameterA") int intA,
      @FnoParam("http://example.org/intParameterB") int intB
  ) {
    return intA + intB;
  }
}
```

and a class ```FunctionRmlMapperConfigurer```

```java

@Component
public class FunctionRmlMapperConfigurer implements RmlMapperConfigurer {

  @Override
  public void configureMapper(RdfRmlMapper.Builder builder) {
    builder.addFunctions(new RmlFunctions());
  }
}
```

To be able to use an `ex:sumFunction` execution in your RMl mappings.
