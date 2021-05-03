# Extension module: `templating-pebble`

This templating module uses the Pebble templating implementation.

See https://pebbletemplates.io for documentation on Pebble templating syntax.

## Setup

Import the following dependency:

```xml

<dependency>
    <groupId>org.dotwebstack.framework</groupId>
    <artifactId>templating-pebble</artifactId>
</dependency>
```

## Template file location

In order to create your own template files, create these in `config/templates/`.

## Template file variable references

For this implementation, we have prefixed different values regarding the source of the values.

- Values that are part of the result fields of the GraphQL query, are prefixed with: `fields.`
- Values that are part of the input arguments of the GraphQL query, are prefixed with: `args.` (by default, the request
  URI is available as `args.request_uri`)
- Values that are part of the environment variables, are prefixed with: `env.`

For example, in the example project we are referring to breweries. The name of the brewery is part of the result fields,
as key `name`. In order to use this in the pebble template, we need to use `{{ fields.name }}`.

## Custom filter

In order to create a custom Pebble filter, refer to: https://pebbletemplates.io/wiki/guide/extending-pebble.

## RDF4J integration

For integration of RDF4J with Pebble templating, an additional module is provided:

```xml

<dependency>
    <groupId>org.dotwebstack.framework</groupId>
    <artifactId>templating-pebble-rdf4j</artifactId>
</dependency>
```

### Filter `jsonld`

In order to encode an RDF4J model as a JSON-LD document, use the `jsonld` filter.

For example, to encode the value of the `fields` variable as a JSON-LD document, use:

```html
{% block script %}
<script type="application/ld+json">{ fields | jsonld | raw }}</script>
{% endblock %}
```

The addition `raw` makes sure the result is not encoded.
