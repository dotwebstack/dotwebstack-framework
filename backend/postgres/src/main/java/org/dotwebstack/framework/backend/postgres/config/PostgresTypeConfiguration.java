package org.dotwebstack.framework.backend.postgres.config;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.CaseFormat;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.KeyCondition;
import org.dotwebstack.framework.core.datafetchers.MappedByKeyCondition;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.dotwebstack.framework.core.query.model.filter.FieldPathHelper;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("postgres")
public class PostgresTypeConfiguration extends AbstractTypeConfiguration<PostgresFieldConfiguration> {

  private String table;

  @Setter(AccessLevel.NONE)
  private Map<String, PostgresFieldConfiguration> referencedColumns = new HashMap<>();

  @Override
  public void init(DotWebStackConfiguration dotWebStackConfiguration) {

    getFields().values()
        .forEach(fieldConfiguration -> {
          if (fieldConfiguration.isScalar()) {
            fieldConfiguration.setTypeConfiguration(this);

            if (fieldConfiguration.getColumn() == null) {
              String columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldConfiguration.getName());
              fieldConfiguration.setColumn(columnName);
            }
          }

          if (TypeHelper.isNumericType(fieldConfiguration.getType())) {
            fieldConfiguration.setNumeric(true);
          }

          if (TypeHelper.isTextType(fieldConfiguration.getType())
              || isEnum(fieldConfiguration.getType(), dotWebStackConfiguration)) {
            fieldConfiguration.setText(true);
          }
        });

    initAggregateTypes(dotWebStackConfiguration.getObjectTypes());
    initNestedObjectTypes(dotWebStackConfiguration.getObjectTypes());
    initSortCriterias(dotWebStackConfiguration);
    initReferencedColumns(dotWebStackConfiguration.getObjectTypes());
    initObjectTypes(dotWebStackConfiguration.getObjectTypes());
    initKeyFields();
  }

  private void initSortCriterias(DotWebStackConfiguration dotWebStackConfiguration) {
    sortCriterias = sortableBy.entrySet()
        .stream()
        .map(entry -> Map.entry(entry.getKey(), entry.getValue()
            .stream()
            .map(sortableByConfiguration -> createSortCriteria(dotWebStackConfiguration, sortableByConfiguration))
            .collect(Collectors.toList())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private SortCriteria createSortCriteria(DotWebStackConfiguration dotWebStackConfiguration,
      SortableByConfiguration sortableByConfiguration) {
    return SortCriteria.builder()
        .fieldPath(FieldPathHelper.createFieldPath(dotWebStackConfiguration, this, sortableByConfiguration.getField()))
        .direction(sortableByConfiguration.getDirection())
        .build();
  }

  private void validateJoinTableConfig(PostgresFieldConfiguration fieldConfiguration,
      Map<String, AbstractTypeConfiguration<?>> objectTypes) {
    List<JoinColumn> joinColumns = new ArrayList<>();
    Optional.ofNullable(fieldConfiguration.findInverseJoinColumns())
        .ifPresent(joinColumns::addAll);
    Optional.ofNullable(fieldConfiguration.findJoinColumns())
        .ifPresent(joinColumns::addAll);

    joinColumns.forEach(joinColumn -> {
      if (isNotValidJoinColumn(joinColumn)) {
        throw invalidConfigurationException(
            "The field 'referencedField' or 'referencedColumn' must have a value in field '{}'.",
            fieldConfiguration.getName());
      }
      validateTargetObjectTypeHasPostgresBackend(joinColumn, fieldConfiguration, objectTypes);
    });
  }

  private boolean isNotValidJoinColumn(JoinColumn joinColumn) {
    return !validateReferencedFieldRequiredWithoutReferencedColumn(joinColumn)
        && !validateReferencedColumnRequiredWithoutReferencedField(joinColumn);
  }

  private boolean validateReferencedFieldRequiredWithoutReferencedColumn(JoinColumn joinColumn) {
    return StringUtils.isBlank(joinColumn.getReferencedColumn())
        && !StringUtils.isBlank(joinColumn.getReferencedField());
  }

  private boolean validateReferencedColumnRequiredWithoutReferencedField(JoinColumn joinColumn) {
    return StringUtils.isBlank(joinColumn.getReferencedField())
        && !StringUtils.isBlank(joinColumn.getReferencedColumn());
  }

  private void validateTargetObjectTypeHasPostgresBackend(JoinColumn joinColumn,
      PostgresFieldConfiguration fieldConfiguration, Map<String, AbstractTypeConfiguration<?>> objectTypes) {
    if (StringUtils.isNoneBlank(joinColumn.getReferencedColumn()) && !fieldConfiguration.isAggregate()) {
      TypeConfiguration<?> typeConfiguration = objectTypes.get(fieldConfiguration.getType());
      if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {
        throw invalidConfigurationException("Target objectType must be an 'PostgresTypeConfiguration' but is an '{}'.",
            Optional.ofNullable(typeConfiguration)
                .map(TypeConfiguration::getClass)
                .orElse(null));
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

  private void initObjectTypes(Map<String, AbstractTypeConfiguration<?>> objectTypes) {
    fields.values()
        .stream()
        .filter(PostgresFieldConfiguration::isObjectField)
        .forEach(fieldConfiguration -> {

          PostgresTypeConfiguration typeConfiguration =
              (PostgresTypeConfiguration) objectTypes.get(fieldConfiguration.getType());
          fieldConfiguration.setTypeConfiguration(typeConfiguration);
        });
  }

  private void initAggregateTypes(Map<String, AbstractTypeConfiguration<?>> objectTypes) {
    fields.values()
        .stream()
        .filter(fieldConfiguration -> isNotEmpty(fieldConfiguration.getAggregationOf()))
        .forEach(fieldConfiguration -> {

          PostgresTypeConfiguration typeConfiguration =
              (PostgresTypeConfiguration) objectTypes.get(fieldConfiguration.getAggregationOf());
          fieldConfiguration.setTypeConfiguration(typeConfiguration);

          if (fieldConfiguration.getMappedBy() != null) {

            PostgresFieldConfiguration mappedByFieldConfiguration = typeConfiguration.getFields()
                .get(fieldConfiguration.getMappedBy());

            fieldConfiguration.setJoinColumns(mappedByFieldConfiguration.getJoinColumns());
          } else if (fieldConfiguration.getJoinTable() == null) {
            throw invalidConfigurationException("Invalid aggregate field configuration.");
          }
        });
  }

  private void initNestedObjectTypes(Map<String, AbstractTypeConfiguration<?>> objectTypes) {
    fields.values()
        .forEach(fieldConfiguration -> {

          PostgresTypeConfiguration typeConfiguration =
              (PostgresTypeConfiguration) objectTypes.get(fieldConfiguration.getType());

          if (Objects.nonNull(typeConfiguration) && Objects.isNull(typeConfiguration.getTable())) {
            fieldConfiguration.setNested(true);
            fieldConfiguration.setTypeConfiguration(typeConfiguration);
          }

        });
  }

  private void initReferencedColumns(Map<String, AbstractTypeConfiguration<?>> objectTypes) {
    referencedColumns = fields.values()
        .stream()
        .filter(fieldConfiguration -> fieldConfiguration.getJoinTable() != null)
        .flatMap(fieldConfiguration -> {
          validateJoinTableConfig(fieldConfiguration, objectTypes);
          return fieldConfiguration.getJoinTable()
              .getJoinColumns()
              .stream();
        })
        .filter(joinColumn -> joinColumn.getReferencedColumn() != null)
        .map(joinColumn -> createPostgresFieldConfiguration(joinColumn.getReferencedColumn()))
        .collect(Collectors.toMap(PostgresFieldConfiguration::getColumn, fieldConfig -> fieldConfig, (a, b) -> a));

    fields.putAll(referencedColumns);
  }

  private void initKeyFields() {
    fields.values()
        .forEach(fieldConfiguration -> {

          if ((getKeys().stream()
              .anyMatch(
                  keyConfiguration -> Objects.equals(keyConfiguration.getField(), fieldConfiguration.getName())))) {
            fieldConfiguration.setKeyField(true);
          }
        });
  }

  private PostgresFieldConfiguration createPostgresFieldConfiguration(String column) {
    var postgresFieldConfiguration = new PostgresFieldConfiguration();
    postgresFieldConfiguration.setColumn(column);
    postgresFieldConfiguration.setName(column);
    return postgresFieldConfiguration;
  }

  private boolean isEnum(String type, DotWebStackConfiguration dotWebStackConfiguration) {
    return dotWebStackConfiguration.getEnumerations()
        .containsKey(type);
  }
}
