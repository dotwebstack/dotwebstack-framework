package org.dotwebstack.framework.backend.postgres.config;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.CaseFormat;
import graphql.language.ObjectTypeDefinition;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dotwebstack.framework.core.config.AbstractTypeConfiguration;
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
          String columnName = CaseFormat.LOWER_CAMEL.to(
              CaseFormat.LOWER_UNDERSCORE, fieldDefinition.getName());

          PostgresFieldConfiguration fieldConfiguration = fields.computeIfAbsent(fieldDefinition.getName(),
              fieldName -> new PostgresFieldConfiguration());

          fieldConfiguration.setSqlField(field(columnName));
        });
  }

  public Table<Record> getSqlTable() {
    return sqlTable;
  }
}
