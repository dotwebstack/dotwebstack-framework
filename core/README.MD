# 1. Core

## 1.1 Data loaders
TODO

## 1.2 Scalar Types
Dotwebstack offers support for the *Object*, *Date* and *DateTime* scalars besides the scalar types that have built-in support 
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
are invalid. The following constraints are currently provided: `min`, `max`, `oneOf`, `oneOfInt`, `valuesIn`, `pattern` and `expr`. 

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

### 1.4.5 ValuesIn

```graphql
 breweries(countries: [String!] @constraint(valuesIn: ["BE","DE", "EN", "NL"])): [Brewery!]!
```

With the `valuesIn` constraint a list argument can be restricted to have values matching any string within the provided list. In the example above
the user is allowed to provide a list with one or more of "BE", "DE", "EN", "NL". 

### 1.4.6 Pattern

```graphql
 name: [String] @constraint(pattern: "^[1-9][0-9]*$") : [Brewery!]!
```

With the `pattern` constraint an argument can be restricted to match the provided regex-pattern. So in this case it is only allowed 
to provide an argument which has to be greater than 0. 

### 1.4.7 Expr

```graphql
 page: Int = 1 @constraint(expr: "args.page > 0 && args.page <= ( 1000 / args.pageSize )")
```

With the `expr` constraint an argument can be restricted to validate true for the provided Jexl expression. So in this case it is only allowed 
to provide an argument for page which is greater than 0 and less than or equal to 1000 divided by pageSize argument. 

## 1.5 Jexl Functions
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