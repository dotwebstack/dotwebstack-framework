# 1. Core

## 1.1 Data Fetchers
To facilitate an extensible way of data fetching the *core* module relies internally on a [CoreDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/IHR2-2318/core/src/main/java/org/dotwebstack/framework/core/datafetchers/CoreDataFetcher.java) interface. 
This interface extends the GraphQL [DataFetcher](https://github.com/graphql-java/graphql-java/blob/master/src/main/java/graphql/schema/DataFetcher.java) interface. 

```java
public interface CoreDataFetcher<T> extends DataFetcher<T> {

  boolean supports(DataFetchingEnvironment environment);

  DataFetcherType getType();

}
```

The [CoreDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/IHR2-2318/core/src/main/java/org/dotwebstack/framework/core/datafetchers/CoreDataFetcher.java) interface offers the capability to evaluate the support for a given DataFetchingEnvironment with the *supports* method. 

We are currently offering two abstract implementations of the CoreDataFetcher which can be used by anyone. When you write your own *CoreDataFetcher* and annotate it with a Spring *@Component* annotation it will be automatically registered.


### 1.1.1 SourceDataFetcher
 
A [SourceDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/IHR2-2318/core/src/main/java/org/dotwebstack/framework/core/datafetchers/SourceDataFetcher.java) can be used to actually fetch data from *any* source.


### 1.1.2 DelegateDataFetcher

A [DelegateDataFetcher](https://github.com/dotwebstack/dotwebstack-framework/blob/IHR2-2318/core/src/main/java/org/dotwebstack/framework/core/datafetchers/DelegateDataFetcher.java) can act as a *decorator* for a *SourceDataFetcher* and gives you the capability to transform the result from a *SourceDataFetcher*


## 1.2 Scalar Types
Dotwebstack offers support for the *Date* and *DateTime* scalars besides the scalar types that have built-in support 
from GraphQL *(Int, Float, String, Boolean, ID)*. Dotwebstack also offers support to write your own scalar in a
pluggable way. To create your own Scalar Type you need to do the following:

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

## 1.3 Argument Constraints

It is possible to add a constraint to an argument on any level by preceding the argument with the `@constraint` directive. 
These constraints can be used to restrict the input from the user and also to be able give hints as to why certain arguments 
are invalid. You can currently use one of the following constraints: `min`, `max`, `oneOf` and `oneOfInt`

### 1.3.1 Min

```graphql
 breweries(page: Int = 1 @constraint(min: 1)): [Brewery!]!
```

With the `min` constraint you can force the argument to have a minimum value. In case of the example
it means that the page provided by the user has to be at least 1. 

### 1.3.2 Max

```graphql
 breweries(pageSize: Int = 10 @constraint(max: 100])): [Brewery!]!
```

With the `max` constraint you can force the argument to have a maximum value. In case of the example
it means that the pageSize provided by the user can be no higher than 100. 

### 1.3.3 OneOf

```graphql
 breweries(country: String @constraint(oneOf: ["BE","DE", "EN", "NL"])): [Brewery!]!
```

With the `oneOf` constraint you can restrict an argument to match any string within a given selection 
of strings, but none of the strings that are not in this selection. So in this case the user is allowed to 
provide an argument with the value of "BE", "DE", "EN", "NL" only. 


### 1.3.4 OneOfInt

```graphql
 breweries(pageSize: Int = 10 @constraint(oneOfInt: [10, 20, 50])): [Brewery!]!
```

With the `oneOfInt` constraint you can restrict an argument to be any integer within a given selection 
of integers, but none of the integers that are not in this selection. So in this case is allowed to 
provide an argument with a value of 10, 20 or 50 only. 

## 1.4 Transform Directive
The transform directive allows the user to transform a scalar field based on a given 
[jexl](http://commons.apache.org/proper/commons-jexl/) expression. This directive takes the 
argument `expr` that contains the jexl expression to transform the original to the desired value:

```graphql
type Brewery {
  status: String! @transform(expr: "statusIri.getLocalName()")
}
```

In the given example the `status` is retrieved from a `statusIri`.  