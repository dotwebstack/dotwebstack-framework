package org.dotwebstack.framework.core.backend;

import static graphql.schema.GraphQLTypeUtil.unwrapAll;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
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
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.getRequestStepInfo;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isCustomValueField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isIntrospectionField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isObjectListField;
import static org.dotwebstack.framework.core.helpers.GraphQlHelper.isScalarField;
import static org.dotwebstack.framework.core.helpers.TypeHelper.QUERY_TYPE_NAME;
import static org.dotwebstack.framework.core.helpers.TypeHelper.unwrapConnectionType;

import com.google.common.base.CaseFormat;
import graphql.execution.ExecutionStepInfo;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.language.TypeName;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInterfaceType;
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
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.core.CustomValueFetcherDispatcher;
import org.dotwebstack.framework.core.OnLocalSchema;
import org.dotwebstack.framework.core.backend.filter.GroupFilterCriteria;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.model.ObjectType;
import org.dotwebstack.framework.core.model.Schema;
import org.dotwebstack.framework.core.query.model.AggregateField;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectRequest;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.FieldRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.RequestContext;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.UnionObjectRequest;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(OnLocalSchema.class)
public class BackendRequestFactory {

  private static final String TYPE_CONDITION = "typeCondition";

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
    var filterCriteria = getFilterCriteria(filterArgument, objectType);

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(executionStepInfo, selectionSet))
        .filterCriteria(filterCriteria.orElse(null))
        .sortCriterias(createSortCriteria(executionStepInfo, objectType))
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
    if (unwrapAll(executionStepInfo.getType()) instanceof GraphQLInterfaceType interfaceType) {
      return createUnionObjectRequest(executionStepInfo, selectionSet, interfaceType);
    }

    var unwrappedType = unwrapConnectionType(executionStepInfo.getType());
    var objectType = getObjectType(unwrappedType);
    return createObjectRequest(executionStepInfo, selectionSet, objectType);
  }

  public ObjectRequest createObjectRequest(ExecutionStepInfo executionStepInfo,
      DataFetchingFieldSelectionSet selectionSet, ObjectType<?> objectType) {

    var keyCriterias =
        createKeyCriterias(objectType, executionStepInfo.getFieldDefinition(), executionStepInfo.getArguments());

    return SingleObjectRequest.builder()
        .objectType(objectType)
        .keyCriterias(keyCriterias)
        .scalarFields(getScalarFields(selectionSet, objectType.getName()))
        .objectFields(getObjectFields(selectionSet, executionStepInfo, objectType.getName()))
        .objectListFields(getObjectListFields(selectionSet, executionStepInfo))
        .contextCriteria(createContextCriteria(schema, getRequestStepInfo(executionStepInfo)))
        .aggregateObjectFields(getAggregateObjectFields(objectType, selectionSet))
        .build();
  }

  private ObjectRequest createObjectRequest(SelectedField selectedField, ExecutionStepInfo executionStepInfo) {
    var unwrappedType = unwrapConnectionType(selectedField.getType());

    if (unwrapAll(unwrappedType) instanceof GraphQLInterfaceType interfaceType) {
      return createUnionObjectRequest(executionStepInfo, selectedField.getSelectionSet(), interfaceType);
    }
    var objectType = getObjectType(unwrappedType);

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

  private UnionObjectRequest createUnionObjectRequest(ExecutionStepInfo executionStepInfo,
      DataFetchingFieldSelectionSet selectionSet, GraphQLInterfaceType interfaceType) {
    var typeNames = new ArrayList<String>();

    if (executionStepInfo.getField() != null) {
      typeNames.addAll(getTypeConditionNamesFromFields(executionStepInfo.getField()
          .getFields()));
    }

    var interfaceNames = getAllImplementedInterfaceNames(interfaceType.getName());
    var objectTypes = getRequestedObjectTypes(interfaceNames, typeNames);

    var objectRequests = objectTypes.stream()
        .map(objectType -> createObjectRequest(executionStepInfo, selectionSet, objectType))
        .map(SingleObjectRequest.class::cast)
        .toList();

    return UnionObjectRequest.builder()
        .objectRequests(objectRequests)
        .build();
  }

  /**
   * Returns all interface names which are inherited from the implemented interface.
   *
   * @param baseInterfaceName - interface which is implemented
   * @return All implicitly implemented Interfaces
   */
  private List<String> getAllImplementedInterfaceNames(String baseInterfaceName) {
    return Stream.concat(schema.getInterfaces()
        .values()
        .stream()
        .filter(iface -> iface.getImplements()
            .contains(baseInterfaceName))
        .map(ObjectType::getName), Stream.of(baseInterfaceName))
        .toList();
  }

  private List<ObjectType<? extends ObjectField>> getRequestedObjectTypes(List<String> interfaceNames,
      List<String> typeNames) {
    return schema.getObjectTypes()
        .values()
        .stream()
        .filter(objectType -> {
          if (interfaceNames.contains(objectType.getName())) {
            return false;
          }
          return objectType.getImplements()
              .stream()
              .anyMatch(interfaceNames::contains);
        })
        .filter(objectType -> {
          if (typeNames.isEmpty()) {
            return true;
          } else {
            return typeNames.contains(objectType.getName());
          }
        })
        .toList();
  }

  private List<String> getTypeConditionNamesFromFields(List<Field> fields) {
    return fields.stream()
        .map(field -> field.getSelectionSet()
            .getSelections()
            .stream()
            .map(this::getFieldTypeConditionName)
            .filter(Objects::nonNull)
            .toList())
        .flatMap(List::stream)
        .toList();
  }

  private String getFieldTypeConditionName(Selection<?> field) {
    var typeCondition = field.getNamedChildren()
        .getChildren(TYPE_CONDITION);
    if (!typeCondition.isEmpty()) {
      return ((TypeName) typeCondition.get(0)).getName();
    } else {
      return null;
    }
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
    return getScalarFields(selectionSet, StringUtils.EMPTY);
  }

  private List<FieldRequest> getScalarFields(DataFetchingFieldSelectionSet selectionSet, String objectName) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isScalarField)
        .filter(not(isIntrospectionField))
        .filter(selectedField -> selectedFieldIsPartOfObject(objectName, selectedField))
        .flatMap(this::mapScalarFieldToFieldRequests)
        .collect(toCollection(ArrayList::new));
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

  private Map<FieldRequest, ObjectRequest> getObjectFields(DataFetchingFieldSelectionSet selectionSet,
      ExecutionStepInfo executionStepInfo) {
    return getObjectFields(selectionSet, executionStepInfo, StringUtils.EMPTY);
  }

  private Map<FieldRequest, ObjectRequest> getObjectFields(DataFetchingFieldSelectionSet selectionSet,
      ExecutionStepInfo executionStepInfo, String objectName) {
    return selectionSet.getImmediateFields()
        .stream()
        .filter(isObjectField)
        .filter(selectedField -> selectedFieldIsPartOfObject(objectName, selectedField))
        .collect(Collectors.toMap(this::mapToFieldRequest,
            selectedField -> createObjectRequest(selectedField, executionStepInfo)));
  }

  private boolean selectedFieldIsPartOfObject(String parentObjectName, SelectedField selectedField) {
    if (parentObjectName.isBlank()) {
      return true;
    } else if (selectedField.getFullyQualifiedName() != null) {
      if (selectedField.getFullyQualifiedName()
          .contains(".")
          && selectedField.getFullyQualifiedName()
              .contains(parentObjectName)) {
        return true;
      } else {
        return !selectedField.getFullyQualifiedName()
            .contains(".");
      }
    } else {
      return true;
    }
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
        .toList();
  }

  private List<AggregateField> getAggregateFields(ObjectType<?> objectType,
      DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getImmediateFields()
        .stream()
        .map(selectedField -> createAggregateField(objectType, selectedField))
        .toList();
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
        .toList();
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
    var queryName = Optional.of(executionStepInfo)
        .filter(requestStepInfo -> requestStepInfo.getObjectType()
            .getName()
            .equals(QUERY_TYPE_NAME))
        .map(ExecutionStepInfo::getFieldDefinition)
        .map(GraphQLFieldDefinition::getName);

    Map<String, List<SortableByConfiguration>> sortableBy = new HashMap<>();

    queryName.ifPresent(name -> sortableBy.putAll(schema.getQueries()
        .get(name)
        .getSortableBy()));

    sortableBy.putAll(objectType.getSortableBy());

    if (sortableBy.isEmpty()) {
      return List.of();
    }

    var sortArgument = executionStepInfo.getArgument(SORT_ARGUMENT_NAME);

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
        .toList();
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

    if (!(rawType instanceof GraphQLObjectType) && !(rawType instanceof GraphQLInterfaceType)) {
      throw illegalStateException("Not an object type.");
    }

    return schema.getObjectTypeOrInterface(rawType.getName())
        .orElseThrow(() -> illegalStateException("No objectType with name '{}' found!", rawType.getName()));
  }
}
