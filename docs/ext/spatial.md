# Extension module: `ext-spatial`

This extension can be used to include geospatial functionality into a backend.

The spatial extension can be included in a Spring Boot project with the following dependency:

```xml

<dependency>
    <groupId>org.dotwebstack.framework</groupId>
    <artifactId>ext-spatial</artifactId>
</dependency>
```

## Schema type definitions

### Geometry & GeometryType

Geometry is a predefined GraphQL Object Type and GeometryType an Enum type.

```graphql
type Geometry{
  type: GeometryType!
  asWKB: String!
  asWKT: String!
}

enum GeometryType{
  POINT
  LINESTRING
  POLYGON
  MULTIPOINT
  MULTILINESTRING
  MULTIPOLYGON
}
```

Do NOT include these in your `schema.graphqls` configuration, you can refer to them as shown in the next example.

```graphql
type Brewery {
  identifier: ID!
  name: String!
  status: String!
  beers: [Beer]
  postalAddress: Address
  visitAddress: Address
  geometry: Geometry
}
```

Please check backend documentation for prerequisites and information about data storage.

## Query options

### GeometryType conversion

It is possible to convert certain GeometryTypes. Add `(type: GeometryType)` to Geometry in `schema.graphqls`.

```graphql
type Brewery {
  identifier: ID!
  name: String!
  geometry(type: GeometryType): Geometry
}
```

Query example:

```graphql
{
  breweries {
    identifier
    name
    geometry(type: MULTIPOINT) {
      type
      asWKT
    }
  }
}
```

Conversions that are possible:

- `Point` => `MultiPoint`
- `MultiPoint` => `Point` (centroid)
- `LineString` => `MultiLineString`, `Point` (centroid)
- `MultiLineString` => `Point` (centroid)
- `Polygon` => `MultiPolygon`, `Point` (centroid)
- `MultiPolygon` => `Point` (centroid)

## Filtering

An field of type `Geometry` can be filtered with the following filter operations. 

```
input GeometryFilter {
  within: GeometryInput!
  contains: GeometryInput!
  intersects: GeometryInput!
  not: GeometryFilter!
}

input GeometryInput {
  fromWKT: String!
}
```

Example graphql query which filters on a `Geometry` field with the `contains` filter operation:

```graphql
query {
    breweries(filter: {geometry: {contains: {fromWKT: "POINT(1 1)"}}}) {
        identifier
        name
    }
}
```

## Configuration

### CRS

The source CRS can be configured within the `dotwebstack.yml` as follows:

```yaml
spatial:
  sourceCrs: EPSG:4258
```

When filtering on a `Geometry` field, the CRS must match the `sourceCrs`. There is currently no support for reprojection of a `Geometry` field.