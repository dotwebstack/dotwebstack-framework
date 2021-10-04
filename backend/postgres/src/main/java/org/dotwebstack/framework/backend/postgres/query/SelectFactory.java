package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.SelectedField;
import java.util.Map;
import lombok.Builder;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.DSLContext;
import org.jooq.JoinType;
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

    objectRequest.getSelectedObjectFields()
        .entrySet()
        .stream()
        .map(entry -> createNestedSelect(entry.getKey(), entry.getValue(), fromTable))
        .forEach(nestedSelect -> {
          var lateralTable = DSL.lateral(nestedSelect.asTable(aliasManager.newAlias()));
          selectQuery.addSelect(DSL.field(lateralTable.getName()
              .concat(".*")));
          selectQuery.addJoin(lateralTable, JoinType.LEFT_OUTER_JOIN);
        });

    return selectQuery;
  }

  private SelectFieldOrAsterisk processScalarField(SelectedField selectedField, PostgresObjectType objectType,
      Table<Record> table) {
    var columnName = objectType.getField(selectedField.getName())
        .map(PostgresObjectField::getColumn)
        .orElseThrow(() -> illegalStateException("Object field '{}' not found.", selectedField.getName()));

    var column = DSL.field(DSL.name(table.getName(), columnName))
        .as(aliasManager.newAlias());

    fieldMapper.register(selectedField.getName(), new ColumnMapper(column));

    return column;
  }

  private SelectQuery<Record> createNestedSelect(SelectedField selectedField, ObjectRequest nestedObjectRequest,
      Table<Record> table) {
    var objectField = getObjectField(objectRequest, selectedField.getName());

    var nestedObjectAlias = aliasManager.newAlias();
    var nestedObjectMapper = new ObjectMapper(nestedObjectAlias);

    fieldMapper.register(selectedField.getName(), nestedObjectMapper);

    var nestedSelectFactory = SelectFactory.builder()
        .dslContext(dslContext)
        .objectRequest(nestedObjectRequest)
        .fieldMapper(nestedObjectMapper)
        .aliasManager(aliasManager)
        .build();

    var nestedSelect = nestedSelectFactory.create();
    nestedSelect.addSelect(DSL.field("1")
        .as(nestedObjectAlias));

    objectField.getJoinColumns()
        .forEach(joinColumn -> {
          var field = DSL.field(DSL.name(table.getName(), joinColumn.getName()));
          var referencedField = DSL.field(joinColumn.getReferencedField());
          nestedSelect.addConditions(referencedField.equal(field));
        });

    return nestedSelect;
  }
}
