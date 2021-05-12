package org.dotwebstack.framework.backend.rdf4j.config;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("rdf4j")
public class Rdf4jTypeConfiguration extends AbstractTypeConfiguration<Rdf4jFieldConfiguration> {

  private String nodeShape;

  @Override
  public void init(DotWebStackConfiguration dotWebStackConfiguration, ObjectTypeDefinition objectTypeDefinition) {
    // Calculate the column names once on init
    objectTypeDefinition.getFieldDefinitions()
        .forEach(fieldDefinition -> fields.computeIfAbsent(fieldDefinition.getName(),
            fieldName -> new Rdf4jFieldConfiguration()));

    fields.entrySet()
        .stream()
        .filter(entry -> Objects.nonNull(entry.getValue()))
        .filter(entry -> isNotEmpty(entry.getValue()
            .getAggregationOf()))
        .findFirst()
        .ifPresent(entry -> {
          throw invalidConfigurationException(
              "Usage of 'aggregationOf' by field '{}.{}' is not supported with an RDF4J backend",
              objectTypeDefinition.getName(), entry.getKey());
        });

    postFieldProcessing();
  }

  @Override
  public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
    return getQueryArgumentKeyConditions(environment, true).orElse(null);
  }

  @Override
  public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
    return super.getKeyCondition(fieldName);
  }

  @Override
  public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
    Rdf4jFieldConfiguration fieldConfiguration = getFields().get(mappedByKeyCondition.getFieldName());

    Map<String, Object> columnValues = fieldConfiguration.getJoinProperties()
        .stream()
        .collect(Collectors.toMap(JoinProperty::getName, joinColumn -> source.get(joinColumn.getReferencedField())));

    return FieldKeyCondition.builder()
        .fieldValues(columnValues)
        .build();
  }
}
