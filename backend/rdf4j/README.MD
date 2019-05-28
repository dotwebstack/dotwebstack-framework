# 1. Backend-Rdf4j

## 1.1 Setup

The following snippet from an `application.yml` file shows the configuration properties for an Rdf4j backend:

```yml
dotwebstack:
  Rdf4j:
    shape:
      graph: https://github.com/dotwebstack/beer/shapes
      prefix: https://github.com/dotwebstack/beer/shapes#
    prefixes:
      rdf: http://www.w3.org/1999/02/22-rdf-syntax-ns#
      beer_def: https://github.com/dotwebstack/beer/def#
      brewery_id: https://github.com/dotwebstack/beer/id/brewery/
```

`dotwebstack.Rdf4j` is reserved by the Rdf4j backend as its properties location. The `shape.graph` property is used to 
point to the graph that is used within the `shapes.trig` file. The `shape.prefix` property is used to point to the prefix
of the shapesfile. This prefix is ignored when mapping GraphQL types from the `schema` to the node shapes in the
`shapes` file. Within the `prefixes` property you can define a list of general prefixes that is added to the 
[QueryEnvironment](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/backend/Rdf4j/src/main/java/org/dotwebstack/framework/backend/Rdf4j/query/QueryEnvironment.java). 

Besides the configuration in the `application.yml` you need to add a `schema` file that defines the GraphQL schema. 
The following example shows the GraphQL description for the Brewery type and a query description to retrieve all the 
breweries from the database:

```graphql
type Query {
  breweries(): [Brewery!]!
}

type Brewery {
  identifier: ID!
}
```

Now you need to write a configuration to translate your GraphQL types to an Rdf4j (`sparql`) query. The Rdf4j backend
uses a `shapes` file to define the mapping between the core GraphQL and the Rdf4j compatible backend. An example of 
this mapping can be seen in the following `shapes.trig` file, that contains a subset of brewery example:

```shacl
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

<https://github.com/dotwebstack/beer/shapes> {
  beer_sh:Brewery a sh:NodeShape ;
    sh:targetClass beer_def:Brewery ;
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
type from `schema` is matched on a `node shape` in the `shapes` file after stripping the prefix from the name 
of the node shape. This means that the `Brewery` from the `schema` file is mapped on the `beer_sh:Brewery` 
node shape. Fields from `schema` file are mapped on property shapes from the `shapes` file.

With these configuration files DotWebStack is now able to generate the Rdf4j query to retrieve the identifier from the
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
  breweries{identifier}
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

## 1.2 Field selections

The Rdf4j backend supports GraphQL field selections on any level within a graph. 

```graphql
{
  breweries { 
    name, 
    address { 
      postalCode, 
      streetAddress 
    }
  }
}
```

## 1.3 Paging parameters

Within the `@sparql` directive it is possible to declare a `limit` and an `offset` parameter. Arguments for both of 
these parameters are treated as [jexl](http://commons.apache.org/proper/commons-jexl/) expressions. The results of 
both expressions need to be an integer and it is validated that `limit > 1` and `offset > 0`. The resulting values
of these parameters are used as paging parameters in the resulting sparql query.  

## 1.4 Sorting

Within the `@sparql` directive it is possible to declare an `orderBy` parameter. The argument of this parameter needs to 
be one or any array of objects containing a `field` and an `order` parameter. The `field` parameter needs to be
a string and the `order` parameter is an `enum` and needs to be either `ASC` or `DESC`. The resulting values of these 
parameters are in the order by clause in the resulting sparql query.  

## 1.5 Property paths

[W3C SHACL specification](https://www.w3.org/TR/shacl/#property-paths)

While it's possible to query object fields directly, SHACL property paths allows you to do a lot more. 
For our RDF4J backend we have built support for the paths described in this document. We will discuss the possibilities 
in the following paragraphs. 

### 1.5.1 Predicate paths
The *predicate path* is the simplest of the paths. These are the building blocks for each property path. It describes one predicate 
to retrieve and object from the current subject. 
 
```shacl
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Brewery a sh:NodeShape ;
    sh:targetClass beer_def:Brewery ;
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

### 1.5.2 Alternative paths
The *alternative path* describes the option get either the object from this predicate `OR` the object from that predicate.
You can chain as many paths as you like, and you can use both predicate paths and any of the other path constructions. 
   
```shacl
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Brewery a sh:NodeShape ;
    sh:targetClass beer_def:Brewery ;
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

   
### 1.5.3 Sequence paths
The *sequence path* describes the possibility to chain multiple predicates. You can use this to access the properties of 
underlying objects. For example when you are not interested in the parent per se, but more in his or her name you can use:

```shacl
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Brewery a sh:NodeShape ;
    sh:targetClass beer_def:Brewery ;
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

### 1.5.4 Inverse paths
The *inverse path* traverses the predicate in the opposite direction. This can be useful when you only have an explicit relation
in one direction, but not the other. So in our previous examples that would mean that we have a relation from a child to a parent,
 but not from a parent to a child. In that case you can use the following inverse path to obtain the name of the child:
 
```shacl
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Brewery a sh:NodeShape ;
    sh:targetClass beer_def:Brewery ;
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

### 1.5.5 OneOrMore path
The *one or more* path finds a connection between subjects and objects using the predicate, and matching the pattern one or more times.
For example, finding the names of all people a person knows either directly, or through another person using the `ex:knows` predicate:

```shacl
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Beer a sh:NodeShape ;
    sh:targetClass beer_def:Beer ;
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

### 1.5.6 ZeroOrMore path
The *zero or more* path works in the same way as the 'one or more' path and allows paths of
length 0.

```shacl
@prefix sh: <http://www.w3.org/ns/shacl#> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix schema: <http://schema.org/> .
@prefix beer: <https://github.com/dotwebstack/beer/> .
@prefix beer_def: <https://github.com/dotwebstack/beer/def#> .
@prefix beer_sh: <https://github.com/dotwebstack/beer/shapes#> .

beer:shapes {
  beer_sh:Beer a sh:NodeShape ;
    sh:targetClass beer_def:Beer ;
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

### 1.5.7 ZeroOrOne path
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
    sh:targetClass beer_def:Beer ;
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
