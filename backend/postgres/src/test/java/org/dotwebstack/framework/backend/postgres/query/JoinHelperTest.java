package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.createJoinConditions;
import static org.dotwebstack.framework.backend.postgres.query.JoinHelper.getExistFieldForRelationObject;
import static org.dotwebstack.framework.backend.postgres.query.QueryHelper.findTable;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.model.Context;
import org.dotwebstack.framework.core.model.ContextField;
import org.dotwebstack.framework.core.query.model.ContextCriteria;
import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

class JoinHelperTest {

  @Test
  void invertOnList_inverted_forList() {
    var objectField = new PostgresObjectField();
    objectField.setList(true);

    var targetObjectType = new PostgresObjectType();
    var field = new PostgresObjectField();
    field.setColumn("field_name_right");
    targetObjectType.setFields(Map.of("fieldNameRight", field));
    objectField.setTargetType(targetObjectType);

    List<JoinColumn> joinColumns = new ArrayList<>();
    var joinColumn = new JoinColumn();
    joinColumn.setName("columnNameLeft");
    joinColumn.setReferencedField("fieldNameRight");
    joinColumns.add(joinColumn);

    var result = JoinHelper.invertOnList(objectField, joinColumns);

    assertThat(result, IsCollectionWithSize.hasSize(1));
    assertThat(result.get(0)
        .getName(), equalTo("field_name_right"));
    assertThat(result.get(0)
        .getReferencedColumn(), equalTo("columnNameLeft"));
    assertThat(result.get(0)
        .getReferencedField(), CoreMatchers.nullValue());
  }

  @Test
  void hasNestedReference_returnsTrue_forObjectField() {
    var objectField = new PostgresObjectField();

    objectField.setJoinTable(createJoinTable());

    var targetObjectType = new PostgresObjectType();
    objectField.setTargetType(targetObjectType);

    var result = JoinHelper.hasNestedReference(objectField);

    assertThat(result, equalTo(Boolean.TRUE));
  }

  @Test
  void hasNestedReference_returnsFalse_forObjectField() {
    var objectField = new PostgresObjectField();

    var targetObjectType = new PostgresObjectType();
    targetObjectType.setTable("nestedtable");
    objectField.setTargetType(targetObjectType);

    var result = JoinHelper.hasNestedReference(objectField);

    assertThat(result, equalTo(Boolean.FALSE));
  }

  @Test
  void resolveJoinTable_returnsResolvedJoinTable_forNestedReference() {
    var fieldref = new PostgresObjectField();
    fieldref.setColumn("fieldrefcolumn");

    var objectType = new PostgresObjectType();
    objectType.setFields(Map.of("fieldref", fieldref));

    var joinTable = createJoinTable();

    var result = JoinHelper.resolveJoinTable(objectType, joinTable);

    assertThat(result, notNullValue());
    assertThat(result.getJoinColumns(), IsCollectionWithSize.hasSize(1));
    assertThat(result.getJoinColumns()
        .get(0)
        .getReferencedColumn(), equalTo("fieldrefcolumn"));
    assertThat(result.getJoinColumns()
        .get(0)
        .getReferencedField(), CoreMatchers.nullValue());
    assertThat(result.getInverseJoinColumns(), IsCollectionWithSize.hasSize(1));
    assertThat(result.getInverseJoinColumns()
        .get(0)
        .getReferencedField(), equalTo("bar"));
  }

  @Test
  void getExistFieldForRelationObject_returnsExistsField_forJoinColumnWithReferencedField() {
    List<JoinColumn> joinColumns = new ArrayList<>();

    var first = new JoinColumn();
    first.setName("scope");
    first.setReferencedColumn("scope_column");
    joinColumns.add(first);

    var second = new JoinColumn();
    second.setName("id");
    second.setReferencedField("ref.id");
    joinColumns.add(second);

    var table = DSL.table("table");

    var result = getExistFieldForRelationObject(joinColumns, table, "x1");

    assertThat(result, notNullValue());
    assertThat(result.getName(), equalTo("x1"));
  }

  @Test
  void getExistFieldForRelationObject_throwsException_forJoinColumnWithoutReferencedField() {
    List<JoinColumn> joinColumns = List.of();

    var table = DSL.table("table");

    var thrown =
        assertThrows(IllegalArgumentException.class, () -> getExistFieldForRelationObject(joinColumns, table, "x1"));

    assertThat(thrown.getMessage(), equalTo("Expected a joinColumn with a referencedField but got nothing!"));
  }

  private JoinTable createJoinTable() {
    List<JoinColumn> joinColumns = new ArrayList<>();
    var joinColumn = new JoinColumn();
    joinColumn.setReferencedField("fieldref");
    joinColumns.add(joinColumn);

    List<JoinColumn> inverseJoinColumns = new ArrayList<>();

    var inverseJoinColumn = new JoinColumn();
    inverseJoinColumn.setReferencedField("foo.bar");
    inverseJoinColumns.add(inverseJoinColumn);

    var joinTable = new JoinTable();
    joinTable.setJoinColumns(joinColumns);
    joinTable.setInverseJoinColumns(inverseJoinColumns);

    return joinTable;
  }

  @Test
  void createJoinConditions_returnsCondition_forJoinColumnWithoutReferencedColumn() {
    var context = new Context();
    context.setFields(Map.of("arg", mock(ContextField.class)));

    var contextCriteria = ContextCriteria.builder()
        .name("test")
        .context(context)
        .values(Map.of("arg", "val"))
        .build();

    Table<Record> junctionTable = findTable("table1", contextCriteria);
    Table<Record> referencedTable = findTable("table2", contextCriteria);
    JoinColumn joinColumn = mock(JoinColumn.class);
    when(joinColumn.getName()).thenReturn("arg");
    List<JoinColumn> joinColumns = List.of(joinColumn);
    when(joinColumn.getReferencedField()).thenReturn("any");
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    PostgresObjectField field = mock(PostgresObjectField.class);
    when(field.getColumn()).thenReturn("arg");
    when(objectType.getField(any(String.class))).thenReturn(field);

    var result = createJoinConditions(junctionTable, referencedTable, joinColumns, objectType);

    assertThat(result, notNullValue());
    assertThat(result.toString(), is("\"table1_test_ctx({0})\".\"arg\" = \"table2_test_ctx({0})\".\"arg\""));
  }

  @Test
  void createJoinConditions_returnsCondition_forJoinColumnWithReferencedColumn() {
    var junctionTable = DSL.table("junction_table");
    var referencedTable = DSL.table("reference_table");
    var joinColumns = List.of(createJoinColumn());
    var result = createJoinConditions(junctionTable, referencedTable, joinColumns);

    assertThat(result, notNullValue());
    assertThat(result.toString(), is("\"junction_table\".\"junction__id\" = \"reference_table\".\"id\""));
  }

  private JoinColumn createJoinColumn() {
    var joinColumn = new JoinColumn();
    joinColumn.setName("junction__id");
    joinColumn.setReferencedColumn("id");
    return joinColumn;
  }

}
