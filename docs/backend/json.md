# Backend module: `backend-json`

## Setup

The following snippet from a `dotwebstack.yaml` file shows the configuration properties for a JSON backend:

```yaml
queries:
  beers:
    type: Beer
    list: true
  beer:
    type: Beer
    keys:
      - field: identifier
    nullable: true

objectTypes:
  Beer:
    backend: json
    file: data.json
    queryPaths:
      beer: $.beers[*].beer[?]
      beers: $.beers[*].beer
    keys:
      - field: identifier
    fields:
      identifier:
        type: ID
      name:
        type: String
      abv:
        type: Float
      brewery:
        type: Brewery

  Brewery:
    backend: json
    keys:
      - field: identifier
    fields:
      identifier:
        type: ID
      name:
        type: String
      beers:
        type: Beer
        list: true
```

In the `file` property you provide the name of the datafile.

You can provide a variety of JSONPath expressions to your needs in the queryPaths property.

A GraphQL schema is constructed based on the configuration in the `dotwebstack.yaml`.

The following example shows the `dotwebstack.yaml` description for several queries to retrieve data from a JSON file:

```yaml
queries:
  beers:
    type: Beer
    list: true
  beer:
    type: Beer
    keys:
      - field: identifier
    nullable: true
```

Notice that the `queries` should exactly match the amount, and the names of the properties
defined in the `queryPaths` property of the `objectTypes`.

An `aggregationOf` field configuration is not supported.
