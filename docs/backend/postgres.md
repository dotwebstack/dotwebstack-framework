# Backend module: `backend-postgres`

## Setup

```yaml
  Beer:
    table: dbeerpedia.beers
    fields:
      identifier:
        type: ID
      name:
        type: String
      status:
        type: String
      geometry:
        type: Geometry
        nullable: true
      beers:
        type: Beer
        list: true
        nullable: true
        mappedBy: brewery
      beerAgg:
        aggregationOf: Beer
        mappedBy: brewery
```

### joinColumns

The `joinColumns` field configuration property contains an array of `joinColumn` objects.
An `joinColumn` object indicates that a given column in the owner entity refers to a primary key in
the reference entity:

```yaml
objectTypes:
  Beer:
    table: dbeerpedia.beers
    fields:
      identifier:
        type: ID
      brewery:
        type: Brewery
        joinColumns:
          - name: brewery
            referencedField: identifier

  Brewery:
    table: dbeerpedia.breweries
    fields:
      identifier:
        type: ID
```

The above configuration example will use a foreign key linking the *Beer* entity with the primary
key from the *Brewery*
entity. The name of the foreign key column in the *Brewery* entity is specified by name property.

### mappedBy

Once we have defined the owning side of the relationship, DotWebStack already has all the
information it needs to map that relationship in our database. To make this association
bidirectional, all we'll have to do is to define the referencing side. The inverse or the
referencing side simply maps to the owning side.

We can easily use the `mappedBy` configuration property to do so. So, let's define it:

```yaml
objectTypes:
  Beer:
    table: dbeerpedia.beers
    fields:
      identifier:
        type: ID
      brewery:
        type: Brewery
        joinColumns:
          - name: brewery
            referencedField: identifier

  Brewery:
    table: dbeerpedia.breweries
    fields:
      identifier:
        type: ID
      beers:
        type: Beer
        list: true
        nullable: true
        mappedBy: brewery
```

Here, the value of mappedBy is the name of the association-mapping field on the owning side. With
this, we have now established a bidirectional association between our *Brewery* and *Beer* entities.

### joinTable

An `joinTable` field configuration property can be used to make a many-to-many relation with a
jointable.

```yaml
objectTypes:
  Beer:
    table: dbeerpedia.beers
    fields:
      identifier:
        type: ID
      ingredients:
        type: Ingredient
        list: true
        joinTable:
          name: dbeerpedia.beers_ingredients
          joinColumns:
            - name: beers_identifier
              referencedField: identifier
          inverseJoinColumns:
            - name: ingredients_identifier
              referencedField: identifier

  Ingredient:
    table: dbeerpedia.ingredients
    fields:
      identifier:
        type: ID
```

This association has two sides i.e. the owning side and the inverse side. In our example, the owning
side is *Beer* so the join table is specified on the owning side by using the *joinTable* annotation
in *Beer* class.

### aggregationOf

An `aggregationOf` field configuration can be used to aggregate a type with a many-to-many or
one-to-many relation. The `mappedBy` or `joinTable` configuration needs to be included.

Simplified configuration example:

```yaml
  Brewery:
    fields:
      beers:
        type: Beer
        list: true
        nullable: true
        mappedBy: brewery
      beerAggregation:
        aggregationOf: Beer
        mappedBy: brewery

  Beer:
    fields:
      ingredients:
        type: Ingredient
        list: true
        joinTable:
          name: dbeerpedia.beers_ingredients
          joinColumns:
            - name: beers_identifier
              referencedField: identifier
          inverseJoinColumns:
            - name: ingredients_identifier
              referencedField: identifier
      ingredientAggregation:
        aggregationOf: Ingredient
        joinTable:
          name: dbeerpedia.beers_ingredients
          joinColumns:
            - name: beers_identifier
              referencedField: identifier
          inverseJoinColumns:
            - name: ingredients_identifier
              referencedField: identifier
```

### Nested objects using parent table

It is possible to store two objects with a 1:1 relation in the same table by omitting the `table`
property of the nested object.

Note that it is possible to define one nested object and reuse it in multiple type definitions. The
correct parent table will be used in every instance assuming each parent table contains the columns
needed for the nested object.

data example:

```json
{
  "brewery": {
    "name": "Alfa",
    "history": {
      "age": 1900,
      "history": "A long and glorious fairytale"
    }
  }
}
```

Simplified configuration example

```yaml
  Brewery:
    table: db.brewery
    fields:
      name:
        type: String
      history:
        type: History

  History:
    fields:
      age:
        type: Int
      history:
        type: String

  Beer:
    table: db.beer
    fields:
      name:
        type: String
      history:
        type: History
```

### Context fields

It is optional to define context fields. Context fields are common to all objects within the query.
A practical application for context fields is time traveling within a bi-temporal datamodel.

If there are context fields defined in the configuration, data for each object will be retrieved
with an context table function named `<table>_ctx` with the context parameters in natural order.

Example configuration:

```yaml
context:
  fields:
    validOn:
      type: Date
      default: NOW
    availableOn:
      type: DateTime
      default: NOW

queries:
  beers:
    type: Beer
    list: true
  beer:
    type: Beer
    keys:
      - identifier_beer
  breweries:
    type: Brewery
    list: true

objectTypes:
  Beer:
    table: db.beer_v
    fields:
      identifier_beer:
        type: ID
      name:
        type: String
```

With this configured context within the `dotwebstack.yaml` you need to create the following table
function:

```sql
CREATE FUNCTION db.beer_v_ctx(date,timestamp with time zone) RETURNS SETOF db.beer_v AS $$
   SELECT * FROM db.beer_v WHERE daterange(valid_start, valid_end) @> $1 and tstzrange(available_start, available_end) @> $2
$$ language SQL immutable;
```

### Spatial
When the Extension module: `ext-spatial` is enabled it is possible to add next to the default `ext-spatial` configuration
extra postgres config for Geometry column mapping.

The property `columnSuffix` can be used to accomplish this. Every Geometry field will have a suffix concatenated for the
corresponding srid.

The same is possible for bounding boxes. Default behaviour is runtime calculation, but it is also possible to store the 
bounding box in a column. The property `bboxColumnSuffix` can be used to accomplish this. Every Geometry field will have 
a suffix concatenated for the corresponding srid.

Example configuration:

```yaml
spatial:
  srid:
    28992:
      dimensions: 2
      precision: 4
      bboxColumnSuffix: _bbox
    7415:
      dimensions: 3
      precision: 4
      equivalent: 28992
      bboxColumnSuffix: _bbox
    9067:
      dimensions: 2
      precision: 9
      columnSuffix: _etrs89
    7931:
      dimensions: 3
      precision: 9
      columnSuffix: _etrs89
      equivalent: 9067
```

### Text search (TSVECTOR)

When a filter for a `String` field is configured with a `term` type DotWebStack will create an SQL condition for this filter against a TSVECTOR column. The TSVECTOR column can be configured with the optional `tsvColumn` property. Default value: `{$fieldname}_tsv`

```yaml
objectTypes:
  Beer:
    table: db.beer_v
    fields:
      name:
        type: String
        tsvColumn: name_TSVECTOR
```

## PostGIS

Geometry and Geography types, as part of the [PostGIS extension](https://postgis.net), are
supported.

For an example implementation,
see [example/example-postgres](https://github.com/dotwebstack/dotwebstack-framework/tree/v0.3/example/example-postgres)
.

## Connection configuration

The PostgreSQL connection properties are included in the spring `application.yml`. These are the
default values:

```yaml
dotwebstack:
  postgres:
    host: localhost
    port: 5432
    username: postgres
    password: postgres
    database: postgres
    sslMode: disable
    options: {}         # Additional PostgreSQL options, e.g. { enable_seqscan: 'off' }
    pool:
      initialSize: 10
      maxSize: 100
      maxIdleTime: 30
```
