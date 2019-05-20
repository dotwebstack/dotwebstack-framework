# backend-rd4j

## Field selections

The RDF4J backend supports GraphQL field selections on any level within a graph. 

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

## Property paths

[W3C SHACL specification](https://www.w3.org/TR/shacl/#property-paths)

While it's possible to query object fields directly, SHACL property paths allows you to do a lot more. 
For our RDF4J backend we have built support for the paths described in this document. We will discuss the possibilities 
in the following paragraphs. 

### Predicate paths
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

### Alternative paths
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

   
### Sequence paths
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

### Inverse paths
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

### OneOrMore path
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

### ZeroOrMore path
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
