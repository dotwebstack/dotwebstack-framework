package org.dotwebstack.framework.backend.postgres.config;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.CaseFormat;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.dotwebstack.framework.core.helpers.TypeHelper;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("postgres")
public class PostgresTypeConfiguration extends AbstractTypeConfiguration<PostgresFieldConfiguration> {

  @NotBlank
  private String table;

  @Setter(AccessLevel.NONE)
  private Map<String, PostgresFieldConfiguration> referencedColumns = new HashMap<>();

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
          if (TypeHelper.hasListType(fieldDefinition.getType())) {
            fieldConfiguration.setList(true);
          }
          if (TypeHelper.isTextType(fieldDefinition.getType())) {
            fieldConfiguration.setText(true);
          }

        });
    initAggregateTypes(typeMapping);
    initReferencedColumns(typeMapping, objectTypeDefinition.getFieldDefinitions());
  }

  private void validateJoinTableConfig(PostgresFieldConfiguration fieldConfiguration,
      Map<String, AbstractTypeConfiguration<?>> typeMapping, FieldDefinition fieldDefinition) {
    List<JoinColumn> joinColumns = new ArrayList<>();
    Optional.ofNullable(fieldConfiguration.findInverseJoinColumns())
        .ifPresent(joinColumns::addAll);
    Optional.ofNullable(fieldConfiguration.findJoinColumns())
        .ifPresent(joinColumns::addAll);

    joinColumns.forEach(joinColumn -> {
      if (!validateReferencedFieldRequiredWithoutReferencedColumn(joinColumn, fieldDefinition)
          && !validateReferencedColumnRequiredWithoutReferencedField(joinColumn, fieldDefinition)) {
        throw invalidConfigurationException(
            "The field 'referencedField' or 'referencedColumn' must have a value in field '{}'.",
            fieldDefinition.getName());
      }
      validateTargetObjectTypeHasPostgresBackend(joinColumn, fieldConfiguration, typeMapping, fieldDefinition);
    });
  }

  private boolean validateReferencedFieldRequiredWithoutReferencedColumn(JoinColumn joinColumn,
      FieldDefinition fieldDefinition) {
    boolean result = false;
    if (StringUtils.isBlank(joinColumn.getReferencedColumn())
        && !StringUtils.isBlank(joinColumn.getReferencedField())) {
      result = true;
    }
    return result;
  }

  private boolean validateReferencedColumnRequiredWithoutReferencedField(JoinColumn joinColumn,
      FieldDefinition fieldDefinition) {
    boolean result = false;
    if (StringUtils.isBlank(joinColumn.getReferencedField())
        && !StringUtils.isBlank(joinColumn.getReferencedColumn())) {
      result = true;
    }
    return result;
  }
  
  private void validateTargetObjectTypeHasPostgresBackend(JoinColumn joinColumn,
      PostgresFieldConfiguration fieldConfiguration, Map<String, AbstractTypeConfiguration<?>> typeMapping,
      FieldDefinition fieldDefinition) {
    if (StringUtils.isNoneBlank(joinColumn.getReferencedColumn()) && !fieldConfiguration.isAggregate()) {
      String targetType = TypeHelper.getTypeName(fieldDefinition.getType());
      TypeConfiguration<?> typeConfiguration = typeMapping.get(targetType);
      if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {

        throw invalidConfigurationException("Target objectType must be an 'PostgresTypeConfiguration' but is an '{}'.",
            typeConfiguration.getClass());
      }
    }
  }

  @Override
  public KeyCondition getKeyCondition(String fieldName, Map<String, Object> source) {
    PostgresFieldConfiguration fieldConfiguration = getFields().get(fieldName);

    if (fieldConfiguration.getJoinTable() != null) {
      Map<String, Object> columnValues = fieldConfiguration.getJoinTable()
          .getJoinColumns()
          .stream()
          .collect(Collectors.toMap(JoinColumn::getName, joinColumn -> source.get(joinColumn.getField())));

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
        .collect(Collectors.toMap(JoinColumn::getName, joinColumn -> source.get(joinColumn.getField())));

    return ColumnKeyCondition.builder()
        .valueMap(columnValues)
        .build();
  }

  private void initAggregateTypes(Map<String, AbstractTypeConfiguration<?>> typeMapping) {
    fields.values()
        .stream()
        .filter(fieldConfiguration -> isNotEmpty(fieldConfiguration.getAggregationOf()))
        .forEach(fieldConfiguration -> {

          if (fieldConfiguration.getMappedBy() != null) {

            PostgresTypeConfiguration typeConfiguration =
                (PostgresTypeConfiguration) typeMapping.get(fieldConfiguration.getAggregationOf());
            PostgresFieldConfiguration mappedByFieldConfiguration = typeConfiguration.getFields()
                .get(fieldConfiguration.getMappedBy());

            fieldConfiguration.setJoinColumns(mappedByFieldConfiguration.getJoinColumns());
          } else if (fieldConfiguration.getJoinTable() == null) {
            throw invalidConfigurationException("Invalid aggregate field configuration.");
          }
        });
  }

  private void initReferencedColumns(Map<String, AbstractTypeConfiguration<?>> typeMapping,
      List<FieldDefinition> fieldDefinitions) {
    referencedColumns = fields.entrySet()
        .stream()
        .filter(entry -> entry.getValue()
            .getJoinTable() != null)
        .flatMap(entry -> {
          validateJoinTableConfig(entry.getValue(), typeMapping, getFieldDefinition(entry.getKey(), fieldDefinitions));
          return entry.getValue()
              .getJoinTable()
              .getJoinColumns()
              .stream();
        })
        .filter(joinColumn -> joinColumn.getReferencedColumn() != null)
        .map(joinColumn -> createPostgresFieldConfiguration(joinColumn.getReferencedColumn()))
        .collect(Collectors.toMap(PostgresFieldConfiguration::getColumn, fieldConfig -> fieldConfig, (a, b) -> a));

    fields.putAll(referencedColumns);
  }

  private FieldDefinition getFieldDefinition(String fieldName, List<FieldDefinition> fieldDefinitions) {
    return fieldDefinitions.stream()
        .filter(fieldDefinition -> fieldDefinition.getName()
            .equals(fieldName))
        .findFirst()
        .orElseThrow(() -> illegalStateException("No fielddefinition available for field {}", fieldName));
  }

  private PostgresFieldConfiguration createPostgresFieldConfiguration(String column) {
    PostgresFieldConfiguration postgresFieldConfiguration = new PostgresFieldConfiguration();
    postgresFieldConfiguration.setColumn(column);
    return postgresFieldConfiguration;
  }

}
