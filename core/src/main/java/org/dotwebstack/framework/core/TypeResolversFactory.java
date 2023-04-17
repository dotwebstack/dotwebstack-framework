package org.dotwebstack.framework.core;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.TypeResolver;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.dotwebstack.framework.core.model.Schema;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class TypeResolversFactory {

  private final Schema schema;
  private final TypeDefinitionRegistry typeDefinitionRegistry;

  private final Map<String, TypeResolver> typeResolvers = new HashMap<>();

  public TypeResolversFactory(Schema schema, TypeDefinitionRegistry typeDefinitionRegistry) {
    this.schema = schema;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
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
    return typeResolutionEnvironment -> {
      if(typeResolutionEnvironment.getObject() instanceof Map<?,?> objectFields) {
        if (objectFields.containsKey("dtype")) {
          String dtypeName = (String) objectFields.get("dtype");
          return typeResolutionEnvironment.getSchema().getObjectType(dtypeName);
        }
      }



      System.out.println("something");
      return null;
//      typeDefinitionRegistry.getType(typeResolutionEnvironment.)
    };
  }
}
