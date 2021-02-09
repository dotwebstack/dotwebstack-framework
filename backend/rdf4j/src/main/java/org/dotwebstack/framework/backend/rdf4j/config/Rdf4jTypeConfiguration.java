package org.dotwebstack.framework.backend.rdf4j.config;

import com.fasterxml.jackson.annotation.JsonTypeName;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.FieldKeyCondition;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("rdf4j")
public class Rdf4jTypeConfiguration extends AbstractTypeConfiguration<Rdf4jFieldConfiguration> {

  private String nodeShape;

  @Override
  public void init(ObjectTypeDefinition objectTypeDefinition) {
    // Calculate the column names once on init
    objectTypeDefinition.getFieldDefinitions()
        .forEach(fieldDefinition -> {
          fields.computeIfAbsent(fieldDefinition.getName(), fieldName -> new Rdf4jFieldConfiguration());
        });
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
