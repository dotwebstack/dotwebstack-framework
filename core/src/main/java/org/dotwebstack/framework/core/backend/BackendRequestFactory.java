package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateFunctionType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateScalarType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getSeparator;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isDistinct;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateValidator.validate;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.createFieldPath;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getQueryName;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getRequestStepInfo;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import com.google.common.base.CaseFormat;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.backend.filter.FilterCriteria;
import org.dotwebstack.framework.core.backend.filter.GroupFilterCriteria;
import org.dotwebstack.framework.core.backend.filter.GroupFilterOperator;
import org.dotwebstack.framework.core.backend.filter.ScalarFieldFilterCriteria;
import org.dotwebstack.framework.core.datafetchers.ContextConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
import org.dotwebstack.framework.core.helpers.MapHelper;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class BackendRequestFactory {

  private final Schema schema;

  private final BackendExecutionStepInfo backendExecutionStepInfo;

  public BackendRequestFactory(Schema schema, BackendExecutionStepInfo backendExecutionStepInfo) {
    this.schema = schema;
    this.backendExecutionStepInfo = backendExecutionStepInfo;
  }

  public CollectionRequest createCollectionRequest(ExecutionStepInfo executionStepInfo,
      DataFetchingFieldSelectionSet selectionSet) {
    var unwrappedType = TypeHelper.unwrapConnectionType(executionStepInfo.getType());
    var objectType = getObjectType(unwrappedType);

    var filterCriteria =
        createFilterCriteria(objectType, executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME));

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(executionStepInfo, selectionSet))
        .filterCriteria(filterCriteria.orElse(null))
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
        .keyCriteria(createKeyCriteria(objectType, executionStepInfo).orElse(null))
        .scalarFields(getScalarFields(selectionSet))
        .objectFields(getObjectFields(selectionSet, executionStepInfo))
        .objectListFields(getObjectListFields(selectionSet, executionStepInfo))
        .contextCriteria(createContextCriteria(getRequestStepInfo(executionStepInfo)))
        .aggregateObjectFields(getAggregateObjectFields(objectType, selectionSet))
        .build();
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField, ExecutionStepInfo executionStepInfo) {
    ObjectType<?> objectType = getObjectType(TypeHelper.unwrapConnectionType(selectedField.getType()));

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriteria(createKeyCriteria(objectType, executionStepInfo).orElse(null))
        .scalarFields(getScalarFields(selectedField.getSelectionSet()))
        .objectFields(getObjectFields(selectedField.getSelectionSet(), executionStepInfo))
        .objectListFields(getObjectListFields(selectedField.getSelectionSet(), executionStepInfo))
        .contextCriteria(createContextCriteria(getRequestStepInfo(executionStepInfo)))
        .aggregateObjectFields(getAggregateObjectFields(objectType, selectedField.getSelectionSet()))
        .build();
  }

  public RequestContext createRequestContext(DataFetchingEnvironment environment) {
    Map<String, Object> source = environment.getSource();

    var objectField = schema.getObjectType(backendExecutionStepInfo.getExecutionStepInfo(environment)
        .getObjectType()
        .getName())
        .map(objectType -> objectType.getField(backendExecutionStepInfo.getExecutionStepInfo(environment)
            .getField()
            .getName()))
        .orElse(null);

    return RequestContext.builder()
        .objectField(objectField)
        .source(source)
        .build();
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

  private List<FieldRequest> getScalarFields(DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isScalarField)
        .filter(not(isIntrospectionField))
        .map(this::mapToFieldRequest)
        .collect(Collectors.toList());
  }

  private FieldRequest mapToFieldRequest(SelectedField selectedField) {
    return FieldRequest.builder()
        .name(selectedField.getName())
        .isList(GraphQLTypeUtil.isList(selectedField.getType()))
        .arguments(selectedField.getArguments())
        .build();
  }

  private Map<FieldRequest, ObjectRequest> getObjectFields(DataFetchingFieldSelectionSet selectionSet,
      ExecutionStepInfo executionStepInfo) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isObjectField)
        .collect(Collectors.toMap(this::mapToFieldRequest,
            selectedField -> createObjectRequest(selectedField, executionStepInfo)));
  }

  private Map<FieldRequest, CollectionRequest> getObjectListFields(DataFetchingFieldSelectionSet selectionSet,
      ExecutionStepInfo executionStepInfo) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isObjectListField)
        .collect(Collectors.toMap(this::mapToFieldRequest,
            selectedField -> createCollectionRequest(selectedField, executionStepInfo)));
  }

  private List<AggregateObjectRequest> getAggregateObjectFields(ObjectType<?> objectType,
      DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(AggregateHelper::isAggregateField)
        .map(selectedField -> {
          var objectField = objectType.getFields()
              .get(selectedField.getName());

          var aggregationObjectType = objectField.getTargetType();

          return AggregateObjectRequest.builder()
              .objectField(objectField)
              .aggregateFields(getAggregateFields(aggregationObjectType, selectedField.getSelectionSet()))
              .build();
        })
        .collect(Collectors.toList());
  }

  private List<AggregateField> getAggregateFields(ObjectType<?> objectType,
      DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getImmediateFields()
        .stream()
        .map(selectedField -> createAggregateField(objectType, selectedField))
        .collect(Collectors.toList());
  }

  private AggregateField createAggregateField(ObjectType<?> objectType, SelectedField selectedField) {
    var aggregateFunctionType = getAggregateFunctionType(selectedField);
    var type = getAggregateScalarType(selectedField);
    var distinct = isDistinct(selectedField);

    validate(schema.getEnumerations(), objectType, selectedField);

    String separator = null;
    if (aggregateFunctionType == AggregateFunctionType.JOIN) {
      separator = getSeparator(selectedField);
    }

    var fieldName = (String) selectedField.getArguments()
        .get(AggregateConstants.FIELD_ARGUMENT);

    var objectField = objectType.getFields()
        .get(fieldName);

    return AggregateField.builder()
        .field(objectField)
        .functionType(aggregateFunctionType)
        .type(type)
        .alias(selectedField.getName())
        .distinct(distinct)
        .separator(separator)
        .build();
  }

  private Optional<KeyCriteria> createKeyCriteria(ObjectType<?> objectType, ExecutionStepInfo executionStepInfo) {
    if (TypeHelper.getTypeName(executionStepInfo.getFieldDefinition()
        .getType())
        .filter(typeName -> typeName.equals(objectType.getName()))
        .isEmpty()) {
      return Optional.empty();
    }

    var queryName = getQueryName(executionStepInfo);

    var arguments = executionStepInfo.getFieldDefinition()
        .getArguments();

    var argumentValues = executionStepInfo.getArguments();

    var queryKeys = queryName.map(name -> schema.getQueries()
        .get(name))
        .stream()
        .flatMap(q -> q.getKeys()
            .stream())
        .collect(Collectors.toList());

    var keys = arguments.stream()
        .filter(argument -> argument.getDefinition()
            .getAdditionalData()
            .containsKey(GraphQlConstants.IS_KEY_ARGUMENT))
        .filter(argument -> argumentValues.containsKey(argument.getName()))
        .map(argument -> createKeyCriteriaEntry(objectType, argumentValues, queryKeys, argument))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return Optional.of(keys)
        .filter(k -> k.size() > 0)
        .map(k -> KeyCriteria.builder()
            .values(k)
            .build());
  }

  private Map.Entry<List<ObjectField>, Object> createKeyCriteriaEntry(ObjectType<?> objectType,
      Map<String, Object> argumentMap, List<String> queryKeys, GraphQLArgument argument) {
    var queryKey = queryKeys.stream()
        .filter(key -> key.substring(key.lastIndexOf(".") + 1)
            .equals(argument.getName()))
        .findFirst()
        .orElseThrow();

    var fieldPath = createFieldPath(objectType, queryKey);
    var value = argumentMap.get(argument.getName());

    return Map.entry(fieldPath, value);
  }

  private Optional<GroupFilterCriteria> createFilterCriteria(ObjectType<?> objectType,
      Map<String, Object> filterArgument) {
    if (filterArgument == null) {
      return Optional.empty();
    }

    var andCriterias = filterArgument.keySet()
        .stream()
        .filter(filterName -> Objects.nonNull(filterArgument.get(filterName)))
        .filter(filterName -> !filterName.startsWith("_"))
        .map(filterName -> createScalarFieldFilterCriteria(objectType, filterArgument, filterName))
        .map(FilterCriteria.class::cast)
        .collect(Collectors.toList());

    var andGroup = GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(andCriterias)
        .build();

    var orGroup = filterArgument.keySet()
        .stream()
        .filter(filterName -> Objects.nonNull(filterArgument.get(filterName)))
        .filter(filterName -> Objects.equals(filterName, FilterConstants.OR_FIELD))
        .findFirst()
        .flatMap(filterName -> createFilterCriteria(objectType, MapHelper.getNestedMap(filterArgument, filterName)));

    return orGroup.map(groupFilterCriteria -> GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.OR)
        .filterCriterias(List.of(andGroup, groupFilterCriteria))
        .build())
        .or(() -> Optional.of(andGroup));

  }

  private ScalarFieldFilterCriteria createScalarFieldFilterCriteria(ObjectType<?> objectType,
      Map<String, Object> filterArgument, String filterName) {
    var filterConfiguration = objectType.getFilters()
        .get(filterName);

    var fieldPath = createFieldPath(objectType, filterConfiguration.getField());

    var filterValue = createFilterValue(filterArgument, filterName);

    return ScalarFieldFilterCriteria.builder()
        .filterType(filterConfiguration.getType())
        .isCaseSensitive(filterConfiguration.isCaseSensitive())
        .fieldPath(fieldPath)
        .value(filterValue)
        .build();
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

    var sortableByConfig = objectType.getSortableBy()
        .entrySet()
        .stream()
        .filter(entry -> formatSortEnumName(entry.getKey()).toUpperCase()
            .equals(sortArgument))
        .findFirst()
        .map(Map.Entry::getValue)
        .orElseThrow();

    return sortableByConfig.stream()
        .map(config -> SortCriteria.builder()
            .fieldPath(createFieldPath(objectType, config.getField()))
            .direction(config.getDirection())
            .build())
        .collect(Collectors.toList());
  }

  private String formatSortEnumName(String enumName) {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, enumName);
  }

  private ObjectType<?> getObjectType(GraphQLType type) {
    var rawType = unwrapAll(type);

    if (!(rawType instanceof GraphQLObjectType)) {
      throw illegalStateException("Not an object type.");
    }

    return schema.getObjectType(rawType.getName())
        .orElseThrow(() -> illegalStateException("No objectType with name '{}' found!", rawType.getName()));
  }
}
