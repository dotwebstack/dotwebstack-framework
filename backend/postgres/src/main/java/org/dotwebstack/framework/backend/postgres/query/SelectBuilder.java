package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectField;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.getObjectType;
import static org.dotwebstack.framework.backend.postgres.query.TableHelper.findTable;
import static org.dotwebstack.framework.core.backend.BackendConstants.JOIN_KEY_PREFIX;
import static org.dotwebstack.framework.core.backend.BackendConstants.PAGING_KEY_PREFIX;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.FIRST_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.datafetchers.paging.PagingConstants.OFFSET_ARGUMENT_NAME;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.filter.BackendFilterCriteria;
import org.dotwebstack.framework.core.backend.filter.ObjectFieldPath;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.backend.query.ObjectFieldMapper;
import org.dotwebstack.framework.core.datafetchers.filter.FilterConstants;
import org.dotwebstack.framework.core.query.model.CollectionRequest;
import org.dotwebstack.framework.core.query.model.KeyCriteria;
import org.dotwebstack.framework.core.query.model.ObjectRequest;
import org.dotwebstack.framework.core.query.model.SortCriteria;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectFieldOrAsterisk;
import org.jooq.SelectQuery;
import org.jooq.SortField;
import org.jooq.Table;
import org.jooq.impl.DSL;

@Setter
@Accessors(fluent = true)
class SelectBuilder {

  private DSLContext dslContext;

  private ObjectRequest objectRequest;

  private List<SortCriteria> sortCriterias = List.of();

  private List<BackendFilterCriteria> filterCriterias = List.of();

  private ObjectFieldMapper<Map<String, Object>> fieldMapper;

  private AliasManager aliasManager;

  private SelectBuilder() {}

  public static SelectBuilder newSelect() {
    return new SelectBuilder();
  }

  public SelectQuery<Record> build() {
    // TODO null checks on class properties
    var objectType = getObjectType(objectRequest);

    var fromTable = findTable(objectType.getTable(), objectRequest.getContextCriteria()).as(aliasManager.newAlias());

    var selectQuery = dslContext.selectQuery(fromTable);

    createJoinConditions(fromTable).forEach(selectQuery::addConditions);

    objectRequest.getKeyCriteria()
        .stream()
        .map(keyCriteria -> createKeyConditions(keyCriteria, objectType, fromTable))
        .forEach(selectQuery::addConditions);

    objectRequest.getScalarFields()
        .stream()
        .map(selectedField -> processScalarField(selectedField, objectType, fromTable))
        .forEach(selectQuery::addSelect);

    objectRequest.getObjectFields()
        .entrySet()
        .stream()
        .flatMap(entry -> createNestedSelect(entry.getKey()
            .getName(), entry.getValue(), fromTable))
        .forEach(nestedSelect -> {
          var lateralTable = DSL.lateral(nestedSelect.asTable(aliasManager.newAlias()));
          selectQuery.addSelect(DSL.field(String.format("\"%s\".*", lateralTable.getName())));
          selectQuery.addJoin(lateralTable, JoinType.LEFT_OUTER_JOIN);
        });

    objectRequest.getSelectedObjectListFields()
        .entrySet()
        .stream()
        .flatMap(entry -> processObjectListFields(entry.getKey()
            .getName(), entry.getValue(), fromTable))
        .forEach(selectQuery::addSelect);

    createSortConditions(sortCriterias, fromTable).forEach(selectQuery::addOrderBy);

    filterCriterias.stream()
        .map(filterCriteria -> createFilterCondition(filterCriteria.getFieldPath(), filterCriteria.getValue(),
            fromTable))
        .forEach(selectQuery::addConditions);

    addPagingCriteria(selectQuery);

    return selectQuery;
  }

  public Condition createFilterCondition(List<ObjectFieldPath> fieldPath, Map<String, Object> value,
      Table<Record> table) {
    var node = fieldPath.size() > 1;

    var current = fieldPath.get(0);
    var objectField = (PostgresObjectField) current.getObjectField();

    if (node) {
      var fromTable =
          findTable(((PostgresObjectType) current.getObjectType()).getTable(), objectRequest.getContextCriteria())
              .as(aliasManager.newAlias());

      var selectQuery = dslContext.selectQuery(fromTable);

      selectQuery.addSelect(DSL.val(1));

      objectField.getJoinColumns()
          .forEach(joinColumn -> {
            var field = DSL.field(DSL.name(table.getName(), joinColumn.getName()));
            var referencedField = DSL.field(joinColumn.getReferencedField());
            selectQuery.addConditions(referencedField.equal(field));
          });

      var rest = fieldPath.subList(1, fieldPath.size());

      var nestedCondition = createFilterCondition(rest, value, fromTable);

      selectQuery.addConditions(nestedCondition);

      return DSL.exists(selectQuery);
    } else {
      var conditions = value.entrySet()
          .stream()
          .map(entry -> createFilterValue(entry.getKey(), DSL.field(objectField.getColumn()), entry.getValue()))
          .collect(Collectors.toList());

      return conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);
    }
  }

  @SuppressWarnings("unchecked")
  private Condition createFilterValue(String filterField, Field<Object> field, Object value) {
    if (FilterConstants.EQ_FIELD.equals(filterField)) {
      return field.eq(DSL.val(value));
    }

    if (FilterConstants.LT_FIELD.equals(filterField)) {
      return field.lt(DSL.val(value));
    }

    if (FilterConstants.LTE_FIELD.equals(filterField)) {
      return field.le(DSL.val(value));
    }

    if (FilterConstants.GT_FIELD.equals(filterField)) {
      return field.gt(DSL.val(value));
    }

    if (FilterConstants.GTE_FIELD.equals(filterField)) {
      return field.ge(DSL.val(value));
    }

    if (FilterConstants.IN_FIELD.equals(filterField)) {
      List<Object> list = (List<Object>) value;
      return field.in(list);
    }

    if (FilterConstants.NOT_FIELD.equals(filterField)) {
      Map<String, Object> mapValue = (Map<String, Object>) value;

      var conditions = mapValue.entrySet()
          .stream()
          .map(entry -> createFilterValue(entry.getKey(), field, entry.getValue()))
          .collect(Collectors.toList());

      var condition = conditions.size() > 1 ? DSL.and(conditions) : conditions.get(0);

      return DSL.not(condition);
    }

    throw illegalArgumentException("Unknown filter filterField '%s'", filterField);
  }



  @SuppressWarnings("rawtypes")
  public List<SortField> createSortConditions(List<SortCriteria> sortCriterias, Table<Record> table) {
    return sortCriterias.stream()
        .map(sortCriteria -> createSortCondition(sortCriteria, table))
        .collect(Collectors.toList());
  }

  private SortField<?> createSortCondition(SortCriteria sortCriteria, Table<Record> table) {
    if (sortCriteria.getFields()
        .size() > 1) {
      throw illegalStateException("Nested field path not supported!");
    }

    var leafObjectField = (PostgresObjectField) sortCriteria.getFields()
        .get(0);

    Field<?> sortField = DSL.field(DSL.name(table.getName(), leafObjectField.getColumn()));

    switch (sortCriteria.getDirection()) {
      case ASC:
        return sortField.asc();
      case DESC:
        return sortField.desc();
      default:
        throw unsupportedOperationException("Unsupported direction: {}", sortCriteria.getDirection());
    }
  }

  private Stream<Condition> createJoinConditions(Table<Record> table) {
    var source = objectRequest.getSource();

    if (source == null) {
      return Stream.empty();
    }

    return source.entrySet()
        .stream()
        .filter(entry -> entry.getKey()
            .startsWith(JOIN_KEY_PREFIX))
        .map(Map.Entry::getValue)
        .map(JoinCondition.class::cast)
        .map(JoinCondition::getFields)
        .flatMap(map -> map.entrySet()
            .stream())
        .map(entry -> DSL.field(DSL.name(table.getName(), entry.getKey()))
            .equal(entry.getValue()));
  }

  private void addPagingCriteria(SelectQuery<Record> selectQuery) {
    var source = objectRequest.getSource();

    if (source == null) {
      return;
    }

    Optional<Integer> offset = Optional.ofNullable(source.get(PAGING_KEY_PREFIX.concat(OFFSET_ARGUMENT_NAME)))
        .map(Integer.class::cast);
    Optional<Integer> first = Optional.ofNullable(source.get(PAGING_KEY_PREFIX.concat(FIRST_ARGUMENT_NAME)))
        .map(Integer.class::cast);

    if (offset.isPresent() && first.isPresent()) {
      selectQuery.addLimit(offset.get(), first.get());
    }
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

  private Stream<SelectQuery<Record>> createNestedSelect(String fieldName, ObjectRequest nestedObjectRequest,
      Table<Record> table) {
    var nestedObjectAlias = aliasManager.newAlias();
    var nestedObjectMapper = new ObjectMapper(nestedObjectAlias);

    fieldMapper.register(fieldName, nestedObjectMapper);

    var nestedSelect = SelectBuilder.newSelect()
        .dslContext(dslContext)
        .objectRequest(nestedObjectRequest)
        .fieldMapper(nestedObjectMapper)
        .aliasManager(aliasManager)
        .build();

    nestedSelect.addSelect(DSL.field("1")
        .as(nestedObjectAlias));

    getObjectField(objectRequest, fieldName).getJoinColumns()
        .forEach(joinColumn -> {
          var field = DSL.field(DSL.name(table.getName(), joinColumn.getName()));
          var referencedField = DSL.field(joinColumn.getReferencedField());
          nestedSelect.addConditions(referencedField.equal(field));
        });

    return Stream.of(nestedSelect);
  }

  private Stream<SelectFieldOrAsterisk> processObjectListFields(String fieldName, CollectionRequest collectionRequest,
      Table<Record> table) {
    var objectField = getObjectField(objectRequest, fieldName);

    if (objectField.getMappedBy() != null) {
      var nestedObjectField = getObjectField(collectionRequest.getObjectRequest(), objectField.getMappedBy());

      // Provide join info for child data fetcher
      fieldMapper.register(JOIN_KEY_PREFIX.concat(fieldName),
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
