package org.dotwebstack.framework.core.query;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateFunctionType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateScalarType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getSeparator;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isDistinct;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateValidator.validate;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;
import static org.dotwebstack.framework.core.helpers.TypeHelper.isConnectionType;

import com.google.common.collect.Sets;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.SelectedField;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.ContextConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.Feature;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.ContextConstants;
import org.dotwebstack.framework.core.datafetchers.SortConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterCriteriaParserFactory;
import org.dotwebstack.framework.core.datafetchers.paging.PagingDataFetcherContext;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.PagingCriteria;
import org.dotwebstack.framework.core.query.model.ScalarField;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.dotwebstack.framework.core.query.model.origin.Origin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(DotWebStackConfiguration.class)
public class RequestFactory {

  private static final List<String> KEY_ARGUMENTS_EXCLUDE = List.of(FilterConstants.FILTER_ARGUMENT_NAME,
      SortConstants.SORT_ARGUMENT_NAME, ContextConstants.CONTEXT_ARGUMENT_NAME);

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final FilterCriteriaParserFactory filterCriteriaParserFactory;

  private final TypeDefinitionRegistry typeDefinitionRegistry;

  public RequestFactory(DotWebStackConfiguration dotWebStackConfiguration,
      FilterCriteriaParserFactory filterCriteriaParserFactory, TypeDefinitionRegistry typeDefinitionRegistry) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.filterCriteriaParserFactory = filterCriteriaParserFactory;
    this.typeDefinitionRegistry = typeDefinitionRegistry;
  }

  private ExecutionStepInfo getExecutionStepInfo(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo;

    var isList = isList(unwrapNonNull(environment.getFieldType()));

    if (dotWebStackConfiguration.isFeatureEnabled(Feature.PAGING) && isList) {
      executionStepInfo = environment.getExecutionStepInfo()
          .getParent();
    } else {
      executionStepInfo = environment.getExecutionStepInfo();
    }
    return executionStepInfo;
  }

  public CollectionRequest createCollectionRequest(TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    ExecutionStepInfo executionStepInfo = getExecutionStepInfo(environment);

    return CollectionRequest.builder()
        .objectRequest(createObjectRequest(typeConfiguration, environment))
        .filterCriterias(createFilterCriterias(typeConfiguration, executionStepInfo))
        .sortCriterias(createSortCriterias(typeConfiguration, executionStepInfo))
        .pagingCriteria(createPagingCriteria(environment).orElse(null))
        .build();
  }

  public ObjectRequest createObjectRequest(TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {
    return createObjectRequest("", typeConfiguration, environment);
  }

  private ObjectRequest createObjectRequest(FieldConfigurationPair pair, DataFetchingEnvironment environment) {
    String fieldPathPrefix = pair.getSelectedField()
        .getFullyQualifiedName()
        .concat("/");
    TypeConfiguration<?> typeConfiguration = pair.getFieldConfiguration()
        .getTypeConfiguration();

    return createObjectRequest(fieldPathPrefix, typeConfiguration, environment);
  }

  public ObjectRequest createObjectRequest(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    DataFetchingFieldSelectionSet selectionSet = environment.getSelectionSet();

    List<ScalarField> scalarFields = getScalarFields(fieldPathPrefix, typeConfiguration, selectionSet);

    List<ObjectFieldConfiguration> objectFields = getObjectFields(fieldPathPrefix, typeConfiguration, environment);

    List<NestedObjectFieldConfiguration> nestedObjectFields =
        getNestedObjectFields(fieldPathPrefix, typeConfiguration, selectionSet);

    List<AggregateObjectFieldConfiguration> aggregateObjectFields =
        getAggregateObjectFields(fieldPathPrefix, typeConfiguration, selectionSet);

    List<KeyCriteria> keyCriterias = createKeyCriteria(environment);

    List<ObjectFieldConfiguration> collectionObjectFields =
        getCollectionObjectFields(fieldPathPrefix, typeConfiguration, environment);

    List<ContextCriteria> contextCriterias = createContextCriteria(environment);

    return ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .objectFields(objectFields)
        .nestedObjectFields(nestedObjectFields)
        .aggregateObjectFields(aggregateObjectFields)
        .collectionObjectFields(collectionObjectFields)
        .keyCriteria(keyCriterias)
        .contextCriterias(contextCriterias)
        .build();
  }

  private List<ContextCriteria> createContextCriteria(DataFetchingEnvironment environment) {
    ExecutionStepInfo executionStepInfo = getExecutionStepInfo(environment);

    if (executionStepInfo.getFieldDefinition()
        .getArguments()
        .stream()
        .map(GraphQLArgument::getType)
        .map(TypeHelper::getTypeName)
        .anyMatch(ContextConstants.CONTEXT_TYPE_NAME::equals)) {
      Map<String, Object> resolvedArguments =
          getNestedMap(executionStepInfo.getArguments(), ContextConstants.CONTEXT_ARGUMENT_NAME);

      return Optional.of(dotWebStackConfiguration.getContext())
          .map(ContextConfiguration::getFields)
          .stream()
          .flatMap(map -> map.entrySet()
              .stream())
          .map(entry -> ContextCriteria.builder()
              .field(entry.getKey())
              .value(resolvedArguments.get(entry.getKey()))
              .build())
          .collect(Collectors.toList());
    }

    return List.of();
  }

  private List<FilterCriteria> createFilterCriterias(TypeConfiguration<?> typeConfiguration,
      ExecutionStepInfo executionStepInfo) {

    Optional<GraphQLArgument> filterArgument = executionStepInfo.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> Objects.equals(argument.getName(), FilterConstants.FILTER_ARGUMENT_NAME))
        .findFirst();

    if (filterArgument.isPresent()) {
      GraphQLArgument argument = filterArgument.get();

      Map<String, Object> data = getNestedMap(executionStepInfo.getArguments(), argument.getName());

      var graphQlInputObjectType = Optional.of(argument)
          .map(GraphQLArgument::getType)
          .filter(GraphQLInputObjectType.class::isInstance)
          .map(GraphQLInputObjectType.class::cast)
          .orElseThrow(() -> illegalStateException("Filter argument not of type 'GraphQLInputObjectType'"));

      return getInputObjectFields(graphQlInputObjectType)
          .flatMap(inputObjectField -> filterCriteriaParserFactory.getFilterCriteriaParser(inputObjectField)
              .parse(typeConfiguration, inputObjectField, data)
              .stream())
          .collect(Collectors.toList());
    }

    return List.of();
  }

  private List<SortCriteria> createSortCriterias(TypeConfiguration<?> typeConfiguration,
      ExecutionStepInfo executionStepInfo) {
    Optional<GraphQLArgument> sortArgument = executionStepInfo.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> Objects.equals(argument.getName(), SortConstants.SORT_ARGUMENT_NAME))
        .findFirst();

    if (sortArgument.isPresent()) {
      GraphQLArgument argument = sortArgument.get();

      String orderEnumName = (String) executionStepInfo.getArguments()
          .get(argument.getName());

      Optional<String> sortableByConfigurationKey = typeConfiguration.getSortCriterias()
          .keySet()
          .stream()
          .filter(key -> key.toUpperCase()
              .equals(orderEnumName))
          .findFirst();

      return sortableByConfigurationKey.map(key -> typeConfiguration.getSortCriterias()
          .get(key))
          .orElseThrow(() -> illegalStateException("No sortCriterias found for enum '{}'", orderEnumName));
    }

    return List.of();
  }

  private Optional<PagingCriteria> createPagingCriteria(DataFetchingEnvironment environment) {
    if (dotWebStackConfiguration.isFeatureEnabled(Feature.PAGING)) {
      var context = (PagingDataFetcherContext) environment.getLocalContext();
      return Optional.of(PagingCriteria.builder()
          .offset(context.getOffset())
          .first(context.getFirst())
          .build());
    }
    return Optional.empty();
  }

  private Stream<GraphQLInputObjectField> getInputObjectFields(GraphQLInputObjectType inputObjectType) {
    return inputObjectType.getChildren()
        .stream()
        .filter(GraphQLInputObjectField.class::isInstance)
        .map(GraphQLInputObjectField.class::cast);
  }

  private List<AggregateObjectFieldConfiguration> getAggregateObjectFields(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingFieldSelectionSet selectionSet) {
    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, selectionSet)
        .filter(pair -> pair.getFieldConfiguration()
            .isAggregateField())
        .map(pair -> AggregateObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .aggregateFields(getAggregateFields(pair, selectionSet))
            .build())
        .collect(Collectors.toList());
  }

  private List<AggregateFieldConfiguration> getAggregateFields(FieldConfigurationPair fieldConfigurationPair,
      DataFetchingFieldSelectionSet selectionSet) {
    String fieldPathPrefix = fieldConfigurationPair.getSelectedField()
        .getFullyQualifiedName()
        .concat("/");
    TypeConfiguration<?> aggregateTypeConfiguration = fieldConfigurationPair.getFieldConfiguration()
        .getTypeConfiguration();
    return getAggregateFieldConfigurationPairs(fieldPathPrefix, aggregateTypeConfiguration, selectionSet)
        .map(this::createAggregateFieldConfiguration)
        .collect(Collectors.toList());
  }

  private AggregateFieldConfiguration createAggregateFieldConfiguration(FieldConfigurationPair fieldConfigurationPair) {
    var aggregateField = fieldConfigurationPair.getSelectedField();
    var aggregateFunctionType = getAggregateFunctionType(aggregateField);
    var type = getAggregateScalarType(aggregateField);
    var distinct = isDistinct(aggregateField);

    validate(fieldConfigurationPair.getFieldConfiguration(), aggregateField);

    String separator = null;
    if (aggregateFunctionType == AggregateFunctionType.JOIN) {
      separator = getSeparator(aggregateField);
    }

    return AggregateFieldConfiguration.builder()
        .field(fieldConfigurationPair.getFieldConfiguration())
        .aggregateFunctionType(aggregateFunctionType)
        .type(type)
        .alias(aggregateField.getName())
        .distinct(distinct)
        .separator(separator)
        .build();
  }

  private List<KeyCriteria> createKeyCriteria(DataFetchingEnvironment environment) {
    return environment.getArguments()
        .entrySet()
        .stream()
        .filter(argument -> !KEY_ARGUMENTS_EXCLUDE.contains(argument.getKey()))
        .map(entry -> KeyCriteria.builder()
            .values(Map.of(entry.getKey(), entry.getValue()))
            .build())
        .collect(Collectors.toList());
  }

  private List<ScalarField> getScalarFields(String fieldPathPrefix,
      TypeConfiguration<? extends AbstractFieldConfiguration> typeConfiguration,
      DataFetchingFieldSelectionSet selectionSet) {

    List<AbstractFieldConfiguration> selectedFields = getSelectedFields(fieldPathPrefix, selectionSet).stream()
        .map(selectedField -> (AbstractFieldConfiguration) typeConfiguration.getFields()
            .get(selectedField.getName()))
        .collect(Collectors.toList());

    List<ScalarField> requested = selectedFields.stream()
        .filter(AbstractFieldConfiguration::isScalarField)
        .map(field -> ScalarField.builder()
            .field(field)
            .origins(Sets.newHashSet(Origin.requested()))
            .build())
        .collect(Collectors.toList());

    // add referenced scalar field
    List<ScalarField> referred = selectedFields.stream()
        .filter(field -> !field.isScalarField())
        .flatMap(field -> typeConfiguration.getReferencedFields(field.getName())
            .stream())
        .filter(field -> requested.stream()
            .noneMatch(scalarField -> field.getName()
                .equals(scalarField.getName())))
        .map(field -> ScalarField.builder()
            .field(field)
            .origins(Sets.newHashSet(Origin.requested()))
            .build())
        .collect(Collectors.toList());

    return Stream.concat(requested.stream(), referred.stream())
        .collect(Collectors.toList());
  }

  private List<ObjectFieldConfiguration> getCollectionObjectFields(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment.getSelectionSet())
        .filter(pair -> pair.getFieldConfiguration()
            .isObjectField())
        .filter(pair -> pair.getFieldConfiguration()
            .isList())
        .map(pair -> ObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .objectRequest(createObjectRequest(pair, environment))
            .build())
        .collect(Collectors.toList());
  }

  private List<ObjectFieldConfiguration> getObjectFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment.getSelectionSet())
        .filter(pair -> pair.getFieldConfiguration()
            .isObjectField())
        .filter(pair -> !pair.getFieldConfiguration()
            .isList())
        .map(pair -> ObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .objectRequest(createObjectRequest(pair, environment))
            .build())
        .collect(Collectors.toList());
  }

  private List<NestedObjectFieldConfiguration> getNestedObjectFields(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingFieldSelectionSet selectionSet) {
    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, selectionSet)
        .filter(pair -> pair.getFieldConfiguration()
            .isNestedObjectField())
        .map(pair -> NestedObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .scalarFields(getScalarFields(pair.getSelectedField()
                .getFullyQualifiedName()
                .concat("/"),
                pair.getFieldConfiguration()
                    .getTypeConfiguration(),
                selectionSet))
            .build())
        .collect(Collectors.toList());
  }

  private Stream<FieldConfigurationPair> getFieldConfigurationPairs(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingFieldSelectionSet selectionSet) {
    return getSelectedFields(fieldPathPrefix, selectionSet).stream()
        .map(selectedField -> FieldConfigurationPair.builder()
            .selectedField(selectedField)
            .fieldConfiguration(typeConfiguration.getFields()
                .get(selectedField.getName()))
            .build());
  }

  private Stream<FieldConfigurationPair> getAggregateFieldConfigurationPairs(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingFieldSelectionSet selectionSet) {
    return getSelectedFields(fieldPathPrefix, selectionSet).stream()
        .map(selectedField -> FieldConfigurationPair.builder()
            .selectedField(selectedField)
            .fieldConfiguration(typeConfiguration.getFields()
                .get(selectedField.getArguments()
                    .get(FIELD_ARGUMENT)
                    .toString()))
            .build());
  }

  private List<SelectedField> getSelectedFields(String fieldPathPrefix, DataFetchingFieldSelectionSet selectionSet) {
    return selectionSet.getFields(fieldPathPrefix.concat("*.*"))
        .stream()
        .filter(selectedField -> !selectedField.getName()
            .startsWith("__"))
        .filter(selectedField -> !isConnectionType(typeDefinitionRegistry, selectedField.getFieldDefinitions()
            .stream()
            .findFirst()
            .orElseThrow(() -> illegalStateException("No field definition found for selected field."))
            .getType()))
        .collect(Collectors.toList());
  }

  @Data
  @Builder
  private static class FieldConfigurationPair {
    private final SelectedField selectedField;

    private final AbstractFieldConfiguration fieldConfiguration;
  }
}
