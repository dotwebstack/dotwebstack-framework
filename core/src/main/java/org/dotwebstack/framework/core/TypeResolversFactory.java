package org.dotwebstack.framework.core;

import graphql.schema.TypeResolver;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class TypeResolversFactory {

  private final Schema schema;

  private final Map<String, TypeResolver> typeResolvers = new HashMap<>();

  public TypeResolversFactory(Schema schema) {
    this.schema = schema;
  }

  public Map<String, TypeResolver> createTypeResolvers() {

    schema.getInterfaces()
        .forEach((name, interfaceType) -> {
          var typeResolver = createTypeResolver();
          typeResolvers.put(name, typeResolver);
        });

    return typeResolvers;
  }

  private TypeResolver createTypeResolver() {
    // This method should be implemented when we wish to support the "... on" functionality of GraphQL.
    return typeResolutionEnvironment -> null;
  }
}
