# 1. Backend-Rdf4j

## 1.1 Setup

The following snippet from an `application.yml` file shows the configuration properties for an Rdf4j backend:

```yml
dotwebstack:
  rdf4j:
    shape:
      graph: https://github.com/dotwebstack/beer/shapes
      prefix: https://github.com/dotwebstack/beer/shapes#
    prefixes:
      rdf: http://www.w3.org/1999/02/22-rdf-syntax-ns#
      beer_def: https://github.com/dotwebstack/beer/def#
      brewery_id: https://github.com/dotwebstack/beer/id/brewery/
```

`dotwebstack.Rdf4j` is reserved by the Rdf4j backend as its properties location. The `shape.graph` property is used to 
point to the graph that is used within the `shapes` file. The `shape.prefix` property is used to point to the prefix
of the shapesfile. This prefix is ignored when mapping GraphQL types from `schema.graphqls` to the node shapes in the
`shapes` file. Within the `prefixes` property you can define a list of general prefixes that is added to the 
[QueryEnvironment](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/backend/rdf4j/src/main/java/org/dotwebstack/framework/backend/rdf4j/query/QueryEnvironment.java). 

Besides the configuration in the `application.yml` you need to add a `schema.graphqls` file that defines the GraphQL schema. 
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
this mapping can be seen in the following `shapes` file, that contains a subset of brewery example:

```shacl
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

### 1.1.1 Repository configuration
Two types of repositories are distinguished within the rdf4j backend domain. An inmemory one, referred to as `local` 
and `external` ones. `local` is an inmemory database filled on startup with the `.trig` files from under the `config/model`
folder. A query can point to this repository by specifying the local repository in the @sparql directive:
 
```graphql
type Query {
  breweries(): [Brewery!]!
  @sparql(
    repository: local
  )
}
```

External repositories can be configured in the `application.yml` file under `dotwebstack.rdf4j.repositories`: 
```yml
dotwebstack:
  rdf4j:
    repositories:
      beerRepository:
        type: sparql
        args:
          endpointUrl: http://...
```

A query can point to this repository by specifying the repository name in the @sparl directive:

```graphql
type Query {
  breweries(): [Brewery!]!
  @sparql(
    repository: beerRepository
  )
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

## 1.3 Sparql directive

The Rdf4j backend introduces the `@sparql` directive which can be added to a query in the `schema.graphqls` file. It allows 
you to specify and assign paging and sort parameters and the specific query to a repository. You can optional specify a `distinct` parameter with the value `true` to enforce distinct results.

## 1.4 Paging parameters

Within the `@sparql` directive it is possible to declare a `limit` and an `offset` parameter. Arguments for both of 
these parameters are treated as [jexl](http://commons.apache.org/proper/commons-jexl/) expressions and should evaluate 
to an integer. The resulting limit should be greater than 1 and the resulting offset should be greater than 0. The 
resulting values of these parameters are used as paging parameters in the resulting sparql query.  

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

## 1.6 Filter directive
It is possible to filter results by setting a *filter* directive on a GraphQL query. 
You can do this by adding the `@filter` keyword to an argument on a GraphQL Query. For example: 

```graphql
type Query {
  breweries(filterName: String @filter(field: "name", operator: "=")): [Brewery!]!
}
```

The `@filter` directive takes two arguments, the `field` and the `operator` argument. The `field` argument
sets the field to which the filter is applied, in the example above this is the `name` property of the `Brewery`. 
If the directive is assigned to a nonexistent field, an exception will be thrown at compile time. The `operator` 
argument is used to describe the filter condition that is used to set the relation between the field and the value provided
by the user. The following operators are currently supported: `=`, `!=`, `<`, `<=`, `>`, `>=`, `contains` and `like`. It is possible 
to omit the `operator` argument, in that case the default value (`=`) will be used.

The relational operators are self-explanatory, below the `contains` and `like` operators are further explained:
The `contains` operator indicates a case-sensitive search for any of the specified words or phrases (as a substring) within a particular `field`.
The `like` operator indicates a case-insensitive search for any of the specified words or phrases (as a substring) within a particular `field`.

It is also possible to add a filter to a nested input object, as illustrated in the example below. While the example
shows a nesting of only one layer, there is currently no restriction in how deep you can nest your `@filter` directives. 
```graphql
type Query {
  breweries(filter: Filter): [Brewery!]!
}

input Filter {
  filterName: String @filter(field: "name", operator: "=")
}
```

It is possible to combine multiple filters on the same field, as illustrated in the example below. Combining these 
filters results in a combined filter, joined together with the `AND` keyword. The resulting query will return the 
`Breweries` founded between the `foundedFrom` and `foundedTo` arguments.

```graphql
type Query {
  breweries(foundedFrom: String @filter(field: "founded", operator: ">"),
            foundedTo: String @filter(field: "founded", operator: "<")): [Brewery!]!
}
```

Another possibility is to use an array type on a filter argument, as can be seen in the example below:

```graphql
type Query {
  breweries(nameIn: [String] @filter(field: "name", operator: "=")): [Brewery!]!
}
``` 

The names that are provided in the `nameIn` argument are used in a filter where the arguments are combined with `OR`'s; the resulting query will
return all the breweries that match one of the provided names. 

### 1.6.1 Nested filters
In the examples in the previous paragraph the filtering was done on the root level of the query. The filters were 
applied to direct fields of a brewery and they only had effect on the breweries that were returned. It is, however, 
also possible to query on and filter indirect properties of the root element. 

```graphql
type Query {
  breweries(beerName: [String!] @filter(field: "beers.name", operator: "=")): [Brewery]!
}

type Brewery {
  beers: [Beer!]
}

type Beer {
  name: String
}
```

The example above shows a way to filter the breweries on the existence of the given beer name. A brewery that does not
have a beer with the given name, will not be returned in the result set. Note the dotted notation that is used to point 
to the name of a beer. This dotted notation makes it possible to point a filter to an indirect property.  

It is also possible to filter a subset of the result set. For example, if you only want to have the beers with a certain 
name in your result set. The set of breweries itself is not filtered, only the set of beers under each brewery:

```graphql
type Query {
  breweries: [Brewery]!
}

type Brewery {
  beers(name: [String!] @filter): [Beer!]
}

type Beer {
  name: String
}
```

### 1.6.2 Language filters
Filtering can be applied to all sorts of datatypes. One special case is the language string, a string with a certain
language applied to it:

```
beer_brewery:1 beer_def:localName "Heineken Nederland"@nl, "Heineken Niederlande"@de, "Heineken Netherlands"@en;
```

The the example above shows a brewery that has different names in different languages. Two types of filtering are
possible in this case, one where the breweries are filtered on a localName in a certain language and one where the 
local names themselves are filtered on the preferred language. In both cases `sh:datatype rdf:langString` has to be 
assigned to the property shape belonging to `beer_def:localName`  

For this to work a application wide language should be specified in the `application.yml` file
```yml
dotwebstack:
  rdf4j:
    shape:
      language: nl
``` 

## 1.7 sh:or 
It is possible to have objects of multiple classes connected to the same subject with the same predicate. 
A `beer` can have both `ingredients` and `supplements` as objects of the `ingredient` predicate:

```trig
  beer_id:1 a beer_def:Beer ;
    beer_def:ingredient ingredient_id:1 ;
    beer_def:ingredient ingredient_id:2 ;
    beer_def:ingredient supplement_id:1
  .
```

While the type of the object is different (`supplement` versus `ingredient`), the relation used to map the object is in 
both cases the same. This poses a problem, because while the mapping in sparql and rdf usually depends on the predicate,
in GraphQL it depends on the type of the object:

```graphql
type Beer {
  ingredients: [Ingredient!]
  supplements: [Supplement!]
}
```

In this case it is impossible to retrieve just the supplements or the ingredients using `sh:path`. The `sh:or` construct 
can be used to address this issue by defining multiple property shapes that can each point to a different node shape:

```shacl
sh:property [
  sh:path ( beer_def:ingredient ) ;
  sh:or (
    [
      sh:name "ingredients" ;
      sh:node beer_sh:Ingredient ;
    ]
    [
      sh:name "supplements" ;
      sh:node beer_sh:Supplement ;
    ]
  )
]
``` 

This construct makes it possible to have two property shapes with the same `sh:path` and each pointing to different node shape. 
Because the naming for both of them is different it is possible to point to each one individually from `schema.graphqls`.

## 1.8 Constraints
Constraints can be added to the shapes file to further restrict data from fitting as property or node shape constructs. 
Without these constraints it is only possible to restrict the model by the given paths, sometimes this is not enough.
Think about objects that have to be of a certain class to fit a nodeshape, or objects that need to have at least one 
predicate of a certain type, sometimes even with a certain value. In Dotwebstack each constraint will limit what will be
returned for a certain query, so that it applies to the rules specified in the shapes file. In the following paragraphs 
the constraints supported in Dotwebstack are discussed. 

### 1.8.1 minCount
It is possible to add `sh:minCount` to a property shape, to add constraints to your query regarding required properties.
When adding minCount of 0 or omitting minCount altogether, the property is optional. When adding a minCount of
1, the property is required. Currently, minCount > 1 is not supported.

### 1.8.2 hasValue
By adding `sh:hasValue` to a property shape, at least on of the triples in the data needs to have given predicate in 
combination with the given value in order to meet this requirement. 

### 1.8.3 Class
By adding an `sh:class` to a node shape, you can bind your node shape to a class. This means that a constraint is added to the 
query that will constrain the returned object to have an `rdf:type` relation with the given class. Multiple classes will
result in multiple constraints and thus objects will have to have `rdf:type` relations with all given classes. By defining
an `sh:or` around multiple `sh:class` constraints, it is possible to state that only one of the constraints has to be met
in order for an object to fit the shape. 

## 1.9 NodeShape Inheritance
It is possible to let one or more `sub` NodeShapes inherit PropertyShapes of a `super` NodeShape:

```shacl
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

## 1.10 Aggregate directive
It is possible to aggregate results by setting an `@aggregate` directive on a GraphQL field.
The `@aggregate` directive takes one argument, the `type` argument. The following types are currently supported: `COUNT`  For example: 

```graphql
type Brewery {
  identifier: ID!
  name: String!
  beerCount: Int! @aggregate(type: COUNT)
}
```

The `@aggregate` directive can also be used in combination with the `@transform` directive. 

```graphql
type Brewery {
  identifier: ID!
  name: String!
  hasBeers: Boolean! @aggregate(type: COUNT) @transform(expr: "hasBeers > 0")
}
```

and in combination with the `@filter` directive

```graphql
type Brewery {
  identifier: ID!
  name: String!
  beerCount: Int! @aggregate(type: COUNT)
}

type Query { 
  breweries(withBeerCount: Int = 0 @filter(field: "beerCount")): [Brewery!]!
    @sparql(
      repository: "local"
    )
}
```

## 1.11 Resource directive
It is possible to return the value of a subject, by using `@resource` on an IRI field.
```graphql
type Beer {
  identifier: ID!
  name: String!
  subject: IRI! @resource
}
```
The `@resource` directive can only be used for an IRI field.

## 1.12 Static Query for IRI, Model and SparqlQueryResult output types
For output types IRI and Model, it is possible to use a static query. This can be done by adding
a ```sparql``` folder to the ```config``` folder. Within the ```sparql``` folder, you can define a ```.rq``` file containing 
a Sparql query. The reference to this ```.rq``` is defined by using the ```queryRef``` property within the ```@sparql``` directive, like:

### For IRI
```graphql
type Query {
  nameOfQuery: IRI
  @sparql(
    queryRef: "iriQuery"
  )
}
```
In the example above, the name of the static query file should be ```iriQuery.rq```.
The following rules apply for output type IRI:
<ul>
<li>The query MUST be a SELECT query</li>
<li>The result MUST be one row</li>
<li>The binding MUST be equal to the graphQl field name</li>
</ul>

As mentioned, the binding name must be equal to the graphQl field:
```
SELECT ?nameOfQuery WHERE {
   ?s ?p ?nameOfQuery
}
```

### For Model
```graphql
type Query {
  nameOfQuery: Model
  @sparql(
    queryRef: "queryForModel"
  )
}
```
In the example above, the name of the static query file should be ```queryForModel.rq```.
The following rules apply for output type Model:
<ul>
<li>The query MUST be a DESCRIBE or CONSTRUCT query</li>
</ul>

### For SparqlQueryResult
```graphql
type Query {
  nameOfQuery: SparqlQueryResult
  @sparql(
    queryRef: "queryForSparqlQueryResult"
  )
}
```
In the example above, the name of the static query file should be ```queryForSparqlQueryResult.rq```.
