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
  srid: Int!
  asWKB: String!
  asWKT: String!
  asGeoJSON: String!
}

enum GeometryType{
  POINT
  LINESTRING
  POLYGON
  MULTIPOINT
  MULTILINESTRING
  MULTIPOLYGON
  GEOMETRYCOLLECTION
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

### Geometry repojection

It is possible to reproject to another spatial reference system. For reprojection see the configuration below and 
different backends for support.

Query example:

```graphql
{
  breweries {
    identifier
    name
    geometry(srid: 28992) {
      type
      srid
      asWKT
    }
  }
}
```

### Geometry bounding box

It is possible to retrieve the bounding box for a geometry. The bounding box will be calculated on runtime. See the 
different backends for support to retrieve the bbox from a storage.

```graphql
{
  breweries {
    identifier
    name
    geometry(bbox: true) {
      type
      srid
      asWKT
    }
  }
}
```

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
  fromWKT: String
  fromWKB: String
  fromGeoJSON: String
}
```

Example GraphQL query which filters on a `Geometry` field with the `contains` filter operation:

```graphql
query {
    breweries(filter: {geometry: {contains: {fromWKT: "POINT(1 1)"}}}) {
        identifier
        name
    }
}
```

## Configuration

In the `dotwebstack.yml` every supported srid can be configured. Example configuration:

```yaml
spatial:
  srid:
    28992:
      dimensions: 2
      scale: 4
    7415:
      dimensions: 3
      scale: 4
      equivalent: 28992
```

A three dimensional geometry can be reprojected to a two dimensional geometry when the property `equivalent` is configured.

The scale of a GeoJSON can be configured with the `scale` property. 
