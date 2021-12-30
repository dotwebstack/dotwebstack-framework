package org.dotwebstack.framework.core.backend;

import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateFunctionType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateScalarType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getSeparator;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isDistinct;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateValidator.validate;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getRequestStepInfo;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.dotwebstack.framework.core.helpers.MapHelper.resolveSuppliers;
import static org.dotwebstack.framework.core.helpers.ObjectHelper.castToMap;

import com.google.common.base.CaseFormat;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.dotwebstack.framework.core.config.FilterType;
import org.dotwebstack.framework.core.datafetchers.ContextConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterOperator;
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

    var filterCriteria = createFilterCriterias(objectType,
        executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME), new ArrayList<>());

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(executionStepInfo, selectionSet))
        .filterCriteria(filterCriteria.map(GroupFilterCriteria.class::cast)
            .orElse(null))
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
            .getArguments(), executionStepInfo.getArguments()).orElse(null))
        .scalarFields(getScalarFields(selectionSet))
        .objectFields(getObjectFields(selectionSet, executionStepInfo))
        .objectListFields(getObjectListFields(selectionSet, executionStepInfo))
        .contextCriteria(createContextCriteria(getRequestStepInfo(executionStepInfo)))
        .aggregateObjectFields(getAggregateObjectFields(objectType, selectionSet))
        .build();
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField, ExecutionStepInfo executionStepInfo) {
    ObjectType<?> objectType = getObjectType(TypeHelper.unwrapConnectionType(selectedField.getType()));

    var arguments = selectedField.getFieldDefinitions()
        .get(0)
        .getArguments();

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriteria(createKeyCriteria(arguments, selectedField.getArguments()).orElse(null))
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

  private Optional<KeyCriteria> createKeyCriteria(List<GraphQLArgument> arguments, Map<String, Object> argumentMap) {
    var keys = arguments.stream()
        .filter(argument -> argument.getDefinition()
            .getAdditionalData()
            .containsKey(GraphQlConstants.IS_KEY_ARGUMENT))
        .filter(argument -> argumentMap.containsKey(argument.getName()))
        .map(argument -> Map.entry(argument.getName(), argumentMap.get(argument.getName())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));

    return Optional.of(keys)
        .filter(k -> k.size() > 0)
        .map(k -> KeyCriteria.builder()
            .values(k)
            .build());
  }

  private Optional<FilterCriteria> createFilterCriterias(ObjectType<?> objectType, Map<String, Object> filterArgument,
      List<ObjectField> parentFieldPath) {
    if (filterArgument == null) {
      return Optional.empty();
    }

    var andCriterias = filterArgument.keySet()
        .stream()
        .filter(filterName -> Objects.nonNull(filterArgument.get(filterName)))
        .filter(filterName -> !filterName.equals(FilterConstants.OR_FIELD))
        .flatMap(filterName -> createFilterCriteria(objectType, Map.entry(filterName, filterArgument.get(filterName)),
            parentFieldPath).stream())
        .map(FilterCriteria.class::cast)
        .collect(Collectors.toList());

    FilterCriteria andGroup = GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.AND)
        .filterCriterias(andCriterias)
        .build();

    var orGroup = filterArgument.keySet()
        .stream()
        .filter(filterName -> Objects.nonNull(filterArgument.get(filterName)))
        .filter(filterName -> Objects.equals(filterName, FilterConstants.OR_FIELD))
        .findFirst()
        .flatMap(filterName -> createFilterCriterias(objectType, MapHelper.getNestedMap(filterArgument, filterName),
            new ArrayList<>()));

    return orGroup.map(groupFilterCriteria -> (FilterCriteria) GroupFilterCriteria.builder()
        .logicalOperator(GroupFilterOperator.OR)
        .filterCriterias(List.of(andGroup, groupFilterCriteria))
        .build())
        .or(() -> Optional.of(andGroup));

  }

  private Optional<FilterCriteria> createFilterCriteria(ObjectType<?> objectType, Map.Entry<String, Object> filterEntry,
      List<ObjectField> parentFieldPath) {
    var filterName = filterEntry.getKey();

    if (FilterOperator.EXISTS.toString()
        .equalsIgnoreCase(filterName)) {
      return Optional.of(ScalarFieldFilterCriteria.builder()
          .filterType(FilterType.EXACT)
          .fieldPath(parentFieldPath)
          .value(Map.of(filterEntry.getKey(), filterEntry.getValue()))
          .build());
    }

    var filterConfiguration = objectType.getFilters()
        .get(filterName);

    var field = objectType.getField(filterConfiguration.getField());

    var targetType = field.getTargetType();

    var fieldPath = new ArrayList<>(parentFieldPath);
    fieldPath.add(field);

    if (targetType != null) {
      return createFilterCriterias(targetType, castToMap(filterEntry.getValue()), fieldPath);
    }

    var filterValue = createFilterValue(filterEntry);

    return Optional.of(ScalarFieldFilterCriteria.builder()
        .filterType(filterConfiguration.getType())
        .isCaseSensitive(filterConfiguration.isCaseSensitive())
        .fieldPath(fieldPath)
        .value(filterValue)
        .build());



  }

  private Map<String, Object> createFilterValue(Map.Entry<String, Object> entry) {
    if (entry.getValue() instanceof Boolean) {
      return Map.of(FilterConstants.EQ_FIELD, entry.getValue());
    }

    if (entry.getValue() instanceof Map) {
      return resolveSuppliers(castToMap(entry.getValue()));
    }

    throw illegalArgumentException("Expected entry value of type 'java.util.Map' but got '{}'", entry.getValue()
        .getClass()
        .getName());
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
            .fieldPath(createObjectFieldPath(objectType, config.getField()))
            .direction(config.getDirection())
            .build())
        .collect(Collectors.toList());
  }

  private String formatSortEnumName(String enumName) {
    return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, enumName);
  }

  private List<ObjectField> createObjectFieldPath(ObjectType<?> objectType, String path) {
    var current = objectType;
    var fieldPath = new ArrayList<ObjectField>();

    for (var segment : path.split("\\.")) {
      var field = Optional.ofNullable(current)
          .map(o -> o.getField(segment))
          .orElseThrow();

      current = Optional.ofNullable(field.getTargetType())
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
