package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.backend.filter.FilterCriteriaBuilder.newFilterCriteriaBuilder;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateFunctionType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateScalarType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getSeparator;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isDistinct;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateValidator.validate;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_BATCH_KEY_QUERY;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.KEY_FIELD;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.KEY_PATH;
import static org.dotwebstack.framework.core.helpers.ContextCriteriaHelper.createContextCriteria;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.createFieldPath;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getAdditionalData;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getQueryName;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getRequestStepInfo;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isCustomValueField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.dotwebstack.framework.core.helpers.TypeHelper.unwrapConnectionType;

import com.google.common.base.CaseFormat;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.backend.filter.GroupFilterCriteria;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Query;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.UnionObjectRequest;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class BackendRequestFactory {

  private final Schema schema;

  private final BackendExecutionStepInfo backendExecutionStepInfo;

  private final CustomValueFetcherDispatcher customValueFetcherDispatcher;

  public BackendRequestFactory(Schema schema, BackendExecutionStepInfo backendExecutionStepInfo,
      @Nullable CustomValueFetcherDispatcher customValueFetcherDispatcher) {
    this.schema = schema;
    this.backendExecutionStepInfo = backendExecutionStepInfo;
    this.customValueFetcherDispatcher = customValueFetcherDispatcher;
  }

  public CollectionRequest createCollectionRequest(ExecutionStepInfo executionStepInfo,
      DataFetchingFieldSelectionSet selectionSet) {
    //TODO: fixme - omgaan met interfaces
//    var unwrappedType = unwrapConnectionType(executionStepInfo.getType());
//    var objectType = getObjectType(unwrappedType);

//    Map<String, Object> filterArgument = executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME);

//    Optional<GroupFilterCriteria> filterCriteria = getFilterCriteria(filterArgument, objectType);

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(executionStepInfo, selectionSet))
        .filterCriteria(null)
        .sortCriterias(Collections.emptyList())
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
    var unwrappedType = unwrapConnectionType(executionStepInfo.getType());



    if (unwrapAll(executionStepInfo.getType()) instanceof GraphQLInterfaceType interfaceType) {
      var x = schema.getObjectTypes().values().stream().filter(objectType -> objectType.getImplements().contains(interfaceType.getName())).toList();

      var y = x.stream().map(objectType ->
        createObjectRequest(executionStepInfo, selectionSet, objectType)
      ).map(SingleObjectRequest.class::cast).toList();

      return UnionObjectRequest.builder()
          .objectRequests(y).build();
    }
    var objectType = getObjectType(unwrappedType);
    return createObjectRequest(executionStepInfo, selectionSet, objectType);
  }

  public ObjectRequest createObjectRequest(ExecutionStepInfo executionStepInfo,
      DataFetchingFieldSelectionSet selectionSet, ObjectType objectType) {

    var keyCriterias =
        createKeyCriterias(objectType, executionStepInfo.getFieldDefinition(), executionStepInfo.getArguments());

    return SingleObjectRequest.builder()
        .objectType(objectType)
        .keyCriterias(keyCriterias)
        .scalarFields(getScalarFields(selectionSet))
        .objectFields(getObjectFields(selectionSet, executionStepInfo))
        .objectListFields(getObjectListFields(selectionSet, executionStepInfo))
        .contextCriteria(createContextCriteria(schema, getRequestStepInfo(executionStepInfo)))
        .aggregateObjectFields(getAggregateObjectFields(objectType, selectionSet))
        .build();
  }

  private SingleObjectRequest createObjectRequest(SelectedField selectedField, ExecutionStepInfo executionStepInfo) {
    var objectType = getObjectType(unwrapConnectionType(selectedField.getType()));

    var fieldDefinition = selectedField.getFieldDefinitions()
        .stream()
        .findFirst()
        .orElseThrow();

    return SingleObjectRequest.builder()
        .objectType(objectType)
        .keyCriterias(createKeyCriterias(objectType, fieldDefinition, selectedField.getArguments()))
        .scalarFields(getScalarFields(selectedField.getSelectionSet()))
        .objectFields(getObjectFields(selectedField.getSelectionSet(), executionStepInfo))
        .objectListFields(getObjectListFields(selectedField.getSelectionSet(), executionStepInfo))
        .contextCriteria(createContextCriteria(schema, getRequestStepInfo(executionStepInfo)))
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

  private List<FieldRequest> getScalarFields(DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isScalarField)
        .filter(not(isIntrospectionField))
        .flatMap(this::mapScalarFieldToFieldRequests)
        .collect(Collectors.toList());
  }

  private Stream<FieldRequest> mapScalarFieldToFieldRequests(SelectedField selectedField) {
    if (isCustomValueField.test(selectedField)) {
      return getAdditionalData(selectedField, CUSTOM_FIELD_VALUEFETCHER).stream()
          .flatMap(
              customValueFetcher -> requireNonNull(customValueFetcherDispatcher).getSourceFieldNames(customValueFetcher)
                  .stream())
          .map(fieldName -> FieldRequest.builder()
              .name(fieldName)
              .resultKey(fieldName)
              .build());
    }

    return Stream.of(mapToFieldRequest(selectedField));
  }

  private FieldRequest mapToFieldRequest(SelectedField selectedField) {
    String resultKey = createResultKey(selectedField);

    return FieldRequest.builder()
        .name(selectedField.getName())
        .resultKey(resultKey)
        .isList(GraphQLTypeUtil.isList(selectedField.getType()))
        .arguments(selectedField.getArguments())
        .build();
  }

  private Map<FieldRequest, SingleObjectRequest> getObjectFields(DataFetchingFieldSelectionSet selectionSet,
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

          String resultKey = createResultKey(selectedField);

          Map<String, Object> filterArgument = (Map<String, Object>) selectedField.getArguments()
              .get(FilterConstants.FILTER_ARGUMENT_NAME);

          Optional<GroupFilterCriteria> filterCriteria = getFilterCriteria(filterArgument, aggregationObjectType);

          return AggregateObjectRequest.builder()
              .objectField(objectField)
              .key(resultKey)
              .filterCriteria(filterCriteria.orElse(null))
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

    String resultKey = createResultKey(selectedField);

    return AggregateField.builder()
        .field(objectField)
        .functionType(aggregateFunctionType)
        .type(type)
        .alias(resultKey)
        .distinct(distinct)
        .separator(separator)
        .build();
  }

  private Optional<GroupFilterCriteria> getFilterCriteria(Map<String, Object> filterArgument,
      ObjectType<?> objectType) {
    return ofNullable(filterArgument).map(argument -> newFilterCriteriaBuilder().objectType(objectType)
        .argument(argument)
        .maxDepth(schema.getSettings()
            .getMaxFilterDepth())
        .build())
        .map(GroupFilterCriteria.class::cast);
  }

  private List<KeyCriteria> createKeyCriterias(ObjectType<?> objectType, GraphQLFieldDefinition fieldDefinition,
      Map<String, Object> argumentValues) {
    var additionalData = requireNonNull(fieldDefinition.getDefinition()).getAdditionalData();

    // do not construct key criteria for batch queries
    if (additionalData.containsKey(IS_BATCH_KEY_QUERY)) {
      return List.of();
    }

    return fieldDefinition.getArguments()
        .stream()
        .filter(argument -> requireNonNull(argument.getDefinition()).getAdditionalData()
            .containsKey(KEY_FIELD))
        .filter(argument -> argumentValues.containsKey(argument.getName()))
        .map(argument -> createKeyCriteria(objectType, argumentValues, argument))
        .collect(Collectors.toList());
  }

  private KeyCriteria createKeyCriteria(ObjectType<?> objectType, Map<String, Object> argumentMap,
      GraphQLArgument argument) {
    var keyPath = requireNonNull(argument.getDefinition()).getAdditionalData()
        .get(KEY_PATH);

    var fieldPath = createFieldPath(objectType, keyPath);
    var value = argumentMap.get(argument.getName());

    return KeyCriteria.builder()
        .fieldPath(fieldPath)
        .value(value)
        .build();
  }

  private List<SortCriteria> createSortCriteria(ExecutionStepInfo executionStepInfo, ObjectType<?> objectType) {
    var sortArgument = executionStepInfo.getArgument(SORT_ARGUMENT_NAME);

    var sortableBy = getQueryName(executionStepInfo).map(queryName -> schema.getQueries()
        .get(queryName))
        .filter(query -> !query.getSortableBy()
            .isEmpty())
        .map(Query::getSortableBy)
        .orElse(objectType.getSortableBy());

    if (sortableBy.isEmpty()) {
      return List.of();
    }

    var sortableByConfig = sortableBy.entrySet()
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

  private String createResultKey(SelectedField selectedField) {
    return selectedField.getAlias() == null ? selectedField.getName()
        : String.format("%s.%s", selectedField.getName(), selectedField.getAlias());
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
