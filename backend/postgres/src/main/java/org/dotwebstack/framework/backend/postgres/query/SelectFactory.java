package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;

import graphql.schema.SelectedField;
import java.util.Map;
import lombok.Builder;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.helpers.ExceptionHelper;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Builder
class SelectFactory {

  private final DSLContext dslContext;

  private final ObjectRequest objectRequest;

  private final ObjectFieldMapper<Map<String, Object>> fieldMapper;

  private final AliasManager aliasManager;

  public SelectQuery<Record> create() {
    var objectType = getObjectType(objectRequest);

    var fromTable = DSL.table(objectType.getTable())
        .as(aliasManager.newAlias());

    var selectQuery = dslContext.selectQuery(fromTable);

    objectRequest.getSelectedScalarFields()
        .stream()
        .map(selectedField -> processScalarField(selectedField, objectType, fromTable))
        .forEach(selectQuery::addSelect);

    return selectQuery;
  }

  private SelectFieldOrAsterisk processScalarField(SelectedField selectedField, PostgresObjectType objectType,
      Table<Record> table) {
    var columnName = objectType.getField(selectedField.getName())
        .map(PostgresObjectField::getColumn)
        .orElseThrow(
            () -> ExceptionHelper.illegalStateException("Object field '{}' not found.", selectedField.getName()));

    var column = DSL.field(DSL.name(table.getName(), columnName))
        .as(aliasManager.newAlias());

    fieldMapper.register(selectedField.getName(), new ColumnMapper(column));

    return column;
  }
}
