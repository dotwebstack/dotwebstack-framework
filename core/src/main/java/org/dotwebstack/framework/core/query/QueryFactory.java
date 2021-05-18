package org.dotwebstack.framework.core.query;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Builder;
import lombok.Data;
import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.dotwebstack.framework.core.config.FieldConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.query.model.AggregateFieldConfiguration;
import org.dotwebstack.framework.core.query.model.AggregateFunctionType;
import org.dotwebstack.framework.core.query.model.AggregateObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.NestedObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectFieldConfiguration;
import org.dotwebstack.framework.core.query.model.ObjectQuery;
import org.dotwebstack.framework.core.query.model.ScalarType;
import org.springframework.stereotype.Component;

import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.COUNT_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.DISTINCT_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FIELD_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.FLOAT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_AVG_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MAX_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_MIN_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.INT_SUM_FIELD;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.SEPARATOR_ARGUMENT;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants.STRING_JOIN_FIELD;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

@Component
public class QueryFactory {

  private static final String DEFAULT_SEPARATOR = ",";

  public ObjectQuery createObjectQuery(TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {

    return createObjectQuery("", typeConfiguration, environment);
  }

  public ObjectQuery createObjectQuery(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    List<FieldConfiguration> scalarFields = getScalarFields(fieldPathPrefix, typeConfiguration, environment);
    List<ObjectFieldConfiguration> objectFields = getObjectFields(fieldPathPrefix, typeConfiguration, environment);
    List<NestedObjectFieldConfiguration> nestedObjectFields =
        getNestedObjectFields(fieldPathPrefix, typeConfiguration, environment);
    List<AggregateObjectFieldConfiguration> aggregateObjectFields = getAggregateObjectFields(fieldPathPrefix, typeConfiguration, environment);

    return ObjectQuery.builder()
        .typeConfiguration(typeConfiguration)
        .scalarFields(scalarFields)
        .objectFields(objectFields)
        .nestedObjectFields(nestedObjectFields)
        .aggregateObjectFields(aggregateObjectFields)
        .build();
  }

  private List<AggregateObjectFieldConfiguration> getAggregateObjectFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration, DataFetchingEnvironment environment) {
    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment)
        .filter(pair -> pair.getFieldConfiguration()
            .isAggregateField())
        .map(pair -> AggregateObjectFieldConfiguration.builder()
            .field(pair.getFieldConfiguration())
            .aggregateFields(getAggregateFields(pair, environment))
            .build())
        .collect(Collectors.toList());
  }

  private List<AggregateFieldConfiguration> getAggregateFields(FieldConfigurationPair fieldConfigurationPair, DataFetchingEnvironment environment) {
    String fieldPathPrefix = fieldConfigurationPair.getSelectedField()
        .getFullyQualifiedName()
        .concat("/");
    TypeConfiguration<?> aggregateTypeConfiguration = fieldConfigurationPair.getFieldConfiguration()
        .getTypeConfiguration();
    return getAggregateFieldConfigurationPairs(fieldPathPrefix, aggregateTypeConfiguration, environment)
        .map(pair -> createAggregateFieldConfiguration(pair))
        .collect(Collectors.toList());
  }

  private AggregateFieldConfiguration createAggregateFieldConfiguration(FieldConfigurationPair fieldConfigurationPair) {
    SelectedField aggregateField = fieldConfigurationPair.getSelectedField();
    AggregateFunctionType aggregateFunctionType = getAggregateFunctionType(aggregateField);
    ScalarType type = getAggregateScalarType(aggregateField);
    boolean distinct = isDistinct(aggregateField);
    // TODO: rework after validation
    String separator = null;
    if(aggregateFunctionType == AggregateFunctionType.JOIN){
      separator = getSeparator(aggregateField);
    }

    return AggregateFieldConfiguration.builder()
        .field(fieldConfigurationPair.getFieldConfiguration())
        .aggregateFunctionType(aggregateFunctionType)
        .type(type)
        .distinct(distinct)
        .separator(separator)
        .build();
  }

// TODO: AggregateUtil
private ScalarType getAggregateScalarType(SelectedField selectedField){
  String aggregateFunction = selectedField.getName();
  switch(aggregateFunction) {
    case INT_MIN_FIELD:
    case INT_MAX_FIELD:
    case INT_AVG_FIELD:
    case INT_SUM_FIELD:
    case COUNT_FIELD:
      return ScalarType.INT;
    case STRING_JOIN_FIELD:
      return ScalarType.STRING;
    case FLOAT_MIN_FIELD:
    case FLOAT_SUM_FIELD:
    case FLOAT_MAX_FIELD:
    case FLOAT_AVG_FIELD:
      return ScalarType.FLOAT;
    default:
      throw illegalArgumentException("Aggregate function {} is not supported", aggregateFunction);
  }
}
  private AggregateFunctionType getAggregateFunctionType(SelectedField selectedField){
    String aggregateFunction = selectedField.getName();
    switch(aggregateFunction) {
      case COUNT_FIELD:
        return AggregateFunctionType.COUNT;
      case STRING_JOIN_FIELD:
        return AggregateFunctionType.JOIN;
      case FLOAT_SUM_FIELD:
      case INT_SUM_FIELD:
        return AggregateFunctionType.SUM;
      case FLOAT_MIN_FIELD:
      case INT_MIN_FIELD:
        return AggregateFunctionType.MIN;
      case FLOAT_MAX_FIELD:
      case INT_MAX_FIELD:
        return AggregateFunctionType.MAX;
      case INT_AVG_FIELD:
      case FLOAT_AVG_FIELD:
        return AggregateFunctionType.AVG;
      default:
        throw illegalArgumentException("Aggregate function {} is not supported", aggregateFunction);
    }
  }

  private boolean isDistinct(SelectedField selectedField) {
    return Optional.ofNullable((Boolean) selectedField.getArguments()
        .get(DISTINCT_ARGUMENT))
        .orElse(Boolean.FALSE);
  }

  private String getSeparator(SelectedField selectedField) {
    return Optional.ofNullable((String) selectedField.getArguments()
        .get(SEPARATOR_ARGUMENT))
        .orElse(DEFAULT_SEPARATOR);
  }


  private ObjectQuery createObjectQuery(FieldConfigurationPair pair, DataFetchingEnvironment environment) {
    String fieldPathPrefix = pair.getSelectedField()
        .getFullyQualifiedName()
        .concat("/");
    TypeConfiguration<?> typeConfiguration = pair.getFieldConfiguration()
        .getTypeConfiguration();

    return createObjectQuery(fieldPathPrefix, typeConfiguration, environment);
  }

  private List<FieldConfiguration> getScalarFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {
    return getSelectedFields(fieldPathPrefix, environment).stream()
        .map(selectedField -> typeConfiguration.getFields()
            .get(selectedField.getName()))
        .filter(AbstractFieldConfiguration::isScalarField)
        .collect(Collectors.toList());
  }

  private List<ObjectFieldConfiguration> getObjectFields(String fieldPathPrefix, TypeConfiguration<?> typeConfiguration,
      DataFetchingEnvironment environment) {

    return getFieldConfigurationPairs(fieldPathPrefix, typeConfiguration, environment)
        .filter(pair -> pair.getFieldConfiguration()
            .isObjectField())
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
                .get((String)selectedField.getArguments().get(FIELD_ARGUMENT)))
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
