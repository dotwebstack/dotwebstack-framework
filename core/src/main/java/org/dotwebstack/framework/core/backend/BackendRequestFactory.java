package org.dotwebstack.framework.core.backend;

import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.datafetchers.ContextConstants;
import org.dotwebstack.framework.core.datafetchers.SortConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;

public class BackendRequestFactory {

  private static final List<String> KEY_ARGUMENTS_EXCLUDE = List.of(FilterConstants.FILTER_ARGUMENT_NAME,
      SortConstants.SORT_ARGUMENT_NAME, ContextConstants.CONTEXT_ARGUMENT_NAME);

  private final Schema schema;

  private final DataFetchingEnvironment environment;

  public BackendRequestFactory(Schema schema, DataFetchingEnvironment environment) {
    this.schema = schema;
    this.environment = environment;
  }

  public CollectionRequest createCollectionRequest() {
    var objectType = getObjectType(environment.getFieldType());

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(objectType, environment.getSelectionSet(), environment.getArguments()))
        .sortCriterias(createSortCriteria(objectType, environment.getArgument(SORT_ARGUMENT_NAME)))
        .build();
  }

  public ObjectRequest createObjectRequest() {
    var objectType = getObjectType(environment.getFieldType());
    return createObjectRequest(objectType, environment.getSelectionSet(), environment.getArguments());
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField) {
    var objectType = getObjectType(selectedField.getType());
    return createObjectRequest(objectType, selectedField.getSelectionSet(), selectedField.getArguments());
  }

  private ObjectRequest createObjectRequest(ObjectType<?> objectType, DataFetchingFieldSelectionSet selectionSet,
      Map<String, Object> arguments) {
    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriteria(createKeyCriteria(arguments))
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

  private List<KeyCriteria> createKeyCriteria(Map<String, Object> arguments) {
    return arguments.entrySet()
        .stream()
        .filter(argument -> !KEY_ARGUMENTS_EXCLUDE.contains(argument.getKey()))
        .map(entry -> KeyCriteria.builder()
            .values(Map.of(entry.getKey(), entry.getValue()))
            .build())
        .collect(Collectors.toList());
  }

  private List<SortCriteria> createSortCriteria(ObjectType<?> objectType, String sortArgument) {
    var sortableBy = objectType.getSortableBy();

    if (sortableBy.isEmpty()) {
      return List.of();
    }

    // TODO fix compound names
    var sortableByConfig = objectType.getSortableBy()
        .entrySet()
        .stream()
        .filter(entry -> entry.getKey()
            .toUpperCase()
            .equals(sortArgument))
        .findFirst()
        .map(Map.Entry::getValue)
        .orElseThrow();

    return sortableByConfig.stream()
        .map(config -> SortCriteria.builder()
            .fields(List.of(objectType.getField(config.getField())
                .orElseThrow()))
            .direction(config.getDirection())
            .build())
        .collect(Collectors.toList());
  }

  private ObjectType<?> getObjectType(GraphQLType type) {
    var rawType = GraphQLTypeUtil.unwrapAll(type);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw ExceptionHelper.illegalStateException("Not an object type.");
    }

    return schema.getObjectType(((GraphQLObjectType) rawType).getName())
        .orElseThrow();
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
