# Core: Operations

In the `dotwebstack.yaml` it is possible to define two types of operations, `subscriptions` and `queries`.
Some basic information about `subscriptions` and `queries` can be found at:
[GraphQl.org/learn: Queries and Mutations](https://spring.io/projects/spring-boot),

```yaml
subscriptions:
  beerSubscription:
    type: Beer
  brewerySubscription:
    type: Brewery

queries:
  beer:
    type: Beer
    keys:
      - identifier
  beerCollection:
    type: Beer
    list: true
  brewery:
    type: Brewery
    keys:
      - identifier
  breweryCollection:
    type: Brewery
    list: true
```

Queries and/or subscriptions exist of name, like `beer` or `brewery` in the example above, and can have the following properties:
- type
- list
- pageable
- keys

## Type

`Type` is mandatory and has the value of the object you want as a result e.g. `Beer` for `beer` and `Brewery` for `brewery`.

## List

`List` is not mandatory and if not provided it resolves to `false`. <br>
As the name suggest, it is used to get a list of items for a specific `Type`.<br>
Can't be used in combination with `Keys`.<br>

## Pageable

`Pageable` can only be used in combinaton with `List`. <br>
See [Paging](https://dotwebstack.org/dotwebstack-framework/#/core/paging) for more information about how paging works.

## Keys

`Keys` are used in queries to get one specific object for `Type`. <br>
They are provided as a list of items.<br>

```yaml
  breweryAddress:
  type: Brewery
    keys:
      - identifier
      - name
```

A query for this example wil result in:
```graphql
query {
    breweryAddress(identifier: "id-1", name: "breweryName") {
        identifier
        name
    }
}
```

It is also possible to provide a key from a (nested) object.<br>
The level of nested keys is supported up to 1 level deep, e.g.: 
- `postalAddress.city` is supported
- `field.postalAddres.city` is not supported

For a nested key the last entry of the key wil be used as the query parameter, for example:
```yaml
  breweryAddress:
  type: Brewery
    keys:
      - identifier
      - name
```
Will; result in the following query:

```graphql
query {
    breweryAddress(identifier: "id-1", city: "Dublin") {
        identifier
        name
    }
}
```
