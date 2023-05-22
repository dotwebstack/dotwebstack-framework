package org.dotwebstack.framework.core.datafetchers.filter;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.TypeName.newTypeName;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.BOOLEAN_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.CONTAINS_ALL_OF_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.CONTAINS_ANY_OF_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.DATE_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.DATE_TIME_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.EQ_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.FLOAT_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.FLOAT_LIST_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.GTE_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.GT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.INT_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.INT_LIST_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.IN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.LTE_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.LT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.MATCH_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.NOT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.SCALAR_LIST_FILTER_POSTFIX;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_LIST_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_PARTIAL;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_PARTIAL_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_PARTIAL_LIST;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_PARTIAL_LIST_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.scalars.CoreScalars.DATE;
import static org.dotwebstack.framework.core.scalars.CoreScalars.DATETIME;

import graphql.Scalars;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Map;
import lombok.NonNull;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
public class CoreFilterConfigurer implements GraphqlConfigurer, FilterConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(createStringFilterType());
    registry.add(createStringListFilterType());
    registry.add(createIntFilterType());
    registry.add(createIntListFilterType());
    registry.add(createFloatFilterType());
    registry.add(createFloatListFilterType());
    registry.add(createDateFilterType());
    registry.add(createDateTimeFilterType());
    registry.add(createStringPartialFilterType());
    registry.add(createStringPartialListFilterType());
    registry.add(createBooleanFilterType());
  }

  @Override
  public void configureFieldFilterMapping(@NonNull Map<String, String> fieldFilterMap) {
    fieldFilterMap.put(GraphQLString.getName(), STRING_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(GraphQLString.getName()
        .concat(SCALAR_LIST_FILTER_POSTFIX), STRING_LIST_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(GraphQLInt.getName(), INT_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(GraphQLInt.getName()
        .concat(SCALAR_LIST_FILTER_POSTFIX), INT_LIST_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(GraphQLFloat.getName(), FLOAT_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(GraphQLFloat.getName()
        .concat(SCALAR_LIST_FILTER_POSTFIX), FLOAT_LIST_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(DATE.getName(), DATE_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(DATETIME.getName(), DATE_TIME_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(GraphQLBoolean.getName(), BOOLEAN_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(STRING_PARTIAL, STRING_PARTIAL_FILTER_INPUT_OBJECT_TYPE);
    fieldFilterMap.put(STRING_PARTIAL_LIST, STRING_PARTIAL_LIST_FILTER_INPUT_OBJECT_TYPE);
  }

  private InputObjectTypeDefinition createBooleanFilterType() {
    var typeName = GraphQLBoolean.getName();

    return newInputObjectDefinition().name(BOOLEAN_FILTER_INPUT_OBJECT_TYPE)
        .inputValueDefinition(createInputValueDefinition(EQ_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(NOT_FIELD, BOOLEAN_FILTER_INPUT_OBJECT_TYPE))
        .build();
  }

  private InputObjectTypeDefinition createStringPartialFilterType() {
    var typeName = Scalars.GraphQLString.getName();

    return newInputObjectDefinition().name(STRING_PARTIAL_FILTER_INPUT_OBJECT_TYPE)
        .inputValueDefinition(createInputValueDefinition(MATCH_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(NOT_FIELD, STRING_PARTIAL_FILTER_INPUT_OBJECT_TYPE))
        .build();
  }

  private InputObjectTypeDefinition createStringPartialListFilterType() {
    var typeName = Scalars.GraphQLString.getName();

    return newInputObjectDefinition().name(STRING_PARTIAL_LIST_FILTER_INPUT_OBJECT_TYPE)
        .inputValueDefinition(createInputValueDefinition(MATCH_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(NOT_FIELD, STRING_PARTIAL_LIST_FILTER_INPUT_OBJECT_TYPE))
        .build();
  }

  private InputObjectTypeDefinition createStringFilterType() {
    var typeName = Scalars.GraphQLString.getName();

    return newInputObjectDefinition().name(STRING_FILTER_INPUT_OBJECT_TYPE)
        .inputValueDefinition(createInputValueDefinition(EQ_FIELD, typeName))
        .inputValueDefinition(createArrayInputValue(IN_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(NOT_FIELD, STRING_FILTER_INPUT_OBJECT_TYPE))
        .build();
  }

  private InputObjectTypeDefinition createStringListFilterType() {
    var typeName = Scalars.GraphQLString.getName();

    return createScalarListFilterType(STRING_LIST_FILTER_INPUT_OBJECT_TYPE, typeName);
  }

  private InputObjectTypeDefinition createIntFilterType() {
    return createNumberFilterType(GraphQLInt, INT_FILTER_INPUT_OBJECT_TYPE);
  }

  private InputObjectTypeDefinition createIntListFilterType() {
    var typeName = GraphQLInt.getName();

    return createScalarListFilterType(INT_LIST_FILTER_INPUT_OBJECT_TYPE, typeName);
  }

  private InputObjectTypeDefinition createFloatFilterType() {
    return createNumberFilterType(GraphQLFloat, FLOAT_FILTER_INPUT_OBJECT_TYPE);
  }

  private InputObjectTypeDefinition createFloatListFilterType() {
    var typeName = GraphQLFloat.getName();
    return createScalarListFilterType(FLOAT_LIST_FILTER_INPUT_OBJECT_TYPE, typeName);
  }

  private InputObjectTypeDefinition createNumberFilterType(GraphQLScalarType scalarType, String filterInputName) {
    var typeName = scalarType.getName();

    return newInputObjectDefinition().name(filterInputName)
        .inputValueDefinition(createInputValueDefinition(EQ_FIELD, typeName))
        .inputValueDefinition(createArrayInputValue(IN_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(LT_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(LTE_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(GT_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(GTE_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(NOT_FIELD, filterInputName))
        .build();
  }

  private InputObjectTypeDefinition createDateFilterType() {
    return createDateFilterType(CoreScalars.DATE, DATE_FILTER_INPUT_OBJECT_TYPE);
  }

  private InputObjectTypeDefinition createDateFilterType(GraphQLScalarType scalarType, String filterInputName) {
    var typeName = scalarType.getName();

    return newInputObjectDefinition().name(filterInputName)
        .inputValueDefinition(createInputValueDefinition(EQ_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(LT_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(LTE_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(GT_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(GTE_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(NOT_FIELD, filterInputName))
        .build();
  }

  private InputObjectTypeDefinition createDateTimeFilterType() {
    return createDateFilterType(CoreScalars.DATETIME, DATE_TIME_FILTER_INPUT_OBJECT_TYPE);
  }

  private InputValueDefinition createArrayInputValue(String filterOperator, String typeName) {
    var type = newTypeName(typeName).build();
    var nonNullType = newNonNullType(type).build();
    var listType = newListType(nonNullType).build();

    return newInputValueDefinition().name(filterOperator)
        .type(listType)
        .build();
  }

  private InputValueDefinition createInputValueDefinition(String filterOperator, String typeName) {
    var type = newTypeName(typeName).build();

    return newInputValueDefinition().name(filterOperator)
        .type(type)
        .build();
  }

  private InputObjectTypeDefinition createScalarListFilterType(String inputObjectName, String typeName) {
    return newInputObjectDefinition().name(inputObjectName)
        .inputValueDefinition(createArrayInputValue(EQ_FIELD, typeName))
        .inputValueDefinition(createArrayInputValue(CONTAINS_ALL_OF_FIELD, typeName))
        .inputValueDefinition(createArrayInputValue(CONTAINS_ANY_OF_FIELD, typeName))
        .inputValueDefinition(createInputValueDefinition(NOT_FIELD, inputObjectName))
        .build();
  }
}
