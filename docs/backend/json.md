# Backend module: `backend-json`

## Setup

The following snippet from a `dotwebstack.yaml` file shows the configuration properties for a JSON
backend:

```yaml
objectTypes:
  Beer:
    backend: json
    file: beers.json
    queryPaths:
      beer: $.beers[?]
      beers: $.beers
      beers_by_country_and_name: $.beers[?][?]
    keys:
      - field: identifier
    fields:
      identifier:
  Brewery:
    backend: json
    file: breweries.json
    queryPaths:
      brewery: $..breweries[?]
      breweries: $..breweries
    keys:
      - field: identifier
    fields:
      identifier:
```

In the `file` property you provide the name of the datafile.

You can provide a variety of JSONPath expressions to your needs in the queryPaths property.

You need to add a `schema.graphqls` file that defines the GraphQL schema.

The following example shows the GraphQL description for several queries to retrieve data from a JSON
file:

```graphql
type Query {
    beers: [Beer!]!
    breweries: [Brewery!]!
    brewery(identifier: String! @key(field: "identifier")): Brewery
    beer(identifier: String! @key(field: "identifier")): Beer
    beers_by_country_and_name(country: String! @key,name: String! @key): [Beer!]!
}
```

Notice that the `queries`, in the `schema.graphqls`, should exactly match the amount, and the names
of the properties defined in the `queryPaths` property of the `dotwebstack.yaml`.

An `aggregationOf` field configuration is not supported.
