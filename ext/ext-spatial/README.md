# 1 Ext-Spatial
This extension can be used to include spatial functionality into a backend.

The spatial extension can be included in a Spring Boot project with the following dependency:
```xml
    <dependency>
        <groupId>org.dotwebstack.framework</groupId>
        <artifactId>ext-spatial</artifactId>
        <version>${project.version}</version>
    </dependency>
```

## 1.1 Schema type definitions

### 1.1.1 Geometry & GeometryType

Geometry is a predefined GraphQl Object Type and GeometryType an Enum type.

```
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
```
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