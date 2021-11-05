# Core: Paging

It is possible to make use of paging while quering an list of (nested) objectTypes. 

## Setup

The paging functionality can be activated per query and objectfield by setting the `pageable` property on `true`. DotWebStack will generate an `Connection` GraphQL object for each list return type to support pagination.

```yaml
features:
  - paging
    
queries:
  beers:
    type: Beer
    list: true
    pageable: true
  breweries:
    type: Brewery
    list: true
    pageable: true

objectTypes:
  Beer:
    fields:
      identifier:
        type: ID
      name:
        type: String
      brewery:
        type: Brewery
  Brewery:
    fields:
      identifier:
        type: ID
      name:
        type: String
      beers:
        type: Beer
        list: true
        pageable: true
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