package org.dotwebstack.framework.backend.postgres.config;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.CaseFormat;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.backend.postgres.ColumnKeyCondition;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.SortableByConfiguration;
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

  private void initKeyFields() {
    fields.values()
        .forEach(fieldConfiguration -> {

          if ((getKeys().stream()
              .anyMatch(keyField -> Objects.equals(keyField, fieldConfiguration.getName())))) {
            fieldConfiguration.setKeyField(true);
          }
        });
  }

  private boolean isEnum(String type, DotWebStackConfiguration dotWebStackConfiguration) {
    return dotWebStackConfiguration.getEnumerations()
        .containsKey(type);
  }

  @Override
  public List<PostgresFieldConfiguration> getReferencedFields(String fieldName) {
    PostgresFieldConfiguration fieldConfiguration = getFields().get(fieldName);

    if (fieldConfiguration.getJoinTable() != null) {
      return fieldConfiguration.getJoinTable()
          .getJoinColumns()
          .stream()
          .map(JoinColumn::getReferencedField)
          .flatMap(referencedFieldName -> getField(referencedFieldName).stream())
          .collect(Collectors.toList());
    }

    return List.of();
  }
}
