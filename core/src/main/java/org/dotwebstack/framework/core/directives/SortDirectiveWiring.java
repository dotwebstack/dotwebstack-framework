package org.dotwebstack.framework.core.directives;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_FIELD;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.dotwebstack.framework.core.validators.SortFieldValidator;
import org.springframework.stereotype.Component;

@Component
public class SortDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

  private CoreTraverser coreTraverser;

  public SortDirectiveWiring(CoreTraverser coreTraverser) {
    this.coreTraverser = coreTraverser;
  }

  @Override
  public String getDirectiveName() {
    return CoreDirectives.SORT_NAME;
  }

  @Override
  @SuppressWarnings("unchecked")
  public GraphQLArgument onArgument(SchemaDirectiveWiringEnvironment<GraphQLArgument> environment) {
    GraphQLType rawType = GraphQLTypeUtil.unwrapNonNull(environment.getFieldDefinition()
        .getType());

    GraphQLType unwrappedType = rawType;
    while (GraphQLTypeUtil.isWrapped(unwrappedType)) {
      unwrappedType = GraphQLTypeUtil.unwrapOne(unwrappedType);
    }

    String fieldName = environment.getFieldsContainer()
        .getName();
    String typeName = environment.getFieldDefinition()
        .getName();
    String argumentName = environment.getElement()
        .getName();

    validateListType(rawType, typeName, fieldName);
    if (!(rawType instanceof GraphQLTypeReference) && GraphQLTypeUtil.isScalar(unwrappedType)) {
      List<Object> defaultSortValues = (List<Object>) environment.getElement()
          .getDefaultValue();
      validateListSize(defaultSortValues, typeName, fieldName);
      GraphQLType sortType = GraphQLTypeUtil.unwrapNonNull(environment.getElement()
          .getType());
      GraphQLUnmodifiedType unpackedSortType = GraphQLTypeUtil.unwrapAll(sortType);
      validateSortFieldList(sortType, unpackedSortType.getName(), typeName, fieldName, argumentName);
      Map<String, String> defaultSortValue = (Map<String, String>) defaultSortValues.get(0);
      validateFieldArgumentDoesNotExist(defaultSortValue, typeName, argumentName);
    } else {
      SortFieldValidator sortFieldValidator = new SortFieldValidator(coreTraverser, environment.getRegistry());
      GraphQLArgument sortArgument = environment.getElement();
      if (sortArgument != null && sortArgument.getDefaultValue() != null) {
        sortFieldValidator.validate(environment.getFieldDefinition(), sortArgument, sortArgument.getDefaultValue());
      }
    }
    return environment.getElement();
  }

  void validateFieldArgumentDoesNotExist(Map<String, String> defaultSortValue, String typeName, String fieldName) {
    if (defaultSortValue.containsKey(SORT_FIELD_FIELD)) {
      throw invalidConfigurationException(
          "Found an error on @sort directive defined on field {}.{}: @sort directive on scalar list cannot have "
              + "argument 'field'",
          fieldName, typeName);
    }
  }

  void validateListSize(List<Object> sortFields, String typeName, String fieldName) {
    if (sortFields.size() != 1) {
      throw invalidConfigurationException(
          "Found an error on @sort directive defined on field {}.{}: @sort directive defined on scalar list fields "
              + "should have a size of exactly one",
          fieldName, typeName);
    }
  }

  void validateSortFieldList(GraphQLType sortType, String sortfieldTypeName, String typeName, String fieldName,
      String argumentName) {
    if (!(GraphQLTypeUtil.isList(sortType) && Objects.equals(sortfieldTypeName, "SortField"))) {
      throw invalidConfigurationException(
          "Found an error on @sort directive defined on field {}.{}: @sort directive argument '{}' should be of "
              + "type [SortField]",
          fieldName, typeName, argumentName);
    }
  }

  void validateListType(GraphQLType rawType, String typename, String fieldname) {
    if (!(GraphQLTypeUtil.isList(rawType))) {
      throw invalidConfigurationException(
          "Found an error on @sort directive defined on field {}.{}: @sort can only be defined on a list fields",
          fieldname, typename);
    }
  }
}
