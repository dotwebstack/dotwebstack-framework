# 1. Backend-Postgres

## 1.1 Type configuration

```yaml
  Beer:
    backend: postgres
    table: dbeerpedia.beers
    keys:
      - field: identifier
```

## 1.2 Field configuration

### 1.2.1 joinColumns

The `joinColumns` field configuration property contains an array of `joinColumn` objects. An `joinColumn` object indicates that a given column in the owner entity refers to a primary key in the reference entity:

```yaml
typeMapping:
  Beer:
    backend: postgres
    table: dbeerpedia.beers
    keys:
      - field: identifier
    fields:
      brewery:
        joinColumns:
          - name: brewery
            referencedField: identifier
            
  Brewery:
    backend: postgres
    table: dbeerpedia.breweries
    keys:
      - field: identifier
    fields:
      identifier:    
```
The above configuration example will use a foreign key linking the *Beer* entity with the primary key from the *Brewery* entity. The name of the foreign key column in the *Brewery* entity is specified by name property.

### 1.2.2 mappedBy

Once we have defined the owning side of the relationship, DotWebStack already has all the information it needs to map that relationship in our database. To make this association bidirectional, all we'll have to do is to define the referencing side. The inverse or the referencing side simply maps to the owning side.

We can easily use the `mappedBy` configuration property to do so. So, let's define it:
```yaml
typeMapping:
  Beer:
    backend: postgres
    table: dbeerpedia.beers
    keys:
      - field: identifier
    fields:
      brewery:
        joinColumns:
          - name: brewery
            referencedField: identifier
            
  Brewery:
    backend: postgres
    table: dbeerpedia.breweries
    keys:
      - field: identifier
    fields:
      beers:
        mappedBy: brewery
```
Here, the value of mappedBy is the name of the association-mapping field on the owning side. With this, we have now established a bidirectional association between our *Brewery* and *Beer* entities.

### 1.2.3 joinTable

An `joinTable` field configuration property can be used to make a many-to-many relation with a jointable. 

```yaml
typeMapping:
  Beer:
    backend: postgres
    table: dbeerpedia.beers
    keys:
      - field: identifier
    fields:
      ingredients:
        joinTable:
          name: dbeerpedia.beers_ingredients
          joinColumns:
            - name: beers_identifier
              referencedField: identifier
          inverseJoinColumns:
            - name: ingredients_identifier
              referencedField: identifier

  Ingredient:
    backend: postgres
    table: dbeerpedia.ingredients
    keys:
      - field: identifier
```
This association has two sides i.e. the owning side and the inverse side. In our example, the owning side is *Beer* so the join table is specified on the owning side by using the *joinTable* annotation in *Beer* class.

### 1.2.4 aggregationOf

An `aggregationOf` field configuration can be used to aggregate a field in a many-to-many or one-to-many relation as defined by `mappedBy` or `joinTable` field configuration.

simplified configuration example:
```yaml
  Brewery:
    fields:
      beers:
        mappedBy: brewery
      beerAggregation:
        aggregationOf: beers

  Beer:
    fields:
      ingredients:
        joinTable:
          name: dbeerpedia.beers_ingredients
          joinColumns:
            - name: beers_identifier
              referencedField: identifier
          inverseJoinColumns:
            - name: ingredients_identifier
              referencedField: identifier
      ingredientAggregation:
        aggregationOf: ingredients
```

## 1.3 Geometry and Geography 

Geometry and Geography types are supported, please use [PostGIS extension](https://postgis.net) for this to work.

For an example implementation, see [example/example-postgres](example/example-postgres).
