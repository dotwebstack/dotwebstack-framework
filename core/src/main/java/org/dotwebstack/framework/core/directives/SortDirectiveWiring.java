package org.dotwebstack.framework.core.directives;

import static org.dotwebstack.framework.core.input.CoreInputTypes.SORT_FIELD_FIELD;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.core.traversers.CoreTraverser;
import org.dotwebstack.framework.core.validators.SortFieldValidator;
import org.springframework.stereotype.Component;

@Component
public class SortDirectiveWiring extends ValidatingDirectiveWiring implements AutoRegisteredSchemaDirectiveWiring {

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
    GraphQLFieldDefinition fieldDefinition = environment.getFieldDefinition();
    GraphQLType rawType = GraphQLTypeUtil.unwrapNonNull(fieldDefinition.getType());

    GraphQLType type = GraphQLTypeUtil.unwrapAll(rawType);
    GraphQLFieldsContainer fieldsContainer = environment.getFieldsContainer();

    GraphQLArgument element = environment.getElement();
    String argumentName = element.getName();
    validate(getDirectiveName(), fieldDefinition, fieldsContainer, () -> {
      validateListType(rawType);
      if (!(rawType instanceof GraphQLTypeReference) && GraphQLTypeUtil.isScalar(type)) {
        List<Object> defaultSortValues = (List<Object>) element.getDefaultValue();
        validateListSize(defaultSortValues);

        GraphQLType sortType = GraphQLTypeUtil.unwrapNonNull(element.getType());
        GraphQLUnmodifiedType unpackedSortType = GraphQLTypeUtil.unwrapAll(sortType);
        validateSortFieldList(sortType, unpackedSortType.getName(), argumentName);

        Map<String, String> defaultSortValue = getDefaultSortValue(defaultSortValues);
        validateFieldArgumentDoesNotExist(defaultSortValue);
      } else {
        SortFieldValidator sortFieldValidator = new SortFieldValidator(coreTraverser, environment.getRegistry());
        if (element.getDefaultValue() != null) {
          sortFieldValidator.validate(fieldDefinition.getType(), element, element.getDefaultValue());
        }
      }
    });

    return element;
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getDefaultSortValue(List<Object> defaultSortValues) {
    return (Map<String, String>) defaultSortValues.get(0);
  }

  void validateListType(GraphQLType rawType) {
    assert GraphQLTypeUtil.isList(rawType) : "can only be defined on a list field";
  }

  void validateListSize(List<Object> sortFields) {
    assert sortFields.size() == 1 : "directive defined on scalar list fields should have a size of exactly one";
  }

  void validateSortFieldList(GraphQLType sortType, String sortfieldTypeName, String argumentName) {
    assert GraphQLTypeUtil.isList(sortType) && "SortField".equals(sortfieldTypeName) : String
        .format("argument '%s' should be of type [SortField]", argumentName);
  }

  void validateFieldArgumentDoesNotExist(Map<String, String> defaultSortValue) {
    assert !defaultSortValue.containsKey(SORT_FIELD_FIELD) : "directive on scalar list cannot have argument 'field'";
  }
}
