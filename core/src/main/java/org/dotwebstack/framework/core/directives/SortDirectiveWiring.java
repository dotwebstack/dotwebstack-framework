package org.dotwebstack.framework.core.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.dotwebstack.framework.core.validators.SortFieldValidator;
import org.springframework.stereotype.Component;

@Component
public class SortDirectiveWiring implements SchemaDirectiveWiring {

  private CoreTraverser coreTraverser;

  public SortDirectiveWiring(CoreTraverser coreTraverser) {
    this.coreTraverser = coreTraverser;
  }

  @Override
  @SuppressWarnings("unchecked")
  public GraphQLArgument onArgument(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    GraphQLType rawType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldDefinition()
        .getType());
    GraphQLUnmodifiedType unpackedType = GraphQLTypeUtil.unwrapAll(rawType);

    String fieldName = environment.getFieldsContainer()
        .getName();
    String typeName = environment.getFieldDefinition()
        .getName();
    String argumentName = environment.getElement()
        .getName();

    validateListType(rawType, fieldName, typeName);
    if (GraphQLTypeUtil.isScalar(unpackedType)) {
      List<Object> defaultSortValues = (List<Object>) environment.getElement()
          .getDefaultValue();
      validateListSize(defaultSortValues, fieldName, typeName);
      GraphQLType sortType = GraphQLTypeUtil.unwrapNonNull(environment.getElement()
          .getType());
      GraphQLUnmodifiedType unpackedSortType = GraphQLTypeUtil.unwrapAll(sortType);
      validateSortFieldList(sortType, unpackedSortType.getName(), fieldName, typeName, argumentName);
      Map<String, String> defaultSortValue = (LinkedHashMap<String, String>) defaultSortValues.get(0);
      validateFieldArgumentDoesNotExist(defaultSortValue, typeName, argumentName);
    } else {
      SortFieldValidator sortFieldValidator = new SortFieldValidator(coreTraverser, environment.getRegistry());
      GraphQLArgument sortArgument = environment.getElement();
      if (sortArgument != null && sortArgument.getDefaultValue() != null) {
        sortFieldValidator.validate(environment.getFieldDefinition()
            .getType(), sortArgument, sortArgument.getDefaultValue());
      }
    }
    return environment.getElement();
  }

  void validateFieldArgumentDoesNotExist(Map<String, String> defaultSortValue, String typeName, String fieldName) {
    if (defaultSortValue.containsKey("field")) {
      throw invalidConfigurationException(
          "Found an error on @sort directive defined on field {}.{}: @sort directive on scalar list cannot have "
              + "argument 'field'",
          typeName, fieldName);
    }
  }

  void validateListSize(List<Object> sortFields, String typeName, String fieldName) {
    if (sortFields.size() != 1) {
      throw invalidConfigurationException(
          "Found an error on @sort directive defined on field {}.{}: @sort directive defined on scalar list fields "
              + "should have a size of exactly one",
          typeName, fieldName);
    }
  }

  void validateSortFieldList(GraphQLType sortType, String sortfieldTypeName, String typeName, String fieldName,
      String argumentName) {
    if (!(GraphQLTypeUtil.isList(sortType) && Objects.equals(sortfieldTypeName, "SortField"))) {
      throw invalidConfigurationException(
          "Found an error on @sort directive defined on field {}.{}: @sort directive argument '{}' should be of "
              + "type [SortField]",
          typeName, fieldName, argumentName);
    }
  }

  void validateListType(GraphQLType rawType, String typename, String fieldname) {
    if (!(GraphQLTypeUtil.isList(rawType))) {
      throw invalidConfigurationException(
          "Found an error on @sort directive defined on field {}.{}: @sort can only be defined on a list fields",
          typename, fieldname);
    }
  }
}
