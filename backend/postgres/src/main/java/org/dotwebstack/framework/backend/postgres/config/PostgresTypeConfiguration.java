package org.dotwebstack.framework.backend.postgres.config;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
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
import java.util.Objects;
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

          validateJoinColumnConfiguration(typeMapping, fieldConfiguration, fieldDefinition);
        });
    initAggregateTypes(typeMapping);
    initReferencedColumns();
  }

  private void validateJoinColumnConfiguration(Map<String, AbstractTypeConfiguration<?>> typeMapping,
      PostgresFieldConfiguration fieldConfiguration, FieldDefinition fieldDefinition) {
    List<JoinColumn> joinColumns = new ArrayList<>();
    Optional.ofNullable(fieldConfiguration.findInverseJoinColumns())
        .ifPresent(joinColumns::addAll);
    Optional.ofNullable(fieldConfiguration.findJoinColumns())
        .ifPresent(joinColumns::addAll);

    if (joinColumns.size() != 0) {
      joinColumns.forEach(joinColumn -> {

        if (Objects.isNull(joinColumn.getReferencedField()) && Objects.isNull(joinColumn.getReferencedColumn())) {
          throw invalidConfigurationException(
              "One of 'referencedField' or 'referencedColumn' must have a valid value in field {}", fieldConfiguration);
        }

        if (StringUtils.isNoneBlank(joinColumn.getReferencedField(), joinColumn.getReferencedColumn())) {
          throw invalidConfigurationException(
              "Only one of 'referencedField' or 'referencedColumn' can have a valid value in field {}",
              fieldConfiguration);
        }

        if (StringUtils.isNoneBlank(joinColumn.getReferencedColumn())) {
          String targetType = TypeHelper.getTypeName(fieldDefinition.getType());
          TypeConfiguration<?> typeConfiguration = typeMapping.get(targetType);
          // TODO: arjenhup fix this
          if (typeConfiguration != null) {
            if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {

              throw invalidConfigurationException("Target objectType must be 'postgres' but is {} ",
                  typeConfiguration.getClass());
            }
          }

        }

      });
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

  private void initReferencedColumns() {
    referencedColumns = fields.values()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getJoinTable() != null)
        .flatMap(config -> config.getJoinTable()
            .getJoinColumns()
            .stream())
        .filter(joinColumn -> joinColumn.getReferencedColumn() != null)
        .map(joinColumn -> createPostgresFieldConfiguration(joinColumn.getReferencedColumn()))
        .collect(Collectors.toMap(fieldConfig -> fieldConfig.getColumn(), fieldConfig -> fieldConfig, (a, b) -> a));
    fields.putAll(referencedColumns);
  }

  private PostgresFieldConfiguration createPostgresFieldConfiguration(String column) {
    PostgresFieldConfiguration postgresFieldConfiguration = new PostgresFieldConfiguration();
    postgresFieldConfiguration.setColumn(column);
    return postgresFieldConfiguration;
  }

}
