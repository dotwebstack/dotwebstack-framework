package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.JoinTable;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

class JoinBuilderTest {

  @Test
  void build_returnsJoinConditions_forMappedByJoinColumn() {
    var mappedByObjectField = new PostgresObjectField();
    mappedByObjectField.setJoinColumns(createJoinColumns());

    var parentObjectType = createObjectType();
    var childObjectType = createObjectType();
    mappedByObjectField.setObjectType(parentObjectType);
    mappedByObjectField.setTargetType(childObjectType);

    var objectField = new PostgresObjectField();
    objectField.setObjectType(childObjectType);
    objectField.setTargetType(parentObjectType);
    objectField.setMappedByObjectField(mappedByObjectField);

    var result = newJoin().joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .table(createTable("x1"))
        .relatedTable(createTable("x2"))
        .build();

    assertThat(result, notNullValue());
    assertThat(result.toString(), is("(\n" + "  \"x2\".\"a_identifier\" = \"x1\".\"b_identifier\"\n"
        + "  and \"x2\".\"c_identifier\" = \"x1\".\"d_identifier\"\n" + ")"));
  }

  @Test
  void build_returnsJoinConditions_forMappedByJoinTable() {
    var joinTable = new JoinTable();
    joinTable.setName("p_c");

    var joinColumn = new JoinColumn();
    joinColumn.setName("k_p_identifier");
    joinColumn.setReferencedField("p_identifier_f");
    joinTable.setJoinColumns(List.of(joinColumn));

    var inverseJoinColumn = new JoinColumn();
    inverseJoinColumn.setName("k_c_identifier");
    inverseJoinColumn.setReferencedField("c_identifier_f");
    joinTable.setInverseJoinColumns(List.of(inverseJoinColumn));

    var mappedByObjectField = new PostgresObjectField();
    mappedByObjectField.setJoinTable(joinTable);

    var parentObjectType = createObjectType();
    var parentIdentifierField = new PostgresObjectField();
    parentIdentifierField.setColumn("p_identifier_f_column");
    parentObjectType.setFields(Map.of("p_identifier_f", parentIdentifierField));

    var childObjectType = createObjectType();
    var childIdentifierField = new PostgresObjectField();
    childIdentifierField.setColumn("c_identifier_f_column");
    childObjectType.setFields(Map.of("c_identifier_f", childIdentifierField));

    mappedByObjectField.setObjectType(parentObjectType);
    mappedByObjectField.setTargetType(childObjectType);

    var objectField = new PostgresObjectField();
    objectField.setMappedByObjectField(mappedByObjectField);
    objectField.setObjectType(childObjectType);
    objectField.setTargetType(parentObjectType);

    var result = newJoin().joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .table(createTable("x1"))
        .relatedTable(createTable("x2"))
        .tableCreator(junctionTable -> DSL.table(junctionTable)
            .as("x3"))
        .build();

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("(\n" + "  \"x3\".\"k_p_identifier\" = \"x2\".\"p_identifier_f_column\"\n"
        + "  and \"x3\".\"k_c_identifier\" = \"x1\".\"c_identifier_f_column\"\n" + ")"));
  }

  @Test
  void build_returnsListConditions_forJoinColumn() {
    var objectField = new PostgresObjectField();
    objectField.setJoinColumns(createJoinColumns());

    var objectType = createObjectType();
    objectField.setObjectType(objectType);
    objectField.setTargetType(objectType);

    var result = newJoin().joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .table(createTable("x2"))
        .relatedTable(createTable("x1"))
        .build();

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("(\n" + "  \"x2\".\"a_identifier\" = \"x1\".\"b_identifier\"\n"
        + "  and \"x2\".\"c_identifier\" = \"x1\".\"d_identifier\"\n" + ")"));
  }

  @Test
  void build_throwsException_forNoRelation() {
    var objectField = new PostgresObjectField();
    objectField.setName("identifier");

    objectField.setObjectType(createObjectType());
    objectField.setTargetType(createObjectType());

    var builder = newJoin().joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .table(createTable("x2"))
        .relatedTable(createTable("x1"));

    var exception = assertThrows(IllegalArgumentException.class, builder::build);

    assertThat(exception.getMessage(), equalTo("Object field 'identifier' has no relation configuration!"));
  }

  @Test
  void build_returnsListConditions_forJoinTable() {
    var parentObjectType = new PostgresObjectType();
    var parentFieldIdentifier = new PostgresObjectField();
    parentFieldIdentifier.setColumn("p_identifier");
    parentObjectType.setFields(Map.of("p_identifier_f", parentFieldIdentifier));

    var childObjectType = new PostgresObjectType();
    var childFieldIdentifier = new PostgresObjectField();
    childFieldIdentifier.setColumn("c_identifier");

    childObjectType.setFields(Map.of("c_identifier_f", childFieldIdentifier));

    var objectField = new PostgresObjectField();
    objectField.setObjectType(parentObjectType);
    objectField.setTargetType(childObjectType);

    var joinTable = new JoinTable();
    joinTable.setName("p_c");

    var joinColumn = new JoinColumn();
    joinColumn.setName("k_p_identifier");
    joinColumn.setReferencedField("p_identifier_f");
    joinTable.setJoinColumns(List.of(joinColumn));

    var inverseJoinColumn = new JoinColumn();
    inverseJoinColumn.setName("k_c_identifier");
    inverseJoinColumn.setReferencedField("c_identifier_f");
    joinTable.setInverseJoinColumns(List.of(inverseJoinColumn));

    objectField.setJoinTable(joinTable);

    var result = newJoin().joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .table(createTable("x2"))
        .relatedTable(createTable("x1"))
        .tableCreator(junctionTable -> DSL.table(junctionTable)
            .as("x3"))
        .build();

    assertThat(result, notNullValue());
    assertThat(result.toString(), equalTo("(\n" + "  \"x3\".\"k_p_identifier\" = \"x2\".\"p_identifier\"\n"
        + "  and \"x3\".\"k_c_identifier\" = \"x1\".\"c_identifier\"\n" + ")"));
  }

  @Test
  void build_throwsException_forMissingFields() {
    JoinBuilder builder = newJoin();
    var exception = assertThrows(ConstraintViolationException.class, builder::build);

    assertThat(exception.getMessage(),
        startsWith("class org.dotwebstack.framework.backend.postgres.query.JoinBuilder has validation errors (1):"));
  }

  private PostgresObjectType createObjectType() {
    var objectField = new PostgresObjectField();
    objectField.setColumn("d_identifier");

    var objectType = new PostgresObjectType();
    objectType.setFields(Map.of("otherIdentifier", objectField));

    return objectType;
  }

  private List<JoinColumn> createJoinColumns() {
    JoinColumn joinColumnRefColumn = new JoinColumn();
    joinColumnRefColumn.setName("a_identifier");
    joinColumnRefColumn.setReferencedColumn("b_identifier");

    JoinColumn joinColumnRefField = new JoinColumn();
    joinColumnRefField.setName("c_identifier");
    joinColumnRefField.setReferencedField("otherIdentifier");

    return List.of(joinColumnRefColumn, joinColumnRefField);
  }

  private Table<Record> createTable(String tableName) {
    return DSL.table(tableName);
  }
}
