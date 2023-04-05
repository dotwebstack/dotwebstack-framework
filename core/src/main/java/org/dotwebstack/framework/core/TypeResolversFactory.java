package org.dotwebstack.framework.core;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.core.model.ObjectType;
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
          var isImplementedBy = Stream.concat(schema.getObjectTypes()
              .values()
              .stream(),
              schema.getInterfaces()
                  .values()
                  .stream())
              .filter(o -> o.getImplements() != null && o.getImplements()
                  .contains(name))
              .map(ObjectType::getName)
              .collect(Collectors.toList());

          var typeResolver = new TypeResolver() {

            @Override
            public GraphQLObjectType getType(TypeResolutionEnvironment env) {
              var objName = env.getObject()
                  .toString();
              if (isImplementedBy.contains(objName)) {
                return env.getSchema()
                    .getObjectType(objName);
              }
              return null;
            }
          };

          typeResolvers.put(name, typeResolver);
        });

    return typeResolvers;
  }
}
