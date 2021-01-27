package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.invalidConfigurationException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.lateral;
import static org.jooq.impl.DSL.trueCondition;

import graphql.schema.GraphQLTypeUtil;
import graphql.schema.SelectedField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.JoinTable;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.LoadEnvironment;
import org.dotwebstack.framework.core.datafetchers.filters.FieldFilter;
import org.dotwebstack.framework.core.datafetchers.filters.Filter;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.Table;
import org.jooq.TableLike;
import org.jooq.impl.DSL;

public class PostgresQueryBuilder {

  private final DotWebStackConfiguration dotWebStackConfiguration;

  private final DSLContext dslContext;

  private final AtomicInteger tableCounter = new AtomicInteger();

  private final AtomicInteger selectCounter = new AtomicInteger();

  public PostgresQueryBuilder(DotWebStackConfiguration dotWebStackConfiguration, DSLContext dslContext) {
    this.dotWebStackConfiguration = dotWebStackConfiguration;
    this.dslContext = dslContext;
  }

  public PostgresQueryHolder build(PostgresTypeConfiguration typeConfiguration, LoadEnvironment loadEnvironment,
      List<Filter> filters) {
    return build(typeConfiguration, loadEnvironment, null, filters);
  }

  private PostgresQueryHolder build(PostgresTypeConfiguration typeConfiguration, LoadEnvironment loadEnvironment,
      JoinInformation joinInformation, List<Filter> filters) {
    List<Table<Record>> fromTables = new ArrayList<>();

    JoinTable joinTable = null;
    if (loadEnvironment.getExecutionStepInfo()
        .getParent()
        .getFieldDefinition() != null) {
      String parentTypeName = TypeHelper.getTypeName(loadEnvironment.getExecutionStepInfo()
          .getParent()
          .getFieldDefinition()
          .getType());
      TypeConfiguration parentType = dotWebStackConfiguration.getTypeMapping()
          .get(parentTypeName);

      String fieldName = loadEnvironment.getExecutionStepInfo()
          .getFieldDefinition()
          .getName();

      PostgresFieldConfiguration fieldConfiguration = (PostgresFieldConfiguration) parentType.getFields()
          .get(fieldName);

      if (fieldConfiguration.getJoinTable() != null) {
        joinTable = fieldConfiguration.getJoinTable();

      }
    }

    Map<String, Object> fieldAliasMap = new HashMap<>();

    List<Field<Object>> selectedColumns =
        getDirectFields(typeConfiguration, loadEnvironment.getSelectedFields(), fieldAliasMap);

    Table<Record> fromTable = typeConfiguration.getSqlTable()
        .as(newTableAlias());
    fromTables.add(fromTable);

    Table<Record> fromJoinTable = null;
    if (joinTable != null) {
      fromJoinTable = DSL.table(joinTable.getName())
          .as(newTableAlias());

      Field<Object> aggregateKey = DSL.field(fromJoinTable.getName()
          .concat(".")
          .concat(joinTable.getJoinColumns()
              .get(0)
              .getName()));
      selectedColumns.add(aggregateKey);

      fromTables.add(fromJoinTable);
    }


    List<NestedQueryResult> nestedQueryResults = getNestedResults(typeConfiguration,
        loadEnvironment.getSelectedFields(), fromTable.getName(), fieldAliasMap, selectedColumns);

    SelectJoinStep<Record> query = dslContext.select(selectedColumns)
        .from(fromTables);

    for (NestedQueryResult nestedResult : nestedQueryResults) {
      Table<Record> nestedQuery = nestedResult.getTable();
      query.leftJoin(lateral(nestedQuery))
          .on(trueCondition());
    }

    if (joinInformation != null) {
      Field<Object> self = field(fromTable.getName()
          .concat(".")
          .concat(joinInformation.getReferencedField()));
      query.where(joinInformation.getParent()
          .eq(self));
    }

    if (joinTable != null) {
      // t3.beers_identifier = t1.identifier
      // AND t3.ingredients_identifier = t4.identifier
      System.out.println();

      for (JoinColumn joinColumn : joinTable.getJoinColumns()) {
        Field<Object> left = DSL.field(fromJoinTable.getName()
            .concat(".")
            .concat(joinColumn.getName()));

        Object[] values = filters.stream()
            .map(FieldFilter.class::cast)
            .map(FieldFilter::getValue)
            .toArray(Object[]::new);

        query.where(left.in(values));
      }

      for (JoinColumn joinColumn : joinTable.getInverseJoinColumns()) {
        Field<Object> left = DSL.field(fromJoinTable.getName()
            .concat(".")
            .concat(joinColumn.getName()));
        Field<Object> right = DSL.field(fromTable.getName()
            .concat(".")
            .concat(joinColumn.getReferencedField()));
        query.where(left.eq(right));
      }

    }

    if (filters != null && filters.size() > 0 && joinTable == null) {
      filters.forEach(filter -> filter.flatten()
          .stream()
          .map(FieldFilter.class::cast)
          .forEach(fieldKey -> query.where(field(fromTable.getName()
              .concat(".")
              .concat(fieldKey.getField())).eq(fieldKey.getValue()))));;
    }


    return PostgresQueryHolder.builder()
        .query(query)
        .fieldAliasMap(fieldAliasMap)
        .build();
  }

  private List<NestedQueryResult> getNestedResults(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, String tableName, Map<String, Object> fieldAliasMap,
      List<Field<Object>> selectedColumns) {
    return selectedFields.stream()
        .filter(selectedField -> !GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .filter(
            selectedField -> !GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(selectedField.getFieldDefinition()
                .getType())))
        .map(selectedField -> processNested(getJoinInformation(typeConfiguration, selectedField, tableName),
            selectedField))
        .map(Optional::get)
        .peek(nestedQueryResult -> {
          fieldAliasMap.put(nestedQueryResult.getSelectedField()
              .getResultKey(), nestedQueryResult.getColumnAliasMap());
          selectedColumns.add(nestedQueryResult.getSelectedColumn());
        })
        .collect(Collectors.toList());
  }

  private JoinInformation getJoinInformation(PostgresTypeConfiguration typeConfiguration, SelectedField selectedField,
      String tableName) {
    PostgresFieldConfiguration postgresFieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());

    if (postgresFieldConfiguration.getMappedBy() != null) {
      // throw notImplementedException("The 'mappedBy' configuration item is not implemented yet!");
      return null;
    }

    if (postgresFieldConfiguration.getJoinColumns() == null) {
      throw invalidConfigurationException("Missing relation configuration for table '{}' and field `{}`", tableName,
          selectedField.getName());
    }

    if (postgresFieldConfiguration.getJoinColumns()
        .size() > 1) {
      throw unsupportedOperationException("Multiple joinColumns for a single property aren't supported yet!");
    }

    JoinColumn joinColumn = postgresFieldConfiguration.getJoinColumns()
        .get(0);

    return JoinInformation.builder()
        .parent(field(tableName.concat(".")
            .concat(joinColumn.getName())))
        .referencedField(joinColumn.getReferencedField())
        .build();
  }

  private List<Field<Object>> getDirectFields(PostgresTypeConfiguration typeConfiguration,
      List<SelectedField> selectedFields, Map<String, Object> fieldAliasMap) {
    return selectedFields.stream()
        .filter(selectedField -> GraphQLTypeUtil.isLeaf(selectedField.getFieldDefinition()
            .getType()))
        .map(selectedField -> {
          Field<Object> column = createSelectedColumn(typeConfiguration, selectedField);
          fieldAliasMap.put(selectedField.getResultKey(), column.getName());
          return column;
        })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private Optional<NestedQueryResult> processNested(JoinInformation joinInformation, SelectedField nestedField) {
    String nestedName = GraphQLTypeUtil.unwrapAll(nestedField.getFieldDefinition()
        .getType())
        .getName();

    TypeConfiguration<?> nestedTypeConfiguration = dotWebStackConfiguration.getTypeMapping()
        .get(nestedName);

    // Non-Postgres-backed objects can never be eager-loaded
    if (!(nestedTypeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    List<SelectedField> nestedSelectedFields = nestedField.getSelectionSet()
        .getImmediateFields();

    LoadEnvironment loadEnvironment = LoadEnvironment.builder()
        .selectedFields(nestedSelectedFields)
        .build();

    PostgresQueryHolder queryHolder =
        build((PostgresTypeConfiguration) nestedTypeConfiguration, loadEnvironment, joinInformation, null);

    String joinAlias = newTableAlias();

    return Optional.of(NestedQueryResult.builder()
        .selectedField(nestedField)
        .selectedColumn(field(joinAlias.concat(".*")))
        .typeConfiguration((PostgresTypeConfiguration) nestedTypeConfiguration)
        .table(((TableLike<Record>) queryHolder.getQuery()).asTable(joinAlias))
        .columnAliasMap(queryHolder.getFieldAliasMap())
        .build());
  }

  private Field<Object> createSelectedColumn(PostgresTypeConfiguration typeConfiguration, SelectedField selectedField) {
    PostgresFieldConfiguration fieldConfiguration = typeConfiguration.getFields()
        .get(selectedField.getName());

    return field(fieldConfiguration.getSqlColumnName()).as(newSelectAlias());
  }

  private String newTableAlias() {
    return "t".concat(String.valueOf(tableCounter.incrementAndGet()));
  }

  private String newSelectAlias() {
    return "x".concat(String.valueOf(selectCounter.incrementAndGet()));
  }
}
