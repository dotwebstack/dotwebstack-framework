package org.dotwebstack.framework.core.datafetchers.filter;

import static graphql.language.InputObjectTypeDefinition.newInputObjectDefinition;
import static graphql.language.InputValueDefinition.newInputValueDefinition;
import static graphql.language.ListType.newListType;
import static graphql.language.NonNullType.newNonNullType;
import static graphql.language.TypeName.newTypeName;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.DATE_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.DATE_TIME_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.EQ_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.FLOAT_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.GTE_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.GT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.INT_FILTER_INPUT_OBJECT_TYPE;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.LTE_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.LT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.NOT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.filter.FilterConstants.STRING_FILTER_INPUT_OBJECT_TYPE;

import graphql.Scalars;
import graphql.language.InputObjectTypeDefinition;
import graphql.language.InputValueDefinition;
import graphql.schema.GraphQLScalarType;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.NonNull;
import org.dotwebstack.framework.core.GraphqlConfigurer;
import org.dotwebstack.framework.core.scalars.CoreScalars;
import org.springframework.stereotype.Component;

@Component
public class FilterConfigurer implements GraphqlConfigurer {

  @Override
  public void configureTypeDefinitionRegistry(@NonNull TypeDefinitionRegistry registry) {
    registry.add(createStringFilterType());
    registry.add(createIntFilterType());
    registry.add(createFloatFilterType());
    registry.add(createDateFilterType());
    registry.add(createDateTimeFilterType());
  }

  private InputObjectTypeDefinition createStringFilterType() {
    var typeName = Scalars.GraphQLString.getName();

    return newInputObjectDefinition().name(STRING_FILTER_INPUT_OBJECT_TYPE)
        .inputValueDefinition(createInputValueDefinition(EQ_FIELD, typeName))
        .inputValueDefinition(createArrayInputValue(typeName))
        .inputValueDefinition(createInputValueDefinition(NOT_FIELD, STRING_FILTER_INPUT_OBJECT_TYPE))
        .build();
  }

  private InputObjectTypeDefinition createIntFilterType() {
    return createNumberFilterType(Scalars.GraphQLInt, INT_FILTER_INPUT_OBJECT_TYPE);
  }

  private InputObjectTypeDefinition createFloatFilterType() {
    return createNumberFilterType(Scalars.GraphQLFloat, FLOAT_FILTER_INPUT_OBJECT_TYPE);
  }

  private InputObjectTypeDefinition createNumberFilterType(GraphQLScalarType scalarType, String filterInputName) {
    var typeName = scalarType.getName();

    return newInputObjectDefinition().name(filterInputName)
        .inputValueDefinition(createInputValueDefinition(EQ_FIELD, typeName))
        .inputValueDefinition(createArrayInputValue(typeName))
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

  private InputValueDefinition createArrayInputValue(String typeName) {
    var type = newTypeName(typeName).build();
    var nonNullType = newNonNullType(type).build();
    var listType = newListType(nonNullType).build();

    return newInputValueDefinition().name(FilterConstants.IN_FIELD)
        .type(listType)
        .build();
  }

  private InputValueDefinition createInputValueDefinition(String inputValueName, String typeName) {
    var type = newTypeName(typeName).build();

    return newInputValueDefinition().name(inputValueName)
        .type(type)
        .build();
  }
}
