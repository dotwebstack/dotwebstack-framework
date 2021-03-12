package org.dotwebstack.framework.backend.postgres.config;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.CaseFormat;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.dotwebstack.framework.core.helpers.TypeHelper;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("postgres")
public class PostgresTypeConfiguration extends AbstractTypeConfiguration<PostgresFieldConfiguration> {

  @NotBlank
  private String table;

  @Override
  public void init(Map<String, AbstractTypeConfiguration<?>> typeMapping, ObjectTypeDefinition objectTypeDefinition) {
    // Calculate the column names once on init
    objectTypeDefinition.getFieldDefinitions()
        .forEach(fieldDefinition -> {
          PostgresFieldConfiguration fieldConfiguration =
              fields.computeIfAbsent(fieldDefinition.getName(), fieldName -> new PostgresFieldConfiguration());

          if (fieldConfiguration.getColumn() == null && fieldConfiguration.isScalar()) {
            String columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldDefinition.getName());
            fieldConfiguration.setColumn(columnName);
          }

          if (TypeHelper.isNumericType(fieldDefinition.getType())) {
            fieldConfiguration.setNumeric(true);
          }
        });

    initAggregateTypes(typeMapping, objectTypeDefinition);
  }

  @Override
  public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
    PostgresFieldConfiguration fieldConfiguration = getFields().get(fieldName);

    if (fieldConfiguration.getJoinTable() != null) {
      Map<String, Object> columnValues = fieldConfiguration.getJoinTable()
          .getJoinColumns()
          .stream()
          .collect(Collectors.toMap(JoinColumn::getName, joinColumn -> source.get(joinColumn.getReferencedField())));

      return ColumnKeyCondition.builder()
          .valueMap(columnValues)
          .joinTable(fieldConfiguration.getJoinTable())
          .build();
    }

    return super.getKeyCondition(fieldName);
  }

  @Override
  public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
    return getQueryArgumentKeyConditions(environment, true)
        .map(queryArgumentsKeyCondition -> ColumnKeyCondition.builder()
            .valueMap(queryArgumentsKeyCondition.getFieldValues()
                .entrySet()
                .stream()
                .map(e -> Map.entry(getFields().get(e.getKey())
                    .getColumn(), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            .build())
        .orElse(null);
  }

  @Override
  public KeyCondition invertKeyCondition(MappedByKeyCondition mappedByKeyCondition, Map<String, Object> source) {
    PostgresFieldConfiguration fieldConfiguration = getFields().get(mappedByKeyCondition.getFieldName());

    Map<String, Object> columnValues = fieldConfiguration.getJoinColumns()
        .stream()
        .collect(Collectors.toMap(JoinColumn::getName, joinColumn -> source.get(joinColumn.getReferencedField())));

    return ColumnKeyCondition.builder()
        .valueMap(columnValues)
        .build();
  }

  private void initAggregateTypes(Map<String, AbstractTypeConfiguration<?>> typeMapping,
      ObjectTypeDefinition objectTypeDefinition) {

    fields.values()
        .stream()
        .filter(fieldConfiguration -> isNotEmpty(fieldConfiguration.getAggregationOf()))
        .forEach(fieldConfiguration -> {
          PostgresFieldConfiguration ref = fields.get(fieldConfiguration.getAggregationOf());

          if (ref.getMappedBy() != null) {
            objectTypeDefinition.getFieldDefinitions()
                .stream()
                .filter(fieldDefinition -> fieldDefinition.getName()
                    .equals(fieldConfiguration.getAggregationOf()))
                .findFirst()
                .ifPresent(fieldDefinition -> {
                  String typeName = TypeHelper.getTypeName(fieldDefinition.getType());

                  PostgresTypeConfiguration typeConfiguration = (PostgresTypeConfiguration) typeMapping.get(typeName);

                  PostgresFieldConfiguration mappedByFieldConfiguration = typeConfiguration.getFields()
                      .get(ref.getMappedBy());

                  fieldConfiguration.setJoinColumns(mappedByFieldConfiguration.getJoinColumns());
                });
          } else if (ref.getJoinTable() != null) {
            fieldConfiguration.setJoinTable(ref.getJoinTable());
            fieldConfiguration.setJoinColumns(ref.getJoinTable()
                .getJoinColumns());
          } else {
            throw invalidConfigurationException("Invalid aggregate field configuration.");
          }
        });
  }
}
