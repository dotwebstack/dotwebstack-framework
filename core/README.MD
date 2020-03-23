# 1. Core

## 1.1 Data Fetchers
To facilitate an extensible way of data fetching the *core* module relies internally on a [CoreDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/core/src/main/java/org/dotwebstack/framework/core/datafetchers/CoreDataFetcher.java) interface.
This interface extends the GraphQL [DataFetcher](https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/DataFetcher.java) interface.

```java
public interface CoreDataFetcher<T> extends DataFetcher<T> {

  boolean supports(DataFetchingEnvironment environment);

  DataFetcherType getType();

}
```

The [CoreDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/core/src/main/java/org/dotwebstack/framework/core/datafetchers/CoreDataFetcher.java) interface offers the capability to evaluate the support for a given [DataFetchingEnvironment](https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/DataFetchingEnvironment.java) with the *supports* method.

DotWebStack provides two abstract implementations of the [CoreDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/core/src/main/java/org/dotwebstack/framework/core/datafetchers/CoreDataFetcher.java) . Custom datafetchers can be added by implementing the [CoreDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/core/src/main/java/org/dotwebstack/framework/core/datafetchers/CoreDataFetcher.java) 
interface, these implementations are annotated with a Spring *@Component* annotation, so they will be automatically 
registered.

### 1.1.1 SourceDataFetcher

A [SourceDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/core/src/main/java/org/dotwebstack/framework/core/datafetchers/SourceDataFetcher.java) can be used to fetch data from *any* source.


### 1.1.2 DelegateDataFetcher

A [DelegateDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/core/src/main/java/org/dotwebstack/framework/core/datafetchers/DelegateDataFetcher.java) can act as a *decorator* for a [SourceDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/core/src/main/java/org/dotwebstack/framework/core/datafetchers/SourceDataFetcher.java) and gives you the capability to transform the result from a [SourceDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/v0.2/core/src/main/java/org/dotwebstack/framework/core/datafetchers/SourceDataFetcher.java)

## 1.2 Converters
With lots of different backends, it means there are lots of different possible data types that need to be supported. The
responsibility of converting these backend specific values to a format that the GraphQL middle layer understands is 
with each backend. But a general construct of how to do this is provided in core. A CoreConverterRouter is added to the
SourceDataFetcher. A specific implementation of this type of DataFetcher can have a specific implementation of 
the CoreConverterRouter:  

```java
public interface CoreConverterRouter {

  Object convert(Object object);

}
```

## 1.3 Scalar Types
Dotwebstack offers support for the *Date* and *DateTime* scalars besides the scalar types that have built-in support 
from GraphQL *(Int, Float, String, Boolean, ID)*. It is also possible to create a custom Scalar Type:

- Create the new scalar
```java
  public static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
      .name("Date")
      .description("Date type")
      .coercing(new DateCoercing())
      .build();
```

- Write the scalar coercing 
```java
class DateCoercing implements Coercing<LocalDate, LocalDate> {
  
  @Override
  public LocalDate serialize(@NonNull Object value) {
    ...
  }
}
```

- Add the scalar to the TypeDefinitionRegistry
```java
  typeDefinitionRegistry.add(new ScalarTypeDefinition(CoreScalars.DATE.getName()));
```

- Add the scalar to the RunTimeWiring
```java
  builder.scalar(CoreScalars.DATE)
```

## 1.4 Argument Constraints

It is possible to add a constraint to an argument on any level by preceding the argument with the `@constraint` directive. 
These constraints can be used to restrict the input from the user and provide hints as to why certain arguments 
are invalid. The following constraints are currently provided: `min`, `max`, `oneOf`, `oneOfInt` and `pattern`. 

### 1.4.1 Min

```graphql
 breweries(page: Int = 1 @constraint(min: 1)): [Brewery!]!
```

With the `min` constraint you can force the argument to have a minimum value. In case of the example
it means that the page provided by the user has to be at least 1. 

### 1.4.2 Max

```graphql
 breweries(pageSize: Int = 10 @constraint(max: 100)): [Brewery!]!
```

With the `max` constraint you can force the argument to have a maximum value. In case of the example
it means that the pageSize provided by the user can be no higher than 100. 

### 1.4.3 OneOf

```graphql
 breweries(country: String @constraint(oneOf: ["BE","DE", "EN", "NL"])): [Brewery!]!
```

With the `oneOf` constraint an argument can be restricted to match any string within the provided list. So in this case
the user is allowed to provide an argument with the value of "BE", "DE", "EN", "NL" only. 

### 1.4.4 OneOfInt

```graphql
 breweries(pageSize: Int = 10 @constraint(oneOfInt: [10, 20, 50])): [Brewery!]!
```

With the `oneOfInt` constraint an argument can be restricted to match any integer within the provided list. So in this 
case is allowed to provide an argument with a value of 10, 20 or 50 only. 

### 1.4.5 Pattern

```graphql
 name: [String] @constraint(pattern: "^[1-9][0-9]*$") : [Brewery!]!
```

With the `pattern` constraint an argument can be restricted to match the provided regex-pattern. So in this case it is only allowed 
to provide an argument which has to be greater than 0. 

## 1.5 Paging Directives
Pagination in Dotwebstack can be done with help of the directives `@limit` and `@offset`:

```grapqhl
    beers(limit: Int = 10 @limit,
          offset: Int = 0 @offset):[Beer]
```

In the example query the first 10 beers are returned by default. If a different limit or offset is required, other values
can be provided for them as well. 

While this is one case in which limit and offset can be provided, another common case is by the use of `page` and 
`pageSize` parameters. For this reason it is possible in DotWebStack to define expressions by which the `@limit` and `@offset`
 parameters can be calculated:

```grapqhl
    beers(pageSize: Int = 10 @limit, 
          page: Int = 1 @offset(expr: "(args.page - 1) * args.pageSize")):[Beer]
```

The `pageSize` and the limit are the same in this case, so no expression is needed. To calculate the offset from the page 
parameter however, the expression `(args.page - 1) * args.pageSize` is used. Note that it is also possible to use separate 
parameters for convenience and clearity:

```grapqhl
    beers(pageSize: Int = 10,
          page: Int = 1, 
          limit: Int @limit(expr: "args.pageSize"), 
          offset: Int @offset(expr: "(args.page - 1) * args.pageSize")):[Beer]
```

## 1.6 Transform Directive
The transform directive allows the user to transform a scalar field based on a given 
[jexl](http://commons.apache.org/proper/commons-jexl/) expression. This directive takes the 
argument `expr` that contains the jexl expression to transform the original to the desired value:

```graphql
type Brewery {
  status: String! @transform(expr: "statusIri.getLocalName()")
}
```

In the given example the `status` is retrieved from a `statusIri`.  

## 1.7 Filter Directive
The filter directive (@filter) allows the user to apply a filter on a field. The field-argument is optional and has a default value consisting of the name of the field where the directive is applied on. So when @filter is used without parameters, the name of the field and the default operator (=) will be used.  

```schema.graphqls
  page: Int = 1 @constraint(min: 1), pageSize: Int = 10 @constraint(oneOfInt: [10, 20, 50]),
  founded: String @filter(),
  sinceBefore: String @filter(field: "founded", operator: "<")
  name: [String] @filter(field: "name")): [Brewery!]!
```

## 1.8 Sort Directive
It is possible to declare a `sort` directive on a field or an argument. The argument of this parameter needs to 
be one or any array of objects containing a `field` and an `order` parameter:
* `field`: A `string` that contains the field to sort by, this can also be a path of fields with '.' as separator. The field(s)
should be present in the query output type and may only refer to unmodified scalar types.
* `order`: An `enum` representing the sort order with values `ASC` for ascending and `DESC` for descending.

For example, the following query will sort by `MyOutputType.field1.field2`:
```graphql
myQuery(sort: [SortField!] = [{ field: "field1.field2", order: ASC }] @sort):[MyOutputType!]
```

In the same way it is possible to order the values on a field with type list:
```graphql
type Brewery {
  ...
  beerNames(sort = [SortField!] = [{ order: ASC }]): [String!]
  ...
}
```

Notice that the `field` argument is missing. It is not possible to provide a field on a list consisting of Scalar types.
When the sort directive is applied on a field with a nonscalar type, the `field` can be ommited, in that case the argument
name is used as value: 

```graphql
type Brewery {
  ...
  beers(sort= [SortField!] = [{ order: ASC }]): [Beer!]
  ...
}
```

When the sort encounters a `null` this entry is always sorted last, both with `ASC` and `DESC` sorting.

## 1.9 Jexl Functions
When using the JexlEngine provided by `org.dotwebstack.framework.core.CoreConfiguration.jexlBuilder`, it is possible to add custom functions to allow for more readable Jexl expressions. Custom functions can be added by creating a POJO class that implements the `org.dotwebstack.framework.core.jexl.JexlFunction` interface. Any class implementing this interface will be picked up by Spring and added to the Jexl engine under its provided namespace.
```java
public class MyFunction implements JexlFunction {
  @Override
  public String getNamespace() {
    return "myns";
  }

  public String hello(){
    return "hello world";
  }
}
```
The `hello()` method can now be used from a Jexl expression/script:
```
'var hello = myns:hello(); return hello;'
```
