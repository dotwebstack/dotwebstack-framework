package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isConnectionType;

import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.backend.filter.BackendFilterCriteria;
import org.dotwebstack.framework.core.backend.filter.ObjectFieldPath;
import org.dotwebstack.framework.core.condition.GraphQlNativeEnabled;
import org.dotwebstack.framework.core.datafetchers.ContextConstants;
import org.dotwebstack.framework.core.datafetchers.SortConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.helpers.TypeHelper;
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

  private static final List<String> KEY_ARGUMENTS_EXCLUDE = List.of(FilterConstants.FILTER_ARGUMENT_NAME,
      SortConstants.SORT_ARGUMENT_NAME, ContextConstants.CONTEXT_ARGUMENT_NAME);

  private final Schema schema;

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  public BackendRequestFactory(Schema schema, TypeDefinitionRegistry typeDefinitionRegistry) {
    this.schema = schema;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  public CollectionRequest createCollectionRequest(DataFetchingEnvironment environment) {
    var executionStepInfo = getExecutionStepInfo(environment);
    var objectType = getObjectType(environment.getFieldType());

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(environment))
        .backendFilterCriteria(
            createFilterCriteria(objectType, executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME)))
        .sortCriterias(createSortCriteria(objectType, executionStepInfo.getArgument(SORT_ARGUMENT_NAME)))
        .build();
  }

  private CollectionRequest createCollectionRequest(SelectedField selectedField, DataFetchingEnvironment environment) {
    ObjectType<?> objectType = getObjectType(TypeHelper.unwrapConnectionType(selectedField.getType()));

    var executionStepInfo = getExecutionStepInfo(environment);

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(selectedField, environment))
        .backendFilterCriteria(createFilterCriteria(objectType,
            getNestedMap(executionStepInfo.getArguments(), FilterConstants.FILTER_ARGUMENT_NAME)))
        .sortCriterias(createSortCriteria(objectType, executionStepInfo.getArgument(SORT_ARGUMENT_NAME)))
        .build();
  }

  public ObjectRequest createObjectRequest(DataFetchingEnvironment environment) {
    var objectType = getObjectType(environment.getFieldType());

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriteria(createKeyCriteria(environment.getArguments()))
        .scalarFields(getScalarFields(environment.getSelectionSet()))
        .objectFields(getObjectFields(environment.getSelectionSet(), environment))
        .selectedObjectListFields(getObjectListFields(environment.getSelectionSet(), environment))
        .contextCriteria(createContextCriteria(environment))
        .build();
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField, DataFetchingEnvironment environment) {
    ObjectType<?> objectType = getObjectType(TypeHelper.unwrapConnectionType(selectedField.getType()));

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriteria(createKeyCriteria(selectedField.getArguments()))
        .scalarFields(getScalarFields(selectedField.getSelectionSet()))
        .objectFields(getObjectFields(selectedField.getSelectionSet(), environment))
        .selectedObjectListFields(getObjectListFields(selectedField.getSelectionSet(), environment))
        .contextCriteria(createContextCriteria(environment))
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

  private ContextCriteria createContextCriteria(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo = getExecutionStepInfo(environment);

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

  private Map<SelectedField, ObjectRequest> getObjectFields(DataFetchingFieldSelectionSet selectionSet,
      DataFetchingEnvironment environment) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isObjectField)
        // TODO: typeDefinitionRegistry weg refactoren
        .filter(selectedField -> !isConnectionType(typeDefinitionRegistry, selectedField.getType()))
        .collect(
            Collectors.toMap(Function.identity(), selectedField -> createObjectRequest(selectedField, environment)));
  }

  private Map<SelectedField, CollectionRequest> getObjectListFields(DataFetchingFieldSelectionSet selectionSet,
      DataFetchingEnvironment environment) {
    return selectionSet.getImmediateFields()
        .stream()
        // TODO: typeDefinitionRegistry weg refactoren
        .filter(
            isObjectListField.or((selectedField) -> isConnectionType(typeDefinitionRegistry, selectedField.getType())))
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

  private List<BackendFilterCriteria> createFilterCriteria(ObjectType<?> objectType,
      Map<String, Object> filterArgument) {
    if (filterArgument == null) {
      return List.of();
    }

    return filterArgument.keySet()
        .stream()
        .map(filterName -> {
          var filterConfiguration = objectType.getFilters()
              .get(filterName);

          var fieldPath = createObjectFieldPath(objectType, filterConfiguration.getField());

          return BackendFilterCriteria.builder()
              .fieldPath(fieldPath)
              .value(getNestedMap(filterArgument, filterName))
              .build();

        })
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
            .fields(createObjectFieldPath(objectType, config.getField()).stream()
                .map(ObjectFieldPath::getObjectField)
                .collect(Collectors.toList()))
            .direction(config.getDirection())
            .build())
        .collect(Collectors.toList());
  }

  private List<ObjectFieldPath> createObjectFieldPath(ObjectType<?> objectType, String path) {
    var current = objectType;
    var fieldPath = new ArrayList<ObjectFieldPath>();

    for (var segment : path.split("\\.")) {
      var field = Optional.ofNullable(current)
          .flatMap(o -> o.getField(segment))
          .orElseThrow();

      current = schema.getObjectType(field.getType())
          .orElse(null);

      var objectFieldPath = ObjectFieldPath.builder()
          .objectField(field)
          .objectType(current)
          .build();

      fieldPath.add(objectFieldPath);
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
