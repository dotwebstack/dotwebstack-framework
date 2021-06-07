package org.dotwebstack.framework.core.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateFunctionType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getAggregateScalarType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.getSeparator;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isDistinct;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateValidator.validate;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.SelectedField;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.SortConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterCriteriaParserFactory;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.Origin;
import org.dotwebstack.framework.core.query.model.PagingCriteria;
import org.dotwebstack.framework.core.query.model.ScalarField;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.springframework.stereotype.Component;

@Component
public class RequestFactory {

  private final FilterCriteriaParserFactory filterCriteriaParserFactory;

  public RequestFactory(FilterCriteriaParserFactory filterCriteriaParserFactory) {
    this.filterCriteriaParserFactory = filterCriteriaParserFactory;
  }

  public CollectionRequest createCollectionRequest(TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment, boolean addLimit) {

    var collectionQueryBuilder = CollectionRequest.builder()
        .objectRequest(createObjectRequest(typeConfiguration, environment))
        .filterCriterias(createFilterCriterias(typeConfiguration, environment))
        .sortCriterias(createSortCriterias(typeConfiguration, environment));
    if (addLimit) {
      collectionQueryBuilder.pagingCriteria(PagingCriteria.builder()
          .page(0)
          .pageSize(10)
          .build());
    }

    return collectionQueryBuilder.build();
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

    List<ScalarField> scalarFields = getScalarFields(fieldPathPrefix, typeConfiguration, environment);
    List<ObjectFieldConfiguration> objectFields = getObjectFields(fieldPathPrefix, typeConfiguration, environment);
    List<NestedObjectFieldConfiguration> nestedObjectFields =
        getNestedObjectFields(fieldPathPrefix, typeConfiguration, environment);
    List<AggregateObjectFieldConfiguration> aggregateObjectFields =
        getAggregateObjectFields(fieldPathPrefix, typeConfiguration, environment);

    List<KeyCriteria> keyCriterias = createKeyCriteria(environment);

    List<ObjectFieldConfiguration> collectionObjectFields =
        getCollectionObjectFields(fieldPathPrefix, typeConfiguration, environment);

    return ObjectRequest.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .objectFields(objectFields)
        .nestedObjectFields(nestedObjectFields)
        .aggregateObjectFields(aggregateObjectFields)
        .collectionObjectFields(collectionObjectFields)
        .keyCriteria(keyCriterias)
        .build();
  }

  private List<FilterCriteria> createFilterCriterias(TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    Optional<GraphQLArgument> filterArgument = environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> Objects.equals(argument.getName(), FilterConstants.FILTER_ARGUMENT_NAME))
        .findFirst();

    if (filterArgument.isPresent()) {
      GraphQLArgument argument = filterArgument.get();

      Map<String, Object> data = getNestedMap(environment.getArguments(), argument.getName());

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
      DataFetchingEnvironment environment) {
    Optional<GraphQLArgument> sortArgument = environment.getFieldDefinition()
        .getArguments()
        .stream()
        .filter(argument -> Objects.equals(argument.getName(), SortConstants.SORT_ARGUMENT_NAME))
        .findFirst();

    if (sortArgument.isPresent()) {
      GraphQLArgument argument = sortArgument.get();

      String orderEnumName = (String) environment.getArguments()
          .get(argument.getName());

      Optional<String> sortableByConfigurationKey = typeConfiguration.getSortableBy()
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

  private Stream<GraphQLInputObjectField> getInputObjectFields(GraphQLInputObjectType inputObjectType) {
    return inputObjectType.getChildren()
        .stream()
        .filter(GraphQLInputObjectField.class::isInstance)
        .map(GraphQLInputObjectField.class::cast);
  }

  private List<AggregateObjectFieldConfiguration> getAggregateObjectFields(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment)
        .filter(pair -> pair.getFieldConfiguration()
            .isAggregateField())
        .map(pair -> AggregateObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .aggregateFields(getAggregateFields(pair, environment))
            .build())
        .collect(Collectors.toList());
  }

  private List<AggregateFieldConfiguration> getAggregateFields(FieldConfigurationPair fieldConfigurationPair,
      DataFetchingEnvironment environment) {
    String fieldPathPrefix = fieldConfigurationPair.getSelectedField()
        .getFullyQualifiedName()
        .concat("/");
    TypeConfiguration<?> aggregateTypeConfiguration = fieldConfigurationPair.getFieldConfiguration()
        .getTypeConfiguration();
    return getAggregateFieldConfigurationPairs(fieldPathPrefix, aggregateTypeConfiguration, environment)
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
        .filter(argument ->
        // TODO: fix sort constant
        !Objects.equals(argument.getKey(), FilterConstants.FILTER_ARGUMENT_NAME)
            && !Objects.equals(argument.getKey(), SortConstants.SORT_ARGUMENT_NAME))
        .map(entry -> KeyCriteria.builder()
            .values(Map.of(entry.getKey(), entry.getValue()))
            .build())
        .collect(Collectors.toList());
  }

  private List<ScalarField> getScalarFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {
    return getSelectedFields(fieldPathPrefix, environment).stream()
        .map(selectedField -> typeConfiguration.getFields()
            .get(selectedField.getName()))
        .filter(AbstractFieldConfiguration::isScalarField)
        .map(field -> ScalarField.builder()
            .field(field)
            .origins(new HashSet<>(Set.of(Origin.REQUESTED)))
            .build())
        .collect(Collectors.toList());
  }

  private List<ObjectFieldConfiguration> getCollectionObjectFields(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment)
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

    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment)
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
      TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment)
        .filter(pair -> pair.getFieldConfiguration()
            .isNestedObjectField())
        .map(pair -> NestedObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .scalarFields(getScalarFields(pair.getSelectedField()
                .getFullyQualifiedName()
                .concat("/"),
                pair.getFieldConfiguration()
                    .getTypeConfiguration(),
                environment))
            .build())
        .collect(Collectors.toList());
  }

  private Stream<FieldConfigurationPair> getFieldConfigurationPairs(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getSelectedFields(fieldPathPrefix, environment).stream()
        .map(selectedField -> FieldConfigurationPair.builder()
            .selectedField(selectedField)
            .fieldConfiguration(typeConfiguration.getFields()
                .get(selectedField.getName()))
            .build());
  }

  private Stream<FieldConfigurationPair> getAggregateFieldConfigurationPairs(String fieldPathPrefix,
      TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getSelectedFields(fieldPathPrefix, environment).stream()
        .map(selectedField -> FieldConfigurationPair.builder()
            .selectedField(selectedField)
            .fieldConfiguration(typeConfiguration.getFields()
                .get(selectedField.getArguments()
                    .get(FIELD_ARGUMENT)
                    .toString()))
            .build());
  }

  private List<SelectedField> getSelectedFields(String fieldPathPrefix, DataFetchingEnvironment environment) {
    return environment.getSelectionSet()
        .getFields(fieldPathPrefix.concat("*.*"));
  }

  @Data
  @Builder
  private static class FieldConfigurationPair {
    private final SelectedField selectedField;

    private final AbstractFieldConfiguration fieldConfiguration;
  }
}
