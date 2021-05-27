# Core: `filtering`

It is possible to make use of filters while quering an list of (nested) objectTypes. 

The following types are currently supported:

An `String` field can be filtered with the operators: `eq`, `in` and `not`.

An `Int` field can be filtered with the operators: `eq`, `in`, `lt`,`lte`,`gt`,`gte` and `not`.

An `Float` field can be filtered with the operators: `eq`, `in`, `lt`,`lte`,`gt`,`gte` and `not`.

An `Date` field can be filtered with the operators: `eq`, `lt`,`lte`,`gt`,`gte` and `not`.

An `DateTime` field can be filtered with the operators: `eq`, `lt`,`lte`,`gt`,`gte` and `not`.

An `Boolean` field can be filtered with `true` or `false`.

## Setup

The following snippet from an `dotwebstack.yaml` file shows the `filters` configuration properties. The `field`and `default`properties within the `filters` configuration property are both optional. When the `field` property isn't supplied the `filter` name needs to correspond with an existing field.


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
      soldPerYear:
        type: Int
    filters:
      name: {}
      soldPerYear: {}
  Brewery:
    backend: x
    keys:
      - field: identifier
    fields:
      identifier:
        type: ID
      name:
        type: String
      multinational:
        type: Boolean
      beers:
        type: Beer
        list: true
        nullable: true
        mappedBy: brewery
    filters:
      multinational:
        default: true
```

### Equals filter

The `eq` filter operator can be applied with the following query:

```graphql
query {
    beers(filter: { name: {eq: "Beer 1"} }) {
        identifier
        name
    }
}
```

### In filter

The `in` filter operator can be applied with the following query:


```graphql
query {
    beers(filter: { name: {in: ["Beer 1","Beer 2"]} }) {
        identifier
        name
    }
}
```

### LowerThen filter

The `lt` filter operator can be applied with the following query:


```graphql
query {
    beers(filter: { soldPerYear: {lt: 300} }) {
        identifier
        name
    }
}
```

### LowerThenEquals filter

The `lte` filter operator can be applied with the following query:


```graphql
query {
    beers(filter: { soldPerYear: {lte: 300} }) {
        identifier
        name
    }
}
```

### GreaterThen filter

The `gt` filter operator can be applied with the following query:

```graphql
query {
    beers(filter: { soldPerYear: {gt: 300} }) {
        identifier
        name
    }
}
```

### GreaterThenEquals filter

The `gte` filter operator can be applied with the following query:


```graphql
query {
    beers(filter: { soldPerYear: {gte: 300} }) {
        identifier
        name
    }
}
```

### Not filter

The `not` filter operator can be applied with the following query:


```graphql
query {
    beers(filter: { not: {soldPerYear: {gt: 300, lt: 500} } }) {
        identifier
        name
    }
}
```

### Boolean Filter

An `boolean` filter can be applied with the following query:


```graphql
query {
    breweries(filter: { multinational: true }) {
        identifier
        name
    }
}
```

### Nested filter

An filter can also be applied on a nested list of objects.

```graphql
query {
    breweries {
        identifier
        name
        beers(filter: { not: {soldPerYear: {gt: 300, lt: 500} } }) { 
            identifier
            name
        }
    }
}
```

