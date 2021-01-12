package org.dotwebstack.framework.backend.postgres.config;

import static org.jooq.impl.DSL.table;

import com.fasterxml.jackson.annotation.JsonTypeName;
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

  private Table<Record> tableQueryPart;

  @Override
  public void init(ObjectTypeDefinition objectTypeDefinition) {
    tableQueryPart = table(table);
  }

  public Table<Record> tableAlias(int alias) {
    return tableQueryPart.as(String.format("%s%s", "t", alias));
  }
}
