package org.dotwebstack.framework.core.typeDefinitionFactories;

import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static org.dotwebstack.framework.core.config.TypeUtils.newType;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.OR_FIELD;

import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.List;
import org.dotwebstack.framework.core.datafetchers.filter.FilterHelper;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;

public class FilterDefinitionFactory {

  public void addFilterTypes(Schema schema, TypeDefinitionRegistry typeDefinitionRegistry) {
    schema.getObjectTypes()
        .forEach((name, objectType) -> typeDefinitionRegistry.add(createFilterObjectTypeDefinition(name, objectType)));
  }

  private InputObjectTypeDefinition createFilterObjectTypeDefinition(String objectTypeName, ObjectType<?> objectType) {
    var filterName = createFilterName(objectTypeName);

    List<InputValueDefinition> inputValueDefinitions = new ArrayList<>();

    inputValueDefinitions.add(newInputValueDefinition().name("_exists")
        .type(newType("Boolean"))
        .build());

    inputValueDefinitions.addAll(objectType.getFilters()
        .entrySet()
        .stream()
        .map(entry -> newInputValueDefinition().name(entry.getKey())
            .type(newType(FilterHelper.getTypeNameForFilter(fieldFilterMap, objectType, entry.getKey(), entry.getValue())))
            .build())
        .toList());

    inputValueDefinitions.add(newInputValueDefinition().name(OR_FIELD)
        .type(newType(filterName))
        .build());

    return newInputObjectDefinition().name(filterName)
        .inputValueDefinitions(inputValueDefinitions)
        .build();
  }
}
