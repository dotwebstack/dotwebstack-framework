package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.dotwebstack.framework.core.backend.filter.FilterCriteriaBuilder.newFilterCriteriaBuilder;
import static org.dotwebstack.framework.core.datafetchers.SortConstants.SORT_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateFunctionType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateScalarType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getSeparator;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isDistinct;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateValidator.validate;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.IS_BATCH_KEY_QUERY;
import static org.dotwebstack.framework.core.graphql.GraphQlConstants.KEY_FIELD;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.FieldPathHelper.createFieldPath;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getRequestStepInfo;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isCustomScalarField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.dotwebstack.framework.core.helpers.TypeHelper.unwrapConnectionType;

import com.google.common.base.CaseFormat;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.backend.filter.GroupFilterCriteria;
import org.dotwebstack.framework.core.datafetchers.ContextConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.graphql.GraphQlConstants;
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

  private final CustomValueFetcherDispatcher customValueFetcherDispatcher;

  public BackendRequestFactory(Schema schema, BackendExecutionStepInfo backendExecutionStepInfo,
      @Nullable CustomValueFetcherDispatcher customValueFetcherDispatcher) {
    this.schema = schema;
    this.backendExecutionStepInfo = backendExecutionStepInfo;
    this.customValueFetcherDispatcher = customValueFetcherDispatcher;
  }

  public CollectionRequest createCollectionRequest(ExecutionStepInfo executionStepInfo,
      DataFetchingFieldSelectionSet selectionSet) {
    var unwrappedType = unwrapConnectionType(executionStepInfo.getType());
    var objectType = getObjectType(unwrappedType);

    Map<String, Object> filterArgument = executionStepInfo.getArgument(FilterConstants.FILTER_ARGUMENT_NAME);

    var filterCriteria = ofNullable(filterArgument).map(argument -> newFilterCriteriaBuilder().objectType(objectType)
        .argument(argument)
        .maxDepth(schema.getSettings()
            .getMaxFilterDepth())
        .build())
        .map(GroupFilterCriteria.class::cast);

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
    var unwrappedType = unwrapConnectionType(executionStepInfo.getType());
    var objectType = getObjectType(unwrappedType);
    var keyCriterias =
        createKeyCriterias(objectType, executionStepInfo.getFieldDefinition(), executionStepInfo.getArguments());

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriterias(keyCriterias)
        .scalarFields(getScalarFields(selectionSet))
        .objectFields(getObjectFields(selectionSet, executionStepInfo))
        .objectListFields(getObjectListFields(selectionSet, executionStepInfo))
        .contextCriteria(createContextCriteria(getRequestStepInfo(executionStepInfo)))
        .aggregateObjectFields(getAggregateObjectFields(objectType, selectionSet))
        .build();
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField, ExecutionStepInfo executionStepInfo) {
    var objectType = getObjectType(unwrapConnectionType(selectedField.getType()));

    var fieldDefinition = selectedField.getFieldDefinitions()
        .stream()
        .findFirst()
        .orElseThrow();

    return ObjectRequest.builder()
        .objectType(objectType)
        .keyCriterias(createKeyCriterias(objectType, fieldDefinition, selectedField.getArguments()))
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
    var customScalarFields = selectionSet.getImmediateFields()
        .stream()
        .filter(isCustomScalarField)
        .filter(not(isIntrospectionField))
        .flatMap(selectedField -> {
          var fieldDefinition = selectedField.getFieldDefinitions()
              .stream()
              .findFirst()
              .orElseThrow()
              .getDefinition();

          var customValueFetcher = fieldDefinition.getAdditionalData()
              .get(GraphQlConstants.CUSTOM_FIELD_VALUEFETCHER);

          return customValueFetcherDispatcher.getSourceFieldNames(customValueFetcher)
              .stream()
              .map(fieldName -> FieldRequest.builder()
                  .name(fieldName)
                  .resultKey(fieldName)
                  .build());
        })
        .collect(Collectors.toList());

    var scalarFields = selectionSet.getImmediateFields()
        .stream()
        .filter(isScalarField)
        .filter(not(isIntrospectionField))
        .map(this::mapToFieldRequest)
        .collect(Collectors.toList());

    return Stream.concat(customScalarFields.stream(), scalarFields.stream())
        .collect(Collectors.toList());
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

          String resultKey = createResultKey(selectedField);

          return AggregateObjectRequest.builder()
              .objectField(objectField)
              .key(resultKey)
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

  private List<KeyCriteria> createKeyCriterias(ObjectType<?> objectType, GraphQLFieldDefinition fieldDefinition,
      Map<String, Object> argumentValues) {
    var additionalData = fieldDefinition.getDefinition()
        .getAdditionalData();

    // do not construct key criteria for batch queries
    if (additionalData.containsKey(IS_BATCH_KEY_QUERY)) {
      return List.of();
    }

    return fieldDefinition.getArguments()
        .stream()
        .filter(argument -> argument.getDefinition()
            .getAdditionalData()
            .containsKey(KEY_FIELD))
        .filter(argument -> argumentValues.containsKey(argument.getName()))
        .map(argument -> createKeyCriteria(objectType, argumentValues, argument))
        .collect(Collectors.toList());
  }

  private KeyCriteria createKeyCriteria(ObjectType<?> objectType, Map<String, Object> argumentMap,
      GraphQLArgument argument) {
    var keyField = argument.getDefinition()
        .getAdditionalData()
        .get(KEY_FIELD);
    var fieldPath = createFieldPath(objectType, keyField);
    var value = argumentMap.get(argument.getName());

    return KeyCriteria.builder()
        .fieldPath(fieldPath)
        .value(value)
        .build();
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
