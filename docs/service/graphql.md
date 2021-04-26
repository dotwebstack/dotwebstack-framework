# Service module: `service-graphql`

This service can be used to expose the internal GraphQL service as a web service.

The graphql service can be included in a Spring Boot project with the following dependency:

```xml
<dependency>
  <groupId>org.dotwebstack.framework</groupId>
  <artifactId>service-graphql</artifactId>
</dependency>
```

# Types

## Geometry

For information about Geometry type see [ext-spatial readme](ext/ext-spatial/README.md).

## Aggregate

Aggregate is a predefined GraphQL Object type see your corresponding backend readme about configuration details.

`stringJoin` can be used for data-types: `text`, `varchar`, `varchar[]` and `text[]`.

```graphql
type Aggregate {
  count(field: String!, distinct: Boolean = false): Int!
  stringJoin( field : String!, distinct : Boolean = false ): String!

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

The backend configuration should define `ingredientAgregation` as an `aggregationOf: ingredients`

Example schema:

```graphql
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

Example query:

```graphql
{
  beers {
    name 
    ingredientAgregation {
      totalWeight: intSum(field: "weight")
      averageWeight: intAvg(field: "weight")
      maxWeight: intMax(field: "weight")
    }
  }
}
````
