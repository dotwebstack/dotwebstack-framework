package org.dotwebstack.framework.core.typeDefinitionFactories;

import static graphql.language.FieldDefinition.newFieldDefinition;
import static graphql.language.NonNullType.newNonNullType;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.dotwebstack.framework.core.config.TypeUtils.createType;
import static org.dotwebstack.framework.core.config.TypeUtils.newListType;
import static org.dotwebstack.framework.core.config.TypeUtils.newNonNullableListType;
import static org.dotwebstack.framework.core.config.TypeUtils.newType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.AGGREGATE_TYPE;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isAggregate;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_CONNECTION_TYPE;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_VISIBLE;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.KEY_FIELD;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.KEY_PATH;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;

public class DefinitionFactoryHelper {

  private static final String GEOMETRY_TYPE = "Geometry";

  public List<FieldDefinition> createFieldDefinitions(Schema schema, ObjectType<?> objectType) {
    return objectType.getFields()
        .values()
        .stream()
        .flatMap(objectField -> createFieldDefinition(schema, objectField).stream())
        .toList();
  }

  private Optional<FieldDefinition> createFieldDefinition(Schema schema, ObjectField objectField) {
    Type<?> type;

    if (StringUtils.isBlank(objectField.getType())) {
      if (isAggregate(objectField)) {
        type = NonNullType.newNonNullType(TypeUtils.newType(AGGREGATE_TYPE))
            .build();
      } else {
        return Optional.empty();
      }
    } else {
      type = createTypeForField(schema, objectField);
    }

    Map<String, String> additionalData = new HashMap<>();
    if (isNotBlank(objectField.getValueFetcher())) {
      additionalData.put(CUSTOM_FIELD_VALUEFETCHER, objectField.getValueFetcher());
    }

    additionalData.put(IS_VISIBLE, Boolean.toString(objectField.isVisible()));

    return Optional.of(newFieldDefinition().name(objectField.getName())
        .type(type)
        .inputValueDefinitions(createInputValueDefinitions(objectField))
        .additionalData(additionalData)
        .build());
  }

  private List<InputValueDefinition> createInputValueDefinitions(Schema schema, ObjectField objectField) {
    var inputValueDefinitions = new ArrayList<InputValueDefinition>();

    if (GEOMETRY_TYPE.equals(objectField.getType())) {
      inputValueDefinitions.addAll(createGeometryArguments());
    }

    schema.getObjectType(objectField.getType())
        .ifPresent(objectType -> objectField.getKeys()
            .stream()
            .map(keyField -> createInputValueDefinition(keyField, objectType, Map.of(KEY_FIELD, keyField, KEY_PATH, keyField)))
            .forEach(inputValueDefinitions::add));

    objectField.getArguments()
        .stream()
        .map(this::createFieldInputValueDefinition)
        .forEach(inputValueDefinitions::add);

    if (objectField.isList() && schema.getObjectTypes()
        .containsKey(objectField.getType())) {

      var objectType = schema.getObjectType(objectField.getType())
          .orElseThrow();

      createFilterArgument(objectField.getType(), objectType).ifPresent(inputValueDefinitions::add);

      createSortArgument(objectField.getType(), objectType.getSortableBy()).ifPresent(inputValueDefinitions::add);

      if (objectField.isPageable() && objectField.isList()) {
        createFirstArgument().ifPresent(inputValueDefinitions::add);
        createOffsetArgument().ifPresent(inputValueDefinitions::add);
      }
    }

    if (isAggregate(objectField)) {
      var objectFieldType = objectField.getAggregationOf();
      var objectType = schema.getObjectType(objectFieldType)
          .orElseThrow();

      createFilterArgument(objectFieldType, objectType).ifPresent(inputValueDefinitions::add);
    }

    return inputValueDefinitions;
  }

  private Type<?> createTypeForField(Schema schema, ObjectField objectField) {
    var type = objectField.getType();

    if (objectField.isList() && schema.getObjectTypes()
        .containsKey(objectField.getType())) {

      return createListType(type, objectField.isPageable(), objectField.isNullable());
    }

    return createType(objectField);
  }

  private Type<?> createListType(String type, boolean pageable, boolean nullable) {
    if (pageable) {
      var connectionTypeName = createConnectionName(type);
      return newNonNullType(newType(connectionTypeName)).additionalData(IS_CONNECTION_TYPE, TRUE.toString())
          .build();
    } else {
      if (nullable) {
        return newListType(type);
      } else {
        return newNonNullableListType(type);
      }
    }
  }

  private String createConnectionName(String objectTypeName) {
    return String.format("%sConnection", StringUtils.capitalize(objectTypeName));
  }

  private String createFilterName(String objectTypeName) {
    return String.format("%sFilter", StringUtils.capitalize(objectTypeName));
  }

}
