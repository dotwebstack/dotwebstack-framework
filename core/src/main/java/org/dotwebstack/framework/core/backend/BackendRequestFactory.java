package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isNestedObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.datafetchers.ContextConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(GraphQlNativeEnabled.class)
public class BackendRequestFactory {

  private final Schema schema;

  public BackendRequestFactory(Schema schema) {
    this.schema = schema;
  }

  public CollectionRequest createCollectionRequest(ExecutionStepInfo executionStepInfo,
      DataFetchingFieldSelectionSet selectionSet) {
    var unwrappedType = TypeHelper.unwrapConnectionType(executionStepInfo.getType());
    var objectType = getObjectType(unwrappedType);

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(executionStepInfo, selectionSet))
        .filterCriterias(
            createFilterCriteria(objectType, executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME)))
        .sortCriterias(createSortCriteria(objectType, executionStepInfo.getArgument(SORT_ARGUMENT_NAME)))
        .build();
  }

  private CollectionRequest createCollectionRequest(SelectedField selectedField, ExecutionStepInfo executionStepInfo) {
    // enkel nodig voor het bepalen van een joinconditie
    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(selectedField, executionStepInfo))
        .build();
  }

  public ObjectRequest createObjectRequest(ExecutionStepInfo executionStepInfo,
      DataFetchingFieldSelectionSet selectionSet) {
    var unwrappedType = TypeHelper.unwrapConnectionType(executionStepInfo.getType());
    var objectType = getObjectType(unwrappedType);

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriteria(createKeyCriteria(executionStepInfo.getFieldDefinition()
            .getArguments(), executionStepInfo.getArguments()))
        .scalarFields(getScalarFields(selectionSet))
        .objectFields(getObjectFields(selectionSet, executionStepInfo))
        .nestedObjectFields(getNestedObjectFields(selectionSet))
        .selectedObjectListFields(getObjectListFields(selectionSet, executionStepInfo))
        .contextCriteria(createContextCriteria(executionStepInfo))
        .build();
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField, ExecutionStepInfo executionStepInfo) {
    ObjectType<?> objectType = getObjectType(TypeHelper.unwrapConnectionType(selectedField.getType()));

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriteria(createKeyCriteria(executionStepInfo.getFieldDefinition()
            .getArguments(), selectedField.getArguments()))
        .scalarFields(getScalarFields(selectedField.getSelectionSet()))
        .objectFields(getObjectFields(selectedField.getSelectionSet(), executionStepInfo))
        .nestedObjectFields(getNestedObjectFields(selectedField.getSelectionSet()))
        .selectedObjectListFields(getObjectListFields(selectedField.getSelectionSet(), executionStepInfo))
        .contextCriteria(createContextCriteria(executionStepInfo))
        .build();
  }

  public RequestContext createRequestContext(DataFetchingEnvironment environment) {
    Map<String, Object> source = environment.getSource();

    var objectField = schema.getObjectType(getExecutionStepInfo(environment).getObjectType()
        .getName())
        .flatMap(objectType -> objectType.getField(getExecutionStepInfo(environment).getField()
            .getName()))
        .orElse(null);

    return RequestContext.builder()
        .objectField(objectField)
        .source(source)
        .build();
  }

  // TODO: getExecutionStepInfo verplaatsen naar aparte helper.
  public ExecutionStepInfo getExecutionStepInfo(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo;

    var isList = isList(unwrapNonNull(environment.getFieldType()));

    if (schema.usePaging() && isList) {
      executionStepInfo = environment.getExecutionStepInfo()
          .getParent();
    } else {
      executionStepInfo = environment.getExecutionStepInfo();
    }
    return executionStepInfo;
  }

  private ContextCriteria createContextCriteria(ExecutionStepInfo executionStepInfo) {
    var contextName = executionStepInfo.getFieldDefinition()
        .getArguments()
        .stream()
        .flatMap(graphQLArgument -> graphQLArgument.getDefinition()
            .getAdditionalData()
            .entrySet()
            .stream())
        .filter(entry -> "contextName".equals(entry.getKey()))
        .map(Map.Entry::getValue)
        .findFirst();

    if (contextName.isPresent()) {
      var context = schema.getContext(contextName.get());

      Map<String, Object> arguments =
          getNestedMap(executionStepInfo.getArguments(), ContextConstants.CONTEXT_ARGUMENT_NAME);

      return context.map(c -> ContextCriteria.builder()
          .name(contextName.get())
          .context(c)
          .values(arguments)
          .build())
          .orElseThrow();
    }

    return null;
  }

  private List<SelectedField> getScalarFields(DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isScalarField)
        .filter(not(isIntrospectionField))
        .collect(Collectors.toList());
  }

  private Map<SelectedField, Collection<SelectedField>> getNestedObjectFields(
      DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isNestedObjectField)
        .collect(
            Collectors.toMap(Function.identity(), selectedField -> getScalarFields(selectedField.getSelectionSet())));
  }

  private Map<SelectedField, ObjectRequest> getObjectFields(DataFetchingFieldSelectionSet selectionSet,
      ExecutionStepInfo executionStepInfo) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isObjectField)
        .collect(Collectors.toMap(Function.identity(),
            selectedField -> createObjectRequest(selectedField, executionStepInfo)));
  }

  private Map<SelectedField, CollectionRequest> getObjectListFields(DataFetchingFieldSelectionSet selectionSet,
      ExecutionStepInfo executionStepInfo) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isObjectListField)
        .collect(Collectors.toMap(Function.identity(),
            selectedField -> createCollectionRequest(selectedField, executionStepInfo)));
  }

  private List<KeyCriteria> createKeyCriteria(List<GraphQLArgument> arguments, Map<String, Object> argumentMap) {
    return arguments.stream()
        .filter(argument -> argument.getDefinition()
            .getAdditionalData()
            .containsKey(GraphQlConstants.IS_KEY_ARGUMENT))
        .filter(argument -> argumentMap.containsKey(argument.getName()))
        .map(argument -> Map.of(argument.getName(), argumentMap.get(argument.getName())))
        .map(values -> KeyCriteria.builder()
            .values(values)
            .build())
        .collect(Collectors.toList());
  }

  private List<FilterCriteria> createFilterCriteria(ObjectType<?> objectType, Map<String, Object> filterArgument) {
    if (filterArgument == null) {
      return List.of();
    }

    return filterArgument.keySet()
        .stream()
        .filter(filterName -> Objects.nonNull(filterArgument.get(filterName)))
        .map(filterName -> {
          var filterConfiguration = objectType.getFilters()
              .get(filterName);

          var fieldPath = createObjectFieldPath(objectType, filterConfiguration.getField());

          return FilterCriteria.builder()
              .fieldPath(fieldPath)
              .value(createFilterValue(filterArgument, filterName))
              .build();

        })
        .collect(Collectors.toList());
  }

  private Map<String, Object> createFilterValue(Map<String, Object> arguments, String key) {
    var value = arguments.get(key);
    if (value instanceof Boolean) {
      return Map.of(FilterConstants.EQ_FIELD, value);
    }

    return getNestedMap(arguments, key);
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
            .fields(createObjectFieldPath(objectType, config.getField()))
            .direction(config.getDirection())
            .build())
        .collect(Collectors.toList());
  }

  private List<ObjectField> createObjectFieldPath(ObjectType<?> objectType, String path) {
    var current = objectType;
    var fieldPath = new ArrayList<ObjectField>();

    for (var segment : path.split("\\.")) {
      var field = Optional.ofNullable(current)
          .flatMap(o -> o.getField(segment))
          .orElseThrow();

      current = schema.getObjectType(field.getType())
          .orElse(null);

      fieldPath.add(field);
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
}
