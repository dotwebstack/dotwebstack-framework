package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
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
  void build_returnsJoinConditions_forMappedBy() {
    var mappedByObjectField = new PostgresObjectField();
    mappedByObjectField.setJoinColumns(createJoinColumns());

    var objectType = createObjectType();
    mappedByObjectField.setTargetType(objectType);

    var objectField = new PostgresObjectField();
    objectField.setMappedByObjectField(mappedByObjectField);

    var result = JoinBuilder.newJoin()
        .joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .table(createTable("x1"))
        .relatedTable(createTable("x2"))
        .build();

    assertThat(result.get(0)
        .toString(), is("\"x2\".\"a_identifier\" = \"x1\".\"b_identifier\""));
    assertThat(result.get(1)
        .toString(), is("\"x2\".\"c_identifier\" = \"x1\".\"d_identifier\""));
  }

  @Test
  void build_returnsListConditions_forJoinColumn() {
    var objectField = new PostgresObjectField();
    objectField.setJoinColumns(createJoinColumns());

    var objectType = createObjectType();
    objectField.setTargetType(objectType);

    var result = JoinBuilder.newJoin()
        .joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .table(createTable("x2"))
        .relatedTable(createTable("x1"))
        .build();

    assertThat(result.get(0)
        .toString(), is("\"x2\".\"a_identifier\" = \"x1\".\"b_identifier\""));
    assertThat(result.get(1)
        .toString(), is("\"x2\".\"c_identifier\" = \"x1\".\"d_identifier\""));
  }

  @Test
  void build_throwsException_forNoRelation() {
    var objectField = new PostgresObjectField();
    objectField.setName("identifier");

    var objectType = createObjectType();
    objectField.setTargetType(objectType);

    var builder = JoinBuilder.newJoin()
        .joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
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

    var joinColumn = new JoinColumn();
    joinColumn.setName("k_p_identifier");
    joinColumn.setReferencedField("p_identifier_f");
    joinTable.setJoinColumns(List.of(joinColumn));

    var inverseJoinColumn = new JoinColumn();
    inverseJoinColumn.setName("k_c_identifier");
    inverseJoinColumn.setReferencedField("c_identifier_f");
    joinTable.setInverseJoinColumns(List.of(inverseJoinColumn));

    objectField.setJoinTable(joinTable);

    var result = JoinBuilder.newJoin()
        .joinConfiguration(JoinConfiguration.toJoinConfiguration(objectField))
        .table(createTable("x2"))
        .relatedTable(createTable("x1"))
        .tableCreator(junctionTable -> DSL.table(junctionTable)
            .as("x3"))
        .build();

    assertThat(result.stream()
        .map(Object::toString)
        .collect(Collectors.toList()),
        containsInAnyOrder("\"x3\".\"k_p_identifier\" = \"x2\".\"p_identifier\"",
            "\"x3\".\"k_c_identifier\" = \"x1\".\"c_identifier\""));
  }

  @Test
  void build_throwsException_forMissingFields() {
    JoinBuilder builder = JoinBuilder.newJoin();
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
