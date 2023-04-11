# Extension module: `ext-orchestrate`

This module introduces several orchestration features and relies on the [`graphql-java-orchestrate`](https://github.com/dotwebstack/graphql-java-orchestrate) library (another DotWebStack project).

Currently, the orchestration extension only supports schema wrapping. In the future, other orchestration patterns will be included.

## Schema wrapping

To enable schema wrapping, a subschema must be defined in `application.yaml`. The root key must match one of the defined subschemas.

```yaml
dotwebstack:
  orchestrate:
    root: dbeerpedia
    subschemas:
      dbeerpedia:
        endpoint: http://localhost:8080
```

(The configuration allows configuring multiple subschemas, to support future enhancements)

The schema wrapper will automatically introspect the remote schema by executing an introspection query.
Every GraphQL query will now be executed against the remote endpoint instead of a local schema.

Subschema properties are:
* `endpoint`: The URI for the remote GraphQL endpoint
* `bearerAuth`: A bearer token, which will be passed in the `Authorization` header (optional).

## Transforms

In case the schema needs to be modified to match certain requirements, a subschema can be modified. To do so, a bean 
implementing `SubschemaModifier` could be provided. For example:

```java
@Configuration
public class ExampleConfiguration {

  @Bean
  public SubschemaModifier subschemaModifier() {
    RenameObjectFields transform = new RenameObjectFields((typeName, fieldName, fieldDefinition) -> {
      if (typeName.equals("Brewery") && fieldName.equals("name")) {
        return "title";
      }
      
      return fieldName;
    });
    
    return (key, subschema) -> subschema.transform(builder -> builder.transform(transform));
  }
}
```

Multiple transforms can be chained by using the `pipe` operator. A [complete example](https://github.com/dotwebstack/dotwebstack-framework/tree/v0.4/example/example-orchestrate) can be found in the `/examples` folder.