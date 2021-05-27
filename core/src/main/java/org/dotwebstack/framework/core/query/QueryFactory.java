package org.dotwebstack.framework.core.query;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.NUMERIC_FUNCTIONS;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateUtil.getAggregateFunctionType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateUtil.getAggregateScalarType;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateUtil.getSeparator;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateUtil.isDistinct;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.MapHelper.getNestedMap;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.datafetchers.filter.FilterCriteriaParserFactory;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.CollectionQuery;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.dotwebstack.framework.core.query.model.PagingCriteria;
import org.dotwebstack.framework.core.query.model.filter.FilterCriteria;
import org.springframework.stereotype.Component;

@Component
public class QueryFactory {

  private final FilterCriteriaParserFactory filterCriteriaParserFactory;

  public QueryFactory(FilterCriteriaParserFactory filterCriteriaParserFactory) {
    this.filterCriteriaParserFactory = filterCriteriaParserFactory;
  }

  public CollectionQuery createCollectionQuery(TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment, boolean addLimit) {

    var collectionQueryBuilder = CollectionQuery.builder()
        .objectQuery(createObjectQuery(typeConfiguration, environment))
        .filterCriterias(createFilterCriterias(typeConfiguration, environment));
    if (addLimit) {
      collectionQueryBuilder.pagingCriteria(PagingCriteria.builder()
          .page(0)
          .pageSize(10)
          .build());
    }

    return collectionQueryBuilder.build();
  }

  public ObjectQuery createObjectQuery(TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return createObjectQuery("", typeConfiguration, environment);
  }

  private ObjectQuery createObjectQuery(FieldConfigurationPair pair, DataFetchingEnvironment environment) {
    String fieldPathPrefix = pair.getSelectedField()
        .getFullyQualifiedName()
        .concat("/");
    TypeConfiguration<?> typeConfiguration = pair.getFieldConfiguration()
        .getTypeConfiguration();

    return createObjectQuery(fieldPathPrefix, typeConfiguration, environment);
  }

  public ObjectQuery createObjectQuery(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    List<FieldConfiguration> scalarFields = getScalarFields(fieldPathPrefix, typeConfiguration, environment);
    List<ObjectFieldConfiguration> objectFields = getObjectFields(fieldPathPrefix, typeConfiguration, environment);
    List<NestedObjectFieldConfiguration> nestedObjectFields =
        getNestedObjectFields(fieldPathPrefix, typeConfiguration, environment);
    List<AggregateObjectFieldConfiguration> aggregateObjectFields =
        getAggregateObjectFields(fieldPathPrefix, typeConfiguration, environment);

    List<KeyCriteria> keyCriterias = createKeyCriteria(environment);

    List<ObjectFieldConfiguration> collectionObjectFields =
        getCollectionObjectFields(fieldPathPrefix, typeConfiguration, environment);

    return ObjectQuery.builder()
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

      GraphQLInputObjectType graphQlInputObjectType = Optional.of(argument)
          .map(GraphQLArgument::getType)
          .filter(type -> type instanceof GraphQLInputObjectType)
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

  private Stream<GraphQLInputObjectField> getInputObjectFields(GraphQLInputObjectType inputObjectType) {
    return inputObjectType.getChildren()
        .stream()
        .filter(schemaElement -> schemaElement instanceof GraphQLInputObjectField)
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
    // TODO: rework after validation

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

  private void validate(AbstractFieldConfiguration aggregateFieldConfiguration, SelectedField selectedField) {

    if (NUMERIC_FUNCTIONS.contains(selectedField.getName())) {
      if (aggregateFieldConfiguration.isNumeric()) {
        return;
      } else {
        throw new IllegalArgumentException(String.format(
            "Numeric aggregation for non-numeric field %s is not supported.", aggregateFieldConfiguration.getName()));
      }
    }
    switch (selectedField.getName()) {
      case STRING_JOIN_FIELD:
        validateStringJoinField(aggregateFieldConfiguration);
        break;
      case COUNT_FIELD:
        // no additional validation needed
        break;
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported aggregation function: %s.", selectedField.getName()));
    }
  }

  private void validateStringJoinField(AbstractFieldConfiguration aggregateFieldConfiguration) {
    if (!aggregateFieldConfiguration.isText()) {
      throw new IllegalArgumentException(String.format("String aggregation for non-text field %s is not supported.",
          aggregateFieldConfiguration.getName()));
    }
  }

  private List<KeyCriteria> createKeyCriteria(DataFetchingEnvironment environment) {
    return environment.getArguments()
        .entrySet()
        .stream()
        .filter(argument -> !Objects.equals(argument.getKey(), FilterConstants.FILTER_ARGUMENT_NAME))
        .map(entry -> KeyCriteria.builder()
            .values(environment.getArguments())
            .build())
        .collect(Collectors.toList());
  }

  private List<FieldConfiguration> getScalarFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {
    return getSelectedFields(fieldPathPrefix, environment).stream()
        .map(selectedField -> typeConfiguration.getFields()
            .get(selectedField.getName()))
        .filter(AbstractFieldConfiguration::isScalarField)
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
            .objectQuery(createObjectQuery(pair, environment))
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
            .objectQuery(createObjectQuery(pair, environment))
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
