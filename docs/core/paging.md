# Core: Paging

It is possible to make use of paging while quering an list of (nested) objectTypes. 

## Setup

The paging functionality can be activated by the `paging` feature toggle. Once activated DotWebStack will generate an `Connection` GraphQL object for each list return type to support pagination.

```yaml
features:
  - paging
    
queries:
  beers:
    type: Beer
    list: true
  breweries:
    type: Brewery
    list: true

objectTypes:
  Beer:
    keys:
      - field: identifier
    fields:
      identifier:
        type: ID
      name:
        type: String
      brewery:
        type: Brewery
  Brewery:
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
```

Query:

```graphql
query {
    beers(first:10, offset: 1) {
        nodes {
            identifier
            name
        }
    }
}
```

```graphql
query {
    breweries {
        nodes {
            identifier
            name
            beers(first: 10, offset: 1) {
                nodes {
                    identifier
                    name
                }
                offset
            }
        }
    }
}
```