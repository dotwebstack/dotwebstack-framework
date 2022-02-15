# Core: Aliases

It is possible to make use of aliases for (multiple) fields when executing a query.

## Setup

No specific setup in the `dotwebstack.yaml` is needed to make use of aliases.

### Example 1:

An example with an ObjectList in the query.

Query:
```graphql
 query brewery {
    brewery (identifier: "d3654375-95fa-46b4-8529-08b0f777bd6b") {
        identifier
        smokybeers: beers(filter: {taste: {containsAnyOf: ["SMOKY"]}}){
            nodes{
                name
                taste
            }
        }
        fruityBeers: beers(filter: {taste: {containsAnyOf: ["FRUITY"]}}){
            nodes{
                name
                taste
            }
        }
    }
}
```
Result:
```json
{
  "data": {
    "brewery": {
      "identifier": "d3654375-95fa-46b4-8529-08b0f777bd6b",
      "smokybeers": {
        "nodes": [
          {
            "name": "Beer 2",
            "taste": [
              "MEATY",
              "SPICY",
              "SMOKY",
              "WATERY",
              "FRUITY"
            ]
          },
          {
            "name": "Beer 3",
            "taste": [
              "MEATY",
              "SMOKY",
              "SMOKY"
            ]
          }
        ]
      },
      "fruityBeers": {
        "nodes": [
          {
            "name": "Beer 1",
            "taste": [
              "MEATY",
              "FRUITY"
            ]
          },
          {
            "name": "Beer 2",
            "taste": [
              "MEATY",
              "SPICY",
              "SMOKY",
              "WATERY",
              "FRUITY"
            ]
          }
        ]
      }
    }
  }
}
```
### Example 2:

An example when using an aggregate:

Query:
```graphql
query beerAggregate {
  beer (identifier: "b0e7cf18-e3ce-439b-a63e-034c8452f59c"){
    name
    ingredientAgg{
      countWeightDis : count( field : "weight", distinct : true )
      countWeightDef : count( field : "weight" )
      countWeight : count( field : "weight", distinct : false )
    }
  }
}
```
Result:
```json
{
  "data": {
    "beer": {
      "name": "Beer 1",
      "ingredientAgg": {
        "countWeightDis": 5,
        "countWeightDef": 6,
        "countWeight": 6
      }
    }
  }
}
```
