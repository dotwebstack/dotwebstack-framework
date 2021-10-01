package org.dotwebstack.framework.core.backend;

import static java.util.function.Predicate.not;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.springframework.stereotype.Component;

@Component
public class BackendRequestFactory {

  private final Schema schema;

  public BackendRequestFactory(Schema schema) {
    this.schema = schema;
  }

  public CollectionRequest createCollectionRequest(DataFetchingEnvironment environment) {
    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(environment))
        .build();
  }

  public ObjectRequest createObjectRequest(DataFetchingEnvironment environment) {
    var rawType = GraphQLTypeUtil.unwrapAll(environment.getFieldType());

    if (!(rawType instanceof GraphQLObjectType)) {
      throw ExceptionHelper.illegalStateException("Not an object type.");
    }

    var objectType = schema.getObjectType(((GraphQLObjectType) rawType).getName())
        .orElseThrow();

    return createObjectRequest(objectType, environment.getSelectionSet());
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField) {
    var objectType = schema.getObjectType(selectedField.getObjectTypeNames()
        .get(0))
        .orElseThrow();

    return createObjectRequest(objectType, selectedField.getSelectionSet());
  }

  private ObjectRequest createObjectRequest(ObjectType<?> objectType, DataFetchingFieldSelectionSet selectionSet) {
    return ObjectRequest.builder()
        .objectType(objectType)
        .selectedScalarFields(getScalarFields(selectionSet.getImmediateFields()))
        .selectedObjectFields(getObjectFields(selectionSet.getImmediateFields()))
        .build();
  }

  private Collection<SelectedField> getScalarFields(Collection<SelectedField> selectedFields) {
    return selectedFields.stream()
        .filter(isScalarField)
        .filter(not(isIntrospectionField))
        .collect(Collectors.toSet());
  }

  private Map<SelectedField, ObjectRequest> getObjectFields(Collection<SelectedField> selectedFields) {
    return selectedFields.stream()
        .filter(isObjectField)
        .collect(Collectors.toMap(Function.identity(), this::createObjectRequest));
  }

  // TODO move to utils?
  private static Predicate<SelectedField> isScalarField = selectedField -> {
    var unwrappedType = GraphQLTypeUtil.unwrapAll(selectedField.getType());
    return unwrappedType instanceof GraphQLScalarType;
  };

  // TODO move to utils?
  private static Predicate<SelectedField> isObjectField = selectedField -> {
    var unwrappedType = GraphQLTypeUtil.unwrapNonNull(selectedField.getType());
    return unwrappedType instanceof GraphQLObjectType;
  };

  // TODO move to utils?
  private static Predicate<SelectedField> isIntrospectionField = selectedField -> selectedField.getName()
      .startsWith("__");
}
