package org.dotwebstack.framework.core.typeDefinitionFactories;

import static graphql.language.InterfaceTypeDefinition.newInterfaceTypeDefinition;
import static graphql.language.TypeName.newTypeName;
import static java.lang.Boolean.TRUE;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_NESTED;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.language.FieldDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;

public class InterfaceDefinitionFactory {

  public static void addInterfaceTypes(Schema schema, TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getInterfaces()
        .forEach((name, interfaceType) -> {
          var interfaceTypeDefinition = newInterfaceTypeDefinition().name(name)
              .definitions(createFieldDefinitions(interfaceType));

          interfaceType.getImplements()
              .forEach(implementz -> {
                if (schema.getInterfaces()
                    .containsKey(implementz)) {
                  interfaceTypeDefinition.implementz(newTypeName().name(implementz)
                      .build());
                } else {
                  throw invalidConfigurationException("Implemented Interface '{}' not found in provided schema for Interface '{}'.", implementz, name);
                }
              });

          if (interfaceType.isNested()) {
            interfaceTypeDefinition.additionalData(IS_NESTED, TRUE.toString());
          }
          typeDefinitionRegistry.add(interfaceTypeDefinition.build());
        });
  }


}
