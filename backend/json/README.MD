# 1. Backend-Json

## 1.1 Setup

The following snippet from an `dotwebstack.yaml` file shows the configuration properties for an json backend:


```yaml
typeMapping:
  Beer:
    backend: json
    path: "$..beer"
    keys:
      - field: identifier
    fields:
      identifier:
      name:
```

You need to add a `schema.graphqls` file that defines the GraphQL schema. 

The following example shows the GraphQL description for the Beer type and the query descriptions to retrieve all the 
beers or an single beer from a json file:

```graphql
type Query {
  beers: [Beer!]!
  
  beer(identifier: ID! @key(field : "identifier")): Beer
}
```