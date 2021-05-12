package org.dotwebstack.framework.backend.postgres.query;

import static graphql.schema.GraphQLTypeUtil.isList;
import static graphql.schema.GraphQLTypeUtil.unwrapNonNull;
import static org.dotwebstack.framework.backend.postgres.query.QueryUtil.createMapAssembler;
import static org.dotwebstack.framework.core.datafetchers.aggregate.AggregateHelper.isAggregate;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalStateException;
import static org.dotwebstack.framework.core.helpers.ExceptionHelper.unsupportedOperationException;

import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dotwebstack.framework.backend.postgres.config.JoinColumn;
import org.dotwebstack.framework.backend.postgres.config.PostgresFieldConfiguration;
import org.dotwebstack.framework.backend.postgres.config.PostgresTypeConfiguration;
import org.dotwebstack.framework.core.config.DotWebStackConfiguration;
import org.dotwebstack.framework.core.config.KeyConfiguration;
import org.dotwebstack.framework.core.config.TypeConfiguration;
import org.dotwebstack.framework.core.datafetchers.aggregate.AggregateConstants;
import org.dotwebstack.framework.core.helpers.TypeHelper;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class DefaultSelectWrapperBuilder extends AbstractSelectWrapperBuilder {

  private final SelectWrapperBuilderFactory factory;

  private final DotWebStackConfiguration dotWebStackConfiguration;

  public DefaultSelectWrapperBuilder(DSLContext dslContext, SelectWrapperBuilderFactory factory,
      DotWebStackConfiguration dotWebStackConfiguration) {
    super(dslContext);
    this.factory = factory;
    this.dotWebStackConfiguration = dotWebStackConfiguration;
  }

  @Override
  public void addFields(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      Table<Record> fromTable, Map<String, SelectedField> selectedFields) {
    Map<String, PostgresFieldConfiguration> fieldNamesConfigurations =
        getFieldNames(typeConfiguration, selectedFields.values());

    var hasJoinCondition = new AtomicBoolean();

    // nested object using same table
    fieldNamesConfigurations.entrySet()
        .stream()
        .filter(entry -> entry.getValue()
            .isNested())
        .forEach(entry -> addNestedObject(selectContext, fromTable, selectedFields.get(entry.getKey())));

    // aggregate/nested object using join table/column
    fieldNamesConfigurations.entrySet()
        .stream()
        .filter(entry -> entry.getValue()
            .isSubselect())
        .forEach(entry -> {
          hasJoinCondition.set(true);
          addObjectField(selectContext, fromTable, selectedFields.get(entry.getKey()), entry.getValue());
        });

    // add scalar fields
    fieldNamesConfigurations.entrySet()
        .stream()
        .filter(entry -> entry.getValue()
            .isScalar())
        .forEach(entry -> addScalarField(selectContext, typeConfiguration, fromTable, entry));

    if (hasJoinCondition.get()) {
      typeConfiguration.getReferencedColumns()
          .keySet()
          .forEach(column -> addScalarField(selectContext, typeConfiguration, fromTable,
              Map.entry(column, typeConfiguration.getFields()
                  .get(column))));
    }
  }

  private void addObjectField(SelectContext selectContext, Table<Record> fromTable, SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration) {
    List<UnaryOperator<Map<String, Object>>> assembleFnsList = new ArrayList<>();

    // Aggregate stringjoin
    Map<String, SelectedField> selectedStringJoinFieldsByName =
        getSelectedAggregateFieldsByName(selectedField, selectContext.getQueryContext()
            .getSelectionSet(), true);

    selectedStringJoinFieldsByName.forEach((name, field) -> processSubSelect(assembleFnsList, selectContext, fromTable,
        selectedField, fieldConfiguration, Collections.singletonMap(name, field)));

    // Aggregate field or object with joincolumn/jointable
    Map<String, SelectedField> otherFieldsByName =
        getSelectedAggregateFieldsByName(selectedField, selectContext.getQueryContext()
            .getSelectionSet(), false);

    if (!otherFieldsByName.isEmpty()) {
      processSubSelect(assembleFnsList, selectContext, fromTable, selectedField, fieldConfiguration, otherFieldsByName);
    }

    if (isAggregate(fieldConfiguration) && !assembleFnsList.isEmpty()) {
      selectContext.getAssembleFns()
          .put(selectedField.getName(), multiSelectRowAssembler(assembleFnsList)::apply);
    } else if (assembleFnsList.size() == 1) {
      selectContext.getAssembleFns()
          .put(selectedField.getName(), assembleFnsList.get(0)::apply);
    }
  }

  private void addNestedObject(SelectContext selectContext, Table<Record> fromTable, SelectedField selectedField) {
    String typeName = GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
        .getType())
        .getName();
    PostgresTypeConfiguration typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(typeName);
    var context = new SelectContext(selectContext.getQueryContext());

    Map<String, SelectedField> selectedFields =
        getSelectedAggregateFieldsByName(selectedField, selectContext.getQueryContext()
            .getSelectionSet(), false);

    selectedFields.keySet()
        .stream()
        .map(key -> new AbstractMap.SimpleEntry<>(key, typeConfiguration.getFields()
            .get(key)))
        .forEach(entry -> addScalarField(context, typeConfiguration, fromTable, entry));

    selectContext.getSelectColumns()
        .addAll(context.getSelectColumns());

    selectContext.getAssembleFns()
        .put(selectedField.getName(), createMapAssembler(context)::apply);
  }

  private void processSubSelect(List<UnaryOperator<Map<String, Object>>> assembleFnsList, SelectContext selectContext,
      Table<Record> fromTable, SelectedField selectedField, PostgresFieldConfiguration fieldConfiguration,
      Map<String, SelectedField> selectedFields) {

    var subSelectContext = new SelectContext(selectContext.getQueryContext());

    executeSubSelect(subSelectContext, selectedField, fieldConfiguration, fromTable, selectedFields)
        .ifPresent(joinTableWrapper -> {
          selectContext.getSelectColumns()
              .add(DSL.field(joinTableWrapper.getTable()
                  .getName()
                  .concat(".*")));

          selectContext.getJoinTables()
              .add(joinTableWrapper.getTable());

          assembleFnsList.add(joinTableWrapper.getRowAssembler());
        });
  }

  private void addScalarField(SelectContext selectContext, PostgresTypeConfiguration typeConfiguration,
      Table<Record> fromTable, Map.Entry<String, PostgresFieldConfiguration> fieldNameConfiguration) {
    String columnAlias = selectContext.getQueryContext()
        .newSelectAlias();

    Field<Object> column = DSL.field(DSL.name(fromTable.getName(), fieldNameConfiguration.getValue()
        .getColumn()))
        .as(columnAlias);

    selectContext.addField(fieldNameConfiguration.getKey(), column);

    if (typeConfiguration.getKeys()
        .stream()
        .anyMatch(keyConfiguration -> Objects.equals(keyConfiguration.getField(), fieldNameConfiguration.getKey()))) {
      selectContext.getCheckNullAlias()
          .set(columnAlias);
    }
  }


  private Map<String, PostgresFieldConfiguration> getFieldNames(PostgresTypeConfiguration typeConfiguration,
      Collection<SelectedField> selectedFields) {

    return Stream.concat(typeConfiguration.getKeys()
        .stream()
        .map(KeyConfiguration::getField),
        selectedFields.stream()
            .map(SelectedField::getName))
        .collect(Collectors.toMap(key -> key, key -> typeConfiguration.getFields()
            .get(key), (a, b) -> a));
  }

  private Optional<QueryBuilder.TableWrapper> executeSubSelect(SelectContext selectContext, SelectedField selectedField,
      PostgresFieldConfiguration fieldConfiguration, Table<Record> fromTable,
      Map<String, SelectedField> selectedFields) {

    // Never construct joins for nested lists
    if (isList(unwrapNonNull(selectedField.getFieldDefinition()
        .getType()))) {
      return Optional.empty();
    }

    String foreignTypeName = getForeignTypeName(selectedField, fieldConfiguration);
    TypeConfiguration<?> typeConfiguration = dotWebStackConfiguration.getTypeConfiguration(foreignTypeName);

    // Non-Postgres backends can never be eager loaded
    if (!(typeConfiguration instanceof PostgresTypeConfiguration)) {
      return Optional.empty();
    }

    if (fieldConfiguration.getJoinColumns() != null || fieldConfiguration.getJoinTable() != null) {
      var selectWrapperBuilder = factory.getSelectWrapperBuilder(fieldConfiguration);

      var selectWrapper = selectWrapperBuilder.build(selectContext, (PostgresTypeConfiguration) typeConfiguration,
          fieldConfiguration.getJoinTable(), selectedFields);

      final var otherSideTypeConfiguration = getPostgresTypeConfigurationForCondition(fieldConfiguration, selectedField,
          (PostgresTypeConfiguration) typeConfiguration);

      var whereCondition = fieldConfiguration.findJoinColumns()
          .stream()
          .map(joinColumn -> createJoinTableCondition(otherSideTypeConfiguration, fieldConfiguration, joinColumn,
              fromTable, selectWrapper.getTable()))
          .reduce(DSL.noCondition(), Condition::and);

      var tableWrapper = QueryBuilder.TableWrapper.builder()
          .table(DSL.lateral(selectWrapper.getQuery()
              .where(whereCondition)
              .limit(1))
              .asTable(selectContext.getQueryContext()
                  .newTableAlias()))
          .rowAssembler(selectWrapper.getRowAssembler())
          .build();

      return Optional.of(tableWrapper);
    }

    if (fieldConfiguration.getMappedBy() != null) {
      return Optional.empty();
    }

    throw unsupportedOperationException("Unsupported field configuration!");
  }


  private Condition createJoinTableCondition(PostgresTypeConfiguration otherSideTypeConfiguration,
      PostgresFieldConfiguration fieldConfiguration, JoinColumn joinColumn, Table<Record> leftTable,
      Table<Record> rightTable) {
    PostgresFieldConfiguration otherSideFieldConfiguration = otherSideTypeConfiguration.getFields()
        .get(joinColumn.getField());

    boolean invert = fieldConfiguration.isAggregate() || fieldConfiguration.getJoinTable() != null;

    var leftColumn =
        DSL.name(leftTable.getName(), invert ? otherSideFieldConfiguration.getColumn() : joinColumn.getName());
    var rightColumn =
        DSL.name(rightTable.getName(), invert ? joinColumn.getName() : otherSideFieldConfiguration.getColumn());

    return DSL.field(leftColumn)
        .eq(DSL.field(rightColumn));
  }

  private String getForeignTypeName(SelectedField selectedField, PostgresFieldConfiguration fieldConfiguration) {
    if (isAggregate(fieldConfiguration)) {
      return fieldConfiguration.getAggregationOf();
    }

    GraphQLUnmodifiedType foreignType = GraphQLTypeUtil.unwrapAll(selectedField.getFieldDefinition()
        .getType());

    if (!(foreignType instanceof GraphQLObjectType)) {
      throw illegalStateException("Foreign output type is not an object type.");
    }
    return foreignType.getName();
  }

  private PostgresTypeConfiguration getPostgresTypeConfigurationForCondition(
      PostgresFieldConfiguration fieldConfiguration, SelectedField selectedField,
      PostgresTypeConfiguration rightTypeConfiguration) {
    if (isAggregate(fieldConfiguration) || fieldConfiguration.getJoinTable() != null) {
      return dotWebStackConfiguration.getTypeConfiguration(TypeHelper.getTypeName(selectedField.getObjectType()));
    }

    return rightTypeConfiguration;
  }

  private Map<String, SelectedField> getSelectedAggregateFieldsByName(SelectedField selectedField,
      DataFetchingFieldSelectionSet selectionSet, boolean stringJoin) {
    String fieldPathPrefix = selectedField.getFullyQualifiedName()
        .concat("/");
    return selectionSet.getFields(fieldPathPrefix.concat("*.*"))
        .stream()
        .filter(field -> AggregateConstants.STRING_JOIN_FIELD.equals(field.getName()) == stringJoin)
        .collect(Collectors.toMap(field -> Optional.ofNullable(field.getAlias())
            .orElse(field.getName()), Function.identity()));
  }

  private UnaryOperator<Map<String, Object>> multiSelectRowAssembler(
      List<UnaryOperator<Map<String, Object>>> assembleFnsList) {

    return row -> assembleFnsList.stream()
        .collect(HashMap::new, (acc, assembler) -> acc.putAll(assembler.apply(row)), HashMap::putAll);
  }
}
