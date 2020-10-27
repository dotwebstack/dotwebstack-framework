# 1. Backend-Json

## 1.1 Setup

There are no configuration properties for the `application.yml` 

You need to add a `schema.graphqls` file that defines the GraphQL schema. 

The following example shows the GraphQL description for the Beer type and the query descriptions to retrieve all the 
beers or an single beer from a json file:

```graphql
type Query {
  beers: [Beer!]!
  @json(file: "data.json", path: "$.beers")
  
  beer(identifier: ID! @constraint(pattern: "[0-9]+") @predicate(property: "identifier")): Beer
  @json(file: "data.json", path: "$.beers[?]")
}
```

## 1.2 Json directive

The Json backend introduces the `@json` directive which can be added to a query in the `schema.graphqls` file. It allows 
you to specify an json file and path expression for this specific query. 


## 1.3 Predicate directive

It is possible to use predicates within a jsonpath expression using the `?` character. The `@predicate` directive can be added to an graphql query argument. For example:

```graphql
type Query {
  beer(identifier: ID! @constraint(pattern: "[0-9]+") @predicate(property: "identifier")): Beer
  @json(file: "data.json", path: "$.beers[?]")
}
```

The `@predicate` directive takes one optional argument, the `property` argument. The `property` argument
sets the matching json property name to which the filter is applied. If this directive argument is not set the graphql query argument name is used.