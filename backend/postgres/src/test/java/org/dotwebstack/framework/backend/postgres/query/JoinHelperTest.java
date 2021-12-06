package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsCollectionWithSize;
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
        .getName(), CoreMatchers.equalTo("field_name_right"));
    assertThat(result.get(0)
        .getReferencedColumn(), CoreMatchers.equalTo("columnNameLeft"));
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

    assertThat(result, CoreMatchers.equalTo(Boolean.TRUE));
  }

  @Test
  void hasNestedReference_returnsFalse_forObjectField() {
    var objectField = new PostgresObjectField();

    var targetObjectType = new PostgresObjectType();
    targetObjectType.setTable("nestedtable");
    objectField.setTargetType(targetObjectType);

    var result = JoinHelper.hasNestedReference(objectField);

    assertThat(result, CoreMatchers.equalTo(Boolean.FALSE));
  }

  @Test
  void resolveJoinTable_returnsResolvedJoinTable_forNestedReference() {
    var fieldref = new PostgresObjectField();
    fieldref.setColumn("fieldrefcolumn");

    var objectType = new PostgresObjectType();
    objectType.setFields(Map.of("fieldref", fieldref));

    var joinTable = createJoinTable();

    var result = JoinHelper.resolveJoinTable(objectType, joinTable);

    assertThat(result, CoreMatchers.notNullValue());
    assertThat(result.getJoinColumns(), IsCollectionWithSize.hasSize(1));
    assertThat(result.getJoinColumns()
        .get(0)
        .getReferencedColumn(), CoreMatchers.equalTo("fieldrefcolumn"));
    assertThat(result.getJoinColumns()
        .get(0)
        .getReferencedField(), CoreMatchers.nullValue());
    assertThat(result.getInverseJoinColumns(), IsCollectionWithSize.hasSize(1));
    assertThat(result.getInverseJoinColumns()
        .get(0)
        .getReferencedField(), CoreMatchers.equalTo("bar"));
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

}
