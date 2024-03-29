package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;
import static org.jooq.impl.DefaultDataType.getDefaultDataType;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.backend.query.AliasManager;
import org.dotwebstack.framework.core.config.FieldEnumConfiguration;
import org.dotwebstack.framework.core.model.ObjectField;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.dotwebstack.framework.core.query.model.SingleObjectRequest;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;

class QueryHelper {

  private QueryHelper() {}

  private static Name name(Table<Record> table, String columnName) {
    if (table == null) {
      return DSL.name(columnName);
    }
    return DSL.name(table.getName(), columnName);
  }

  public static Field<Object> column(String columnName) {
    return DSL.field(DSL.name(columnName));
  }

  public static Field<Object> column(Table<Record> table, String columnName) {
    return DSL.field(name(table, columnName));
  }

  public static Field<Object> column(Table<Record> table, JoinColumn joinColumn, PostgresObjectType objectType) {
    return DSL.field(name(table, columnName(joinColumn, objectType)));
  }

  public static String columnName(JoinColumn joinColumn, PostgresObjectType objectType) {
    return Optional.ofNullable(joinColumn.getReferencedColumn())
        .orElseGet(() -> getColumnNameOfReferencedField(joinColumn, objectType));
  }

  private static String getColumnNameOfReferencedField(JoinColumn joinColumn, PostgresObjectType objectType) {
    return objectType.getField(joinColumn.getReferencedField())
        .getColumn();
  }

  public static PostgresObjectType getObjectType(SingleObjectRequest objectRequest) {
    var objectType = objectRequest.getObjectType();

    if (!(objectType instanceof PostgresObjectType)) {
      throw illegalArgumentException("Object type has wrong type.");
    }

    return (PostgresObjectType) objectType;
  }

  public static PostgresObjectField getObjectField(SingleObjectRequest objectRequest, String name) {
    return getObjectType(objectRequest).getField(name);
  }

  public static Table<Record> findTable(String name, ContextCriteria contextCriteria) {
    if (contextCriteria != null) {
      return createTable(name, contextCriteria);
    }

    return DSL.table(DSL.name(name.split("\\.")));
  }

  private static Table<Record> createTable(String name, ContextCriteria contextCriteria) {
    AtomicInteger atomicInteger = new AtomicInteger(0);

    var bindingKeys = new ArrayList<String>();
    var bindingValues = new ArrayList<>();

    contextCriteria.getContext()
        .getFields()
        .forEach((fieldName, contextField) -> {
          bindingKeys.add(String.format("{%d}", atomicInteger.getAndIncrement()));
          bindingValues.add(contextCriteria.getValues()
              .get(fieldName));
        });

    var joinedBindingKeys = String.join(",", bindingKeys);

    return DSL.table(String.format("%s_%s_ctx(%s)", name, contextCriteria.getName(), joinedBindingKeys),
        bindingValues.toArray(Object[]::new));
  }

  public static Function<String, Table<Record>> createTableCreator(SelectQuery<?> query,
      ContextCriteria contextCriteria, AliasManager aliasManager) {
    return tableName -> {
      var requestedTable = QueryHelper.findTable(tableName, contextCriteria)
          .as(aliasManager.newAlias());

      query.addFrom(requestedTable);
      return requestedTable;
    };
  }

  public static Field<Object> getFieldValue(ObjectField field, Object fieldValue) {
    return Optional.of(field)
        .map(ObjectField::getEnumeration)
        .map(FieldEnumConfiguration::getType)
        .map(type -> {
          var dataType = getDefaultDataType(SQLDialect.POSTGRES, type);
          return DSL.val(fieldValue)
              .cast(dataType);
        })
        .orElse(DSL.val(fieldValue));
  }
}
