# Core: Filtering

It is possible to make use of filters while quering a list of (nested) objects. 

The following types are currently supported:
- A `String` field can be filtered in two ways; exact, with operators `eq`, `in` and `not` or partial based, with operators `match` and `not`.
- An `Int` field can be filtered with the operators: `eq`, `in`, `lt`,`lte`,`gt`,`gte` and `not`.
- A `Float` field can be filtered with the operators: `eq`, `in`, `lt`,`lte`,`gt`,`gte` and `not`.
- A `Date` field can be filtered with the operators: `eq`, `lt`,`lte`,`gt`,`gte` and `not`.
- A `DateTime` field can be filtered with the operators: `eq`, `lt`,`lte`,`gt`,`gte` and `not`.
- A `Boolean` field can be filtered with `true` or `false`.
- The list fields of type `String`, `Int`, `Float` and enumerations can be filtered with the operators: `eq`, `containsAllOf`, `containsAnyOf` and `not`.

The filter type can be configured on each filter with the `type` property. The default filter type is `exact`, the `partial` filter type can be used by `String` field types for partial matching.

## Enumeration

For enum it is possible to add extra enum configuration for validation and typing:

```yaml
enum:
  type: <type>
  values: <array of valid values>
```

Both type and values are optional. `Type` can be used by the backends and `values` will be used to validate the given argument in the filter of the GraphQL query.

## Setup

The following snippet from an `dotwebstack.yaml` file shows the `filters` configuration properties. 

The `field` and `default` properties within the `filters` configuration property are both optional. When the `field` property isn't supplied, the `filter` name needs to correspond with an existing field.

```yaml
queries:
  beers:
    type: Beer
    list: true

objectTypes:
  Beer:
    fields:
      identifier:
        type: ID
      name:
        type: String
      soldPerYear:
        type: Int
      taste:
        type: String
        list: true
        enum:
          type: beer_taste
          values: ["MEATY", "FRUITY", "SMOKY", "SPICY", "WATERY"]  
    filters:
      name: {}
      soldPerYear: {}
      taste: {}
      
  Brewery:
    fields:
      identifier:
        type: ID
      name:
        type: String
      status:
        type: String
        enum:
          type: BreweryStatus
          values: ["active", "inactive"]
      multinational:
        type: Boolean
      beers:
        type: Beer
        list: true
        nullable: true
        mappedBy: brewery
    filters:
      name:
        type: Partial
      status: {}
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

### LowerThan filter

The `lt` filter operator can be applied with the following query:

```graphql
query {
    beers(filter: { soldPerYear: {lt: 300} }) {
        identifier
        name
    }
}
```

### LowerThanEquals filter

The `lte` filter operator can be applied with the following query:

```graphql
query {
    beers(filter: { soldPerYear: {lte: 300} }) {
        identifier
        name
    }
}
```

### GreaterThan filter

The `gt` filter operator can be applied with the following query:

```graphql
query {
    beers(filter: { soldPerYear: {gt: 300} }) {
        identifier
        name
    }
}
```

### GreaterThanEquals filter

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

The `boolean` filter can be applied with the following query:

```graphql
query {
    breweries(filter: { multinational: true }) {
        identifier
        name
    }
}
```

### Nested filter

The filter can also be applied on a nested list of objects.

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

### Or filter

```graphql
query {
    breweries(filter: {name: {eq: "Brewery X"}, status: {eq: "active"}, _or: {name: {eq: "Brewery Z"}}}) {
        identifier
        name
    }
}
```
