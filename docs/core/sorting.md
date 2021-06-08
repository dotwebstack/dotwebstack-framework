# Core: `sorting`

It is possible to make use of sorting while quering an list of (nested) objectTypes. 

## Setup

The following snippet from an `dotwebstack.yaml` file shows the `sortableBy` configuration properties. 

Define a name for sorting which will be used in the graphql `<objectType>Order` enum type. Under each `sortableBy` property you can  
define one or more (nested) fields to sort on.

By default the first entry of the `sortableBy` is used for sorting. 

```yaml
queries:
  beers:
    type: Beer
    list: true

objectTypes:
  Beer:
    backend: x
    keys:
      - field: identifier
    fields:
      identifier:
        type: ID
      name:
        type: String
      brewery:
        type: Brewery
    sortableBy:
      brewery:
        - field: brewery.name
          direction: ASC
      name:
        - field: name
          direction: ASC
        - field: identifier
          direction: ASC
  Brewery:
    backend: x
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
        nullable: true
        mappedBy: brewery
    sortableBy:
      name:
        - field: name
          direction: DESC
```

Query:

```graphql
query {
    beer(sort: BeerOrder = BREWERY) {
        identifier
        name
    }
}
```

```graphql
query {
    beer(sort: BreweryOrder = NAME) {
        identifier
        name
    }
}
```