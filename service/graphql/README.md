# 1 graphql
This service can be used to expose the internal GraphQL service as a web service.

The graphql service can be included in a Spring Boot project with the following dependency:
```xml
    <dependency>
        <groupId>org.dotwebstack.framework</groupId>
        <artifactId>service-graphql</artifactId>
    </dependency>
```

# 2 default types

## 2.1 Geometry

For information about Geometry type see [ext-spatial readme](ext/ext-spatial/README.md).

## 2.2 Aggregation

Aggregation is a predefined GraphQL Object type see your corresponding backend readme about configuration details.

`stringJoin` can be used for data-types: `text`, `varchar`, `varchar[]` and `text[]`.

```
  type Aggregate {
    count(field: String!, distinct: Boolean = false): Int!
    stringJoin( field : String!, distinct : Boolean = false ): Int!

    intSum(field: String!): Int
    intMin(field: String!): Int
    intMax(field: String!): Int
    intAvg(field: String!): Int

    floatSum(field: String!): Float
    floatMin(field: String!): Float
    floatMax(field: String!): Float
    floatAvg(field: String!): Float
  }
```

Example schema.graphqls  
The backend configuration should define `ingredientAgregation` as an `aggregationOf: ingredients`
```
  type Beer {
    identifier: ID!
    name: String!
    ingredients: [Ingredient!]!
    ingredientAgregation: Aggregate!
  }
  
  type Ingredient {
    identifier: ID!
    name: String!
    weight: Float!
  }
```

example query:
````
  { beers {
    name 
    ingredientAgregation{
      totalWeight : intSum( field : "weight" )
      averageWeight : intAvg( field : "weight" )
      maxWeight : intMax( field : "weight" )
    }
  }
````