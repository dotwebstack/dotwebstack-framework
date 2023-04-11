package org.dotwebstack.framework.core;

import graphql.TypeResolutionEnvironment;
import graphql.schema.GraphQLObjectType;
import graphql.schema.TypeResolver;
import java.util.HashMap;
import java.util.List;
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
          var isImplementedBy = isImplementedBy(name, schema);
          var typeResolver = createTypeResolver(isImplementedBy);
          typeResolvers.put(name, typeResolver);
        });

    return typeResolvers;
  }

  private List<String> isImplementedBy(String implementer, Schema schema) {
    return Stream.concat(schema.getObjectTypes()
                .values()
                .stream(),
            schema.getInterfaces()
                .values()
                .stream())
        .filter(o -> o.getImplements()
            .contains(implementer))
        .map(ObjectType::getName)
        .collect(Collectors.toList());
  }

  private TypeResolver createTypeResolver(List<String> isImplementedBy) {
    return typeResolutionEnvironment -> {
      var objName = typeResolutionEnvironment.getObject()
          .toString();
      if (isImplementedBy.contains(objName)) {
        return typeResolutionEnvironment.getSchema()
            .getObjectType(objName);
      }
      return null;
    };
  }
}
