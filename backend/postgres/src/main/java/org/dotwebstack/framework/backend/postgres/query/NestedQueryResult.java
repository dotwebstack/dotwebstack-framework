package org.dotwebstack.framework.backend.postgres.query;

import graphql.schema.SelectedField;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;

@Builder
@Getter
class NestedQueryResult {

  private final SelectedField selectedField;

  private final Field<Object> selectedColumn;

  private final PostgresTypeConfiguration typeConfiguration;

  private final Table<Record> table;

  private final Map<String, Object> columnAliasMap;
}
