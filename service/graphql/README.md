# 1 graphql
This service can be used to expose the internal GraphQL service as a web service.

The graphql service can be included in a Spring Boot project with the following dependency:
```xml
    <dependency>
        <groupId>org.dotwebstack.framework</groupId>
        <artifactId>service-graphql</artifactId>
    </dependency>
```

## 1.1 Schema type definitions

### 1.1.1 Geometry & GeometryType

Geometry is a predefined GraphQl Object Type and GeometryType an Enum type. 

Do NOT include these in your `schema.graphqls` configuration.

Please check backend documentation for prerequisites and information about data storage.

```
type Geometry{
  type: GeometryType
  asWKB: String
  asWKT: String
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
