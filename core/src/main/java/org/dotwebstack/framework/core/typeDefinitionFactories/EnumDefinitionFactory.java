package org.dotwebstack.framework.core.typeDefinitionFactories;

import static graphql.language.EnumTypeDefinition.newEnumTypeDefinition;
import static graphql.language.EnumValueDefinition.newEnumValueDefinition;

import graphql.schema.idl.TypeDefinitionRegistry;
import org.dotwebstack.framework.core.model.Schema;

public class EnumDefinitionFactory {
  public static void addEnumerations(Schema schema, TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getEnumerations()
        .forEach((name, enumeration) -> {
          var enumerationTypeDefinition = newEnumTypeDefinition().name(name)
              .enumValueDefinitions(enumeration.getValues()
                  .stream()
                  .map(value -> newEnumValueDefinition().name(value)
                      .build())
                  .toList())
              .build();
          typeDefinitionRegistry.add(enumerationTypeDefinition);
        });
  }
}
