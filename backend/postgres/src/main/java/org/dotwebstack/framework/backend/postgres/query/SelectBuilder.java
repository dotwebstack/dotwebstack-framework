package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
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
    // TODO null checks on class properties
    var objectType = getObjectType(objectRequest);

    var fromTable = DSL.table(objectType.getTable())
        .as(aliasManager.newAlias());

    var selectQuery = dslContext.selectQuery(fromTable);

    createJoinConditions(fromTable).forEach(selectQuery::addConditions);

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
        .flatMap(entry -> createNestedSelect(entry.getKey(), entry.getValue(), fromTable))
        .forEach(nestedSelect -> {
          var lateralTable = DSL.lateral(nestedSelect.asTable(aliasManager.newAlias()));
          selectQuery.addSelect(DSL.field(String.format("\"%s\".*", lateralTable.getName())));
          selectQuery.addJoin(lateralTable, JoinType.LEFT_OUTER_JOIN);
        });

    objectRequest.getSelectedObjectListFields()
        .entrySet()
        .stream()
        .flatMap(entry -> processObjectListFields(entry.getKey(), entry.getValue(), fromTable))
        .forEach(selectQuery::addSelect);

    return selectQuery;
  }

  private Stream<Condition> createJoinConditions(Table<Record> table) {
    var source = objectRequest.getSource();

    if (source == null) {
      return Stream.empty();
    }

    var parentField = objectRequest.getParentField();
    var joinCondition = (JoinCondition) source.get(JOIN_KEY_PREFIX.concat(parentField.getName()));

    return joinCondition.getFields()
        .entrySet()
        .stream()
        .map(entry -> DSL.field(DSL.name(table.getName(), entry.getKey()))
            .equal(entry.getValue()));
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
    var objectField = objectType.getField(selectedField.getName())
        .orElseThrow(() -> illegalStateException("Object field '{}' not found.", selectedField.getName()));

    var columnMapper = createColumnMapper(objectField, table);

    fieldMapper.register(selectedField.getName(), columnMapper);

    return columnMapper.getColumn();
  }

  private ColumnMapper createColumnMapper(PostgresObjectField objectField, Table<Record> table) {
    var column = DSL.field(DSL.name(table.getName(), objectField.getColumn()))
        .as(aliasManager.newAlias());

    return new ColumnMapper(column);
  }

  private Stream<SelectQuery<Record>> createNestedSelect(SelectedField selectedField, ObjectRequest nestedObjectRequest,
      Table<Record> table) {
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

    getObjectField(objectRequest, selectedField.getName()).getJoinColumns()
        .forEach(joinColumn -> {
          var field = DSL.field(DSL.name(table.getName(), joinColumn.getName()));
          var referencedField = DSL.field(joinColumn.getReferencedField());
          nestedSelect.addConditions(referencedField.equal(field));
        });

    return Stream.of(nestedSelect);
  }

  private Stream<SelectFieldOrAsterisk> processObjectListFields(SelectedField selectedField,
      CollectionRequest collectionRequest, Table<Record> table) {
    var objectField = getObjectField(objectRequest, selectedField.getName());

    if (objectField.getMappedBy() != null) {
      var nestedObjectField = getObjectField(collectionRequest.getObjectRequest(), objectField.getMappedBy());

      // Provide join info for child data fetcher
      fieldMapper.register(JOIN_KEY_PREFIX.concat(selectedField.getName()),
          row -> new JoinCondition(nestedObjectField.getJoinColumns()
              .stream()
              .collect(Collectors.toMap(JoinColumn::getName,
                  joinColumn -> fieldMapper.getFieldMapper(joinColumn.getReferencedField())
                      .apply(row)))));

      // Make sure join columns are selected
      return nestedObjectField.getJoinColumns()
          .stream()
          .map(joinColumn -> {
            var joinField = getObjectField(objectRequest, joinColumn.getReferencedField());
            var columnMapper = createColumnMapper(joinField, table);

            fieldMapper.register(joinField.getName(), columnMapper);

            return columnMapper.getColumn();
          });
    }

    return Stream.of();
  }
}
