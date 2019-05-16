# Backend-Rdf4J

## Property Paths (https://www.w3.org/TR/shacl/#property-paths)
While it is possible to query an object for of its properties directly, with SHACL SPARQL allows you can do a lot more. 
For our RDF4J backend we have built support for the paths described in this document. We will discuss the possibillities 
in the following paragraphs. 

### Predicate Paths (https://www.w3.org/TR/shacl/#property-path-predicate)
The predicate path is the simplest of the paths. These are the building blocks for each property path. It describes one predicate 
to retrieve and object from the current subject. 

- `ex:parent`   

### Alternative Paths (https://www.w3.org/TR/shacl/#property-path-alternative)
The Alternative Path describes the option get either the object from this predicate `OR` the object from that predicate.
You can chain as many paths as you like, and you can use both predicate paths and any of the other path constructions as well. 
   
- `ex:father|ex:mother`  

   
### Sequence Paths (https://www.w3.org/TR/shacl/#property-path-sequence)
The Sequence Path describes the possibility to chain multiple predicates. You can use this to access the properties of 
underlying objects. For example when you are not interested in the parent per se, but more in his or her name you can use:

- `ex:parent/ex:firstName` 

Again, this is the simple example. You can also use any of the other property paths described in this chapter inside a 
Sequence Path, this enables you to create constructions such as:

- `ex:father|ex:mother/ex:firstName`
- `^ex:child/ex:firstName`

### Inverse Paths (https://www.w3.org/TR/shacl/#property-path-inverse)
The inverse path traverses the predicate in the opposite direction. This can be useful when you only have an explicit relation
in one way, but not the other. So in our previous examples that would mean that we have a relation from a child to a parent,
 but not from a parent to a child. In that case you can use the following inverse path to obtain the name of the child:
 
- `^ex:parent`
- `(^ex:father|^ex:mother)/ex:firstName`

In the previous example I used a couple of constructions. Again it is possible to use the other paths within an inverse path
as well.  

### OneOrMore Path (https://www.w3.org/TR/shacl/#property-path-one-or-more)
The one or more path finds a connection between subjects and objects using the predicate, and matching the pattern one or more times.
For example, finding the names of all people a person knows either directly, or through another person using the `ex:knows` predicate:

- `ex:knows+/ex:firstName`

### ZeroOrMore Path (https://www.w3.org/TR/shacl/#property-path-zero-or-more)
The zero or more path works in the same way as the zero or one path, but uses the `*` operator and allowing paths of
length 0.

For example, finding the names of people a person knows using the `ex:knows` predicate, including the name of the person:
-`ex:knows*/ex:firstName`

### ZeroOrOne Path (https://www.w3.org/TR/shacl/#property-path-zero-or-one)