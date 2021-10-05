package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Setter
@Accessors(fluent = true)
class SelectBuilder {

  private DSLContext dslContext;

  private ObjectRequest objectRequest;

  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  private AliasManager aliasManager;

  private SelectBuilder() {}

  public static SelectBuilder newSelect() {
    return new SelectBuilder();
  }

  public SelectQuery<Record> build() {
    var objectType = getObjectType(objectRequest);

    var fromTable = DSL.table(objectType.getTable())
        .as(aliasManager.newAlias());

    var selectQuery = dslContext.selectQuery(fromTable);

    objectRequest.getKeyCriteria()
        .stream()
        .map(keyCriteria -> createKeyConditions(keyCriteria, objectType, fromTable))
        .forEach(selectQuery::addConditions);

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
          selectQuery.addSelect(DSL.field(String.format("\"%s\".*", lateralTable.getName())));
          selectQuery.addJoin(lateralTable, JoinType.LEFT_OUTER_JOIN);
        });

    return selectQuery;
  }

  private List<Condition> createKeyConditions(KeyCriteria keyCriteria, PostgresObjectType objectType,
      Table<Record> table) {
    return keyCriteria.getValues()
        .entrySet()
        .stream()
        .map(entry -> objectType.getField(entry.getKey())
            .map(PostgresObjectField::getColumn)
            .map(column -> DSL.field(DSL.name(table.getName(), column))
                .equal(entry.getValue()))
            .orElseThrow())
        .collect(Collectors.toList());
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

    var nestedSelect = SelectBuilder.newSelect()
        .dslContext(dslContext)
        .objectRequest(nestedObjectRequest)
        .fieldMapper(nestedObjectMapper)
        .aliasManager(aliasManager)
        .build();

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
