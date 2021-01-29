package org.dotwebstack.framework.backend.postgres.config;

import static org.jooq.impl.DSL.table;

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
import org.jooq.Record;
import org.jooq.Table;


@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("postgres")
public class PostgresTypeConfiguration extends AbstractTypeConfiguration<PostgresFieldConfiguration> {

  @NotBlank
  private String table;

  private Table<Record> sqlTable;

  @Override
  public void init(ObjectTypeDefinition objectTypeDefinition) {
    sqlTable = table(table);

    // Calculate the column names once on init
    objectTypeDefinition.getFieldDefinitions()
        .forEach(fieldDefinition -> {
          String columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldDefinition.getName());

          PostgresFieldConfiguration fieldConfiguration =
              fields.computeIfAbsent(fieldDefinition.getName(), fieldName -> new PostgresFieldConfiguration());

          fieldConfiguration.setSqlColumnName(columnName);
        });
  }

  public Table<Record> getSqlTable() {
    return sqlTable;
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
          .columnValues(columnValues)
          .build();
    }

    return super.getKeyCondition(fieldName);
  }

  @Override
  public KeyCondition getKeyCondition(DataFetchingEnvironment environment) {
    return getQueryArgumentKeyConditions(environment).map(queryArgumentsKeyCondition -> ColumnKeyCondition.builder()
        .columnValues(queryArgumentsKeyCondition.getFieldValues()
            .entrySet()
            .stream()
            .map(e -> Map.entry(getFields().get(e.getKey())
                .getSqlColumnName(), e.getValue()))
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
        .columnValues(columnValues)
        .build();
  }
}
