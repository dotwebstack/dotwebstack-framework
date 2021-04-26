# Backend module: `backend-rdf4j`

## Setup

The following snippet from an `dotwebstack.yaml` file shows the configuration properties for an RDF4J backend:

```yaml
shape:
  graph: https://github.com/dotwebstack/beer/shapes
  prefix: https://github.com/dotwebstack/beer/shapes#
typeMapping:
  Beer:
    backend: rdf4j
    keys:
      - field: identifier
    fields:
      brewery:

  Brewery:
    backend: rdf4j
    keys:
      - field: identifier
    fields:
      identifier:
```

The `shape.graph` property is used to point to the graph that is used within the `shapes` file. The `shape.prefix` property is used to point to the prefix of the shapesfile. This prefix is ignored when mapping GraphQL types from `schema.graphqls` to the node shapes in the `shapes` file. 

Besides the configuration in the `dotwebstack.yaml` you need to add a `schema.graphqls` file that defines the GraphQL schema. The following example shows the GraphQL description for the Brewery type and a query description to retrieve all the breweries from the database:

```graphql
type Query {
  breweries(): [Brewery!]!
}

type Brewery {
  identifier: ID!
}
```

Now you need to write a configuration to translate your GraphQL types to an RDF4J (`sparql`) query. The RDF4J backend
uses a `shapes` file to define the mapping between the core GraphQL and the RDF4J compatible backend. An example of 
this mapping can be seen in the following `shapes` file, that contains a subset of brewery example:

```trig
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

<https://github.com/dotwebstack/beer/shapes> {
  beer_sh:Brewery a sh:NodeShape ;
    sh:class beer_def:Brewery ;
    sh:property
      beer_sh:Brewery_identifier 
  .
  
  beer_sh:Brewery_identifier a sh:PropertyShape ;
    sh:name "identifier" ;
    sh:path beer_def:identifier ;
    sh:minCount 1 ;
    sh:maxCount 1 ;
    sh:nodeKind sh:Literal ;
    sh:datatype xsd:string
  .
}
```

First of all, notice that the named graph in the shapes file `https://github.com/dotwebstack/beer/shapes` is the one 
that is referred to by the `shape.graph` property in the snippet from `application.yml`. Also notice that the uri from 
the `beer_sh:` prefix is the one that is referred to by the `shape.prefix` property in the `application.yml`. A GraphQL 
type from `schema.graphqls` is matched on a `node shape` in the `shapes` file after stripping the prefix from the name 
of the node shape. This means that the `Brewery` from the `schema.graphqls` file is mapped on the `beer_sh:Brewery` 
node shape. Fields from `schema.graphqls` file are mapped on property shapes from the 'shapes' file based on the 
'sh:name' property.

With these configuration files DotWebStack is now able to generate the RDF4J query to retrieve the identifier from the
Brewery from the data. In case of this example an in memory database is used that is created with the following data:

```trig
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix brewery_id: <https://github.com/dotwebstack/beer/id/brewery/> .
{
  brewery_id:1 a beer_def:Brewery ;
    beer_def:identifier "1" ;
  .
}
```

When the following query is executed:

```graphql
{
  breweries {
    identifier
  }
}
```

it will result in the following response:

```json
{
  "data": {
    "breweries": [
      {
        "identifier": "1"
      }
    ]
  }
}
```

### Repository configuration

We currently only supports the local in-memory RDF4J repository which can be loaded by placing a trig file within the model directory.

## Field selections

The RDF4J backend supports GraphQL field selections on any level within a graph. 

```graphql
{
  breweries { 
    name
    address { 
      postalCode
      streetAddress 
    }
  }
}
```

An `aggregationOf` field configuration is not supported

## Property paths

[W3C SHACL specification](https://www.w3.org/TR/shacl/#property-paths)

While it's possible to query object fields directly, SHACL property paths allows you to do a lot more. 
For our RDF4J backend we have built support for the paths described in this document. We will discuss the possibilities 
in the following paragraphs. 

### Predicate paths

The *predicate path* is the simplest of the paths. These are the building blocks for each property path. It describes one predicate 
to retrieve and object from the current subject. 
 
```trig
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Brewery a sh:NodeShape ;
    sh:class beer_def:Brewery ;
    sh:property
      beer_sh:Brewery_identifier 
  .

  beer_sh:Brewery_identifier a sh:PropertyShape ;
    sh:name "identifier" ;
    sh:path beer_def:identifier ;
    sh:minCount 1 ;
    sh:maxCount 1 ;
    sh:nodeKind sh:Literal ;
    sh:datatype xsd:string
  .
}
``` 

### Alternative paths

The *alternative path* describes the option get either the object from this predicate `OR` the object from that predicate.
You can chain as many paths as you like, and you can use both predicate paths and any of the other path constructions. 
   
```trig
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Brewery a sh:NodeShape ;
    sh:class beer_def:Brewery ;
    sh:property
      beer_sh:Brewery_name
  .
  
  beer_sh:Brewery_name a sh:PropertyShape ;
    sh:name "name" ;
    sh:path [sh:alternativePath ( schema:name beer_def:label ) ] ;
    sh:minCount 1 ;
    sh:maxCount 1 ;
    sh:nodeKind sh:IRI
  .
}
``` 

   
### Sequence paths

The *sequence path* describes the possibility to chain multiple predicates. You can use this to access the properties of 
underlying objects. For example when you are not interested in the parent per se, but more in his or her name you can use:

```trig
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Brewery a sh:NodeShape ;
    sh:class beer_def:Brewery ;
    sh:property
      beer_sh:Brewery_postalCode 
  .

 beer_sh:Brewery_postalCode a sh:PropertyShape ;
   sh:name "postalCode" ;
   sh:path ( schema:address schema:postalCode ) ;
   sh:minCount 1 ;
   sh:maxCount 1 ;
   sh:nodeKind sh:Literal ;
   sh:datatype xsd:string
 .
}

```

Again, this is the simple example. You can also use any of the other property paths described in this chapter inside a 
*sequence path*, this enables you to create constructions such as:

```shacl
([sh:alternativePath (ex:father ex:mother)] ex:firstname)
```

### Inverse paths

The *inverse path* traverses the predicate in the opposite direction. This can be useful when you only have an explicit relation
in one direction, but not the other. So in our previous examples that would mean that we have a relation from a child to a parent,
 but not from a parent to a child. In that case you can use the following inverse path to obtain the name of the child:
 
```trig
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Brewery a sh:NodeShape ;
    sh:class beer_def:Brewery ;
    sh:property
      beer_sh:Brewery_beers
  .

  beer_sh:Brewery_beers a sh:PropertyShape ;
    sh:name "beers" ;
    sh:path ( [ sh:inversePath beer_def:brewery ] schema:name ) ;
    sh:minCount 0 ;
    sh:nodeKind sh:Literal ;
    sh:datatype xsd:string
  .
}
```

In the previous example a couple of constructions are used. Again it is possible to use the other paths within an inverse path
as well.  

### OneOrMore path

The *one or more* path finds a connection between subjects and objects using the predicate, and matching the pattern one or more times.
For example, finding the names of all people a person knows either directly, or through another person using the `ex:knows` predicate:

```trig
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Beer a sh:NodeShape ;
    sh:class beer_def:Beer ;
    sh:property
      beer_sh:Beer_beerTypes
 .

 beer_sh:Beer_beerTypes a sh:PropertyShape ;
   sh:name "beerTypes" ;
   sh:path [ sh:oneOrMorePath beer_def:beertype ] ;
   sh:minCount 0 ;
   sh:nodeKind sh:Literal ;
   sh:datatype xsd:string
 .
}
```

### ZeroOrMore path

The *zero or more* path works in the same way as the 'one or more' path and allows paths of
length 0.

```trig
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Beer a sh:NodeShape ;
    sh:class beer_def:Beer ;
    sh:property
      beer_sh:Beer_beerTypes
 .

 beer_sh:Beer_beerTypes a sh:PropertyShape ;
   sh:name "beerTypes" ;
   sh:path [ sh:zeroOrMorePath beer_def:beertype ] ;
   sh:minCount 0 ;
   sh:nodeKind sh:Literal ;
   sh:datatype xsd:string
 .
}
```

### ZeroOrOne path

The *zero or one* path works in the same way as the 'zero or more' path, but allows paths of
length 0 or 1.

```shacl
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Beer a sh:NodeShape ;
    sh:class beer_def:Beer ;
    sh:property
      beer_sh:Beer_beerTypes
 .

 beer_sh:Beer_beerTypes a sh:PropertyShape ;
   sh:name "beerTypes" ;
   sh:path [ sh:zeroOrOnePath beer_def:beertype ] ;
   sh:minCount 0 ;
   sh:nodeKind sh:Literal ;
   sh:datatype xsd:string
 .
}
```

## Constraint

Constraints can be added to the shapes file to further restrict data from fitting as property or node shape constructs. 
Without these constraints it is only possible to restrict the model by the given paths, sometimes this is not enough.
Think about objects that have to be of a certain class to fit a nodeshape, or objects that need to have at least one 
predicate of a certain type, sometimes even with a certain value. In Dotwebstack each constraint will limit what will be
returned for a certain query, so that it applies to the rules specified in the shapes file. In the following paragraphs 
the constraints supported in Dotwebstack are discussed. 

### minCount

It is possible to add `sh:minCount` to a property shape, to add constraints to your query regarding required properties.
When adding minCount of 0 or omitting minCount altogether, the property is optional. When adding a minCount of
1, the property is required. Currently, minCount > 1 is not supported.

### hasValue

By adding `sh:hasValue` to a property shape, at least on of the triples in the data needs to have given predicate in 
combination with the given value in order to meet this requirement. 

### Class

By adding an `sh:class` to a node shape, you can bind your node shape to a class. This means that a constraint is added to the 
query that will constrain the returned object to have an `rdf:type` relation with the given class. Multiple classes will
result in multiple constraints and thus objects will have to have `rdf:type` relations with all given classes. By defining
an `sh:or` around multiple `sh:class` constraints, it is possible to state that only one of the constraints has to be met
in order for an object to fit the shape. 

## NodeShape inheritance

It is possible to let one or more `sub` NodeShapes inherit PropertyShapes of a `super` NodeShape:

```trig
ex:Pet a sh:NodeShape ;
  sh:class owl:Thing ;
  sh:name "Animal" ;
  sh:property
    ex:name,
    ex:owner
.

ex:Dog a sh:NodeShape ;
  sh:name "Dog";
  sh:class ex_def:Dog ;
  dws:inherits ex:Pet ;
  sh:property
    ex:fetchesBall ,
    ...
.

ex:Cat a sh:NodeShape ;
  sh:name "Cat";
  sh:class ex_def:Cat ;
  dws:inherits ex:Pet ;
  sh:property
    ex:breed ,
    ...
.
```

In the example the NodeShape `Dog` uses the `dws:inherits` (dws from \<http://www.dotwebstack.org/\>) to inherit the 
properties of `Pet`. This means that besides the `fetchesBall` property (whether or not it likes to fetch a ball) an 
instance of `Dog` also has a name and an owner. The NodeShape `cat` has a breed and because of the same inheritance 
relation with `Animal` also a name and an owner. 

## Geometry and Geography

WKT literals are supported.

Add `sh:datatype ogc:wktLiteral` to propertyShape in order for the conversion to take place. For an example implementation see: `example/example-rdf4j`.

Example:

```trig
beer_sh:Brewery_geometry a sh:PropertyShape ;
  sh:path ogc:asWKT ;
  sh:name "geometry" ;
  sh:minCount 0 ;
  sh:maxCount 1 ;
  sh:nodeKind sh:Literal ;
  sh:datatype ogc:wktLiteral
```
