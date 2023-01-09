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

The `field` and `default` properties within the `filters` configuration property are both optional. When the `field` property isn't supplied, the `filter` name needs to correspond with an existing field. An filter may also point implicit or explicit to a field that is not visible for selection. This can be configured for a object field with the `visible` configuration property. Default value is `true`


The `dependsOn` property can be used when a filter depends on another filter. This will make the depends on filter mandatory.

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
      created:
        type: DateTime
        nullable: true
        visible: false
    filters:
      name:
        type: Partial
      status: {}
      multinational:
        default: true
      created: {}
      beers: {}

  Address:
    fields:
      identifier:
        type: ID
      street:
        type: String
      city:
        type: String
    filters:
      street: {}
      city:
        dependsOn: street
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

### Exists filter

```graphql
query {
    breweries(filter: {beers: {_exists: true}}) {
        identifier
        name
    }
}
```

# Filtering optimized for large geometries
To improve filter performance it can be useful to split large geometries into smaller segments. This can be realized by
plotting a geometry on a fixed tile grid with tiles of 10 x 10 km. This tile grid is generated once as in the example
underneath:

'''sql
CREATE TABLE IF NOT EXISTS public.tiles_10km AS
SELECT x || '_' || y                                        AS tile_id,
-- Create rectangle polygon
ST_MakeEnvelope(x, y, x + 10000, y + 10000, 28992)  AS geom_rd
-- from bottom leftside: x = (-41171,606), y = (306846,073) with steps of 10km (10000)
FROM generate_series(-41200, 306000, 10000) x
-- from top rightside: x = (278026,057), y = (866614,784) with steps of 10km (10000)
CROSS JOIN generate_series(280000, 870000, 10000) y;

CREATE INDEX IF NOT EXISTS tiles_10km_sdx1 ON public.tiles_10km USING GIST (geom_rd);
'''
If you want to split a large geometry into segments according to the fixed tile grid, you need an extra 'segments' table.
This table is automatically updated during mutation of the geometry. When making use of a filter based on a geometry,
dotwebstack will use the 'segments' table to create the filter conditon.

The segment table must conform to the following conventions: <source table>__<geometry column>__segments
- the 'segments' table is prefixed by the name of the table which contains the geometry that needs to be split
  into segments
- the middle part of the 'segments' table is the name of the geometry column
- the 'segments' table has the postfix 'segments'

The 'segments' table has at least three columns
- tile_id: is a referenceto the 'tiles_10km' table
- <geometrie column>: the name of the geometry column
- the other columns refer to the primary key(s) of the source table

- Example: the table 'brewery' has a column 'geometry' which is split into segments.

| brewery         |         
|-----------------|
| identifier (PK) |
| geometry        |
|                 |

The table above results in the 'segments' table as shown underneath:

| brewery__geometry__segments |
|-----------------------------|
| tile_id                     |
| geometry                    |
| identifier                  |


If the 'segments' table conforms to the above conventions, dotwebstack will use the 'segments' table when a geometry filter is applied. 
