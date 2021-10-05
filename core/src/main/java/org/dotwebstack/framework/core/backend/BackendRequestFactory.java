package org.dotwebstack.framework.core.backend;

import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.datafetchers.ContextConstants;
import org.dotwebstack.framework.core.datafetchers.SortConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.springframework.stereotype.Component;

@Component
public class BackendRequestFactory {

  private static final List<String> KEY_ARGUMENTS_EXCLUDE = List.of(FilterConstants.FILTER_ARGUMENT_NAME,
      SortConstants.SORT_ARGUMENT_NAME, ContextConstants.CONTEXT_ARGUMENT_NAME);

  private final Schema schema;

  public BackendRequestFactory(Schema schema) {
    this.schema = schema;
  }

  public CollectionRequest createCollectionRequest(DataFetchingEnvironment environment) {
    var objectType = getObjectType(environment.getFieldType());

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(environment))
        .sortCriterias(createSortCriteria(objectType, environment.getArgument(SORT_ARGUMENT_NAME)))
        .build();
  }

  private CollectionRequest createCollectionRequest(SelectedField selectedField, DataFetchingEnvironment environment) {
    var objectType = getObjectType(selectedField.getType());

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(selectedField, environment))
        .sortCriterias(createSortCriteria(objectType, selectedField.getArguments()
            .get(SORT_ARGUMENT_NAME)
            .toString()))
        .build();
  }

  public ObjectRequest createObjectRequest(DataFetchingEnvironment environment) {
    var objectType = getObjectType(environment.getFieldType());
    Map<String, Object> source = environment.getSource();

    return ObjectRequest.builder()
        .objectType(objectType)
        .parentField(environment.getField())
        .source(source)
        .keyCriteria(createKeyCriteria(environment.getArguments()))
        .selectedScalarFields(getScalarFields(environment.getSelectionSet()))
        .selectedObjectFields(getObjectFields(environment.getSelectionSet(), environment))
        .selectedObjectListFields(getObjectListFields(environment.getSelectionSet(), environment))
        .build();
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField, DataFetchingEnvironment environment) {
    var objectType = getObjectType(selectedField.getType());

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriteria(createKeyCriteria(selectedField.getArguments()))
        .selectedScalarFields(getScalarFields(selectedField.getSelectionSet()))
        .selectedObjectFields(getObjectFields(selectedField.getSelectionSet(), environment))
        .selectedObjectListFields(getObjectListFields(selectedField.getSelectionSet(), environment))
        .build();
  }

  private List<SelectedField> getScalarFields(DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isScalarField)
        .filter(not(isIntrospectionField))
        .collect(Collectors.toList());
  }

  private Map<SelectedField, ObjectRequest> getObjectFields(DataFetchingFieldSelectionSet selectionSet,
      DataFetchingEnvironment environment) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isObjectField)
        .collect(
            Collectors.toMap(Function.identity(), selectedField -> createObjectRequest(selectedField, environment)));
  }

  private Map<SelectedField, CollectionRequest> getObjectListFields(DataFetchingFieldSelectionSet selectionSet,
      DataFetchingEnvironment environment) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isObjectListField)
        .collect(Collectors.toMap(Function.identity(),
            selectedField -> createCollectionRequest(selectedField, environment)));
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
            .fields(createFieldPath(objectType, config.getField()))
            .direction(config.getDirection())
            .build())
        .collect(Collectors.toList());
  }

  private List<ObjectField> createFieldPath(ObjectType<?> objectType, String path) {
    var current = objectType;
    var fieldPath = new ArrayList<ObjectField>();

    for (var segment : path.split("\\.")) {
      var field = Optional.ofNullable(current)
          .flatMap(o -> o.getField(segment))
          .orElseThrow();

      fieldPath.add(field);

      current = schema.getObjectType(field.getType())
          .orElse(null);
    }

    return fieldPath;
  }

  private ObjectType<?> getObjectType(GraphQLType type) {
    var rawType = GraphQLTypeUtil.unwrapAll(type);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw illegalStateException("Not an object type.");
    }

    return schema.getObjectType(rawType.getName())
        .orElseThrow(() -> illegalStateException("No objectType with name '{}' found!", rawType.getName()));
  }

  // TODO move to utils?
  private static final Predicate<SelectedField> isScalarField = selectedField -> {
    var unwrappedType = GraphQLTypeUtil.unwrapAll(selectedField.getType());
    return unwrappedType instanceof GraphQLScalarType;
  };

  // TODO move to utils?
  private static final Predicate<SelectedField> isObjectField =
      selectedField -> !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(selectedField.getType()))
          && GraphQLTypeUtil.isObjectType(GraphQLTypeUtil.unwrapAll(selectedField.getType()));

  // TODO move to utils?
  private static final Predicate<SelectedField> isObjectListField =
      selectedField -> GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(selectedField.getType()))
          && GraphQLTypeUtil.isObjectType(GraphQLTypeUtil.unwrapAll(selectedField.getType()));

  // TODO move to utils?
  private static final Predicate<SelectedField> isIntrospectionField = selectedField -> selectedField.getName()
      .startsWith("__");
}
