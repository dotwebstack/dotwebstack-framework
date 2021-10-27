package org.dotwebstack.framework.backend.postgres.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolationException;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

class JoinBuilderTest {

  @Test
  void build_returnsJoinConditions_forInvertedMappedBy() {
    var mappedByObjectField = new PostgresObjectField();
    mappedByObjectField.setJoinColumns(createJoinColumns());

    var objectField = new PostgresObjectField();
    objectField.setMappedByObjectField(mappedByObjectField);

    var objectType = createObjectType();
    objectField.setObjectType(objectType);

    var result = JoinBuilder.newJoin()
        .current(objectField)
        .table(createTable("x1"))
        .relatedTable(createTable("x2"))
        .build();

    assertThat(result.get(0)
        .toString(), is("\"x1\".\"b_identifier\" = \"x2\".\"a_identifier\""));
    assertThat(result.get(1)
        .toString(), is("\"x1\".\"d_identifier\" = \"x2\".\"c_identifier\""));
  }

  @Test
  void build_returnsListConditions_forNormalJoinColumn() {
    var joinColumnsObjectField = new PostgresObjectField();
    joinColumnsObjectField.setJoinColumns(createJoinColumns());

    var objectType = createObjectType();
    joinColumnsObjectField.setTargetType(objectType);

    var result = JoinBuilder.newJoin()
        .current(joinColumnsObjectField)
        .table(createTable("x2"))
        .relatedTable(createTable("x1"))
        .build();

    assertThat(result.get(0)
        .toString(), is("\"x1\".\"b_identifier\" = \"x2\".\"a_identifier\""));
    assertThat(result.get(1)
        .toString(), is("\"x1\".\"d_identifier\" = \"x2\".\"c_identifier\""));
  }

  @Test
  void build_throwsException_forMissingFields() {
    JoinBuilder builder = JoinBuilder.newJoin();
    var exception = assertThrows(ConstraintViolationException.class, builder::build);

    assertThat(exception.getMessage(),
        startsWith("class org.dotwebstack.framework.backend.postgres.query.JoinBuilder has validation errors (2):"));
  }

  private PostgresObjectType createObjectType() {
    var postgresObjectField = new PostgresObjectField();
    postgresObjectField.setColumn("d_identifier");

    var postgresObjectType = new PostgresObjectType();
    postgresObjectType.setFields(Map.of("otherIdentifier", postgresObjectField));

    return postgresObjectType;
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
