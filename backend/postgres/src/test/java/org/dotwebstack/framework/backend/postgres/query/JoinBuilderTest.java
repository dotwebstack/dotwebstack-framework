package org.dotwebstack.framework.backend.postgres.query;

import static org.dotwebstack.framework.backend.postgres.query.JoinBuilder.newJoin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.dotwebstack.framework.backend.postgres.model.JoinColumn;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectField;
import org.dotwebstack.framework.backend.postgres.model.PostgresObjectType;
import org.dotwebstack.framework.core.model.ObjectType;
import org.hamcrest.CoreMatchers;
import org.jooq.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class JoinBuilderTest {

  private JoinBuilder joinBuilder;

  @BeforeEach
  void setUp() {
    joinBuilder = newJoin();
  }

  @Test
  void build_returnsListCondition_forMappedByObjectField() {
    JoinColumn joinColumn = mock(JoinColumn.class);
    when(joinColumn.getName()).thenReturn("a");
    when(joinColumn.getReferencedColumn()).thenReturn("a");

    var mappedByObjectField = mock(PostgresObjectField.class);
    lenient().when(mappedByObjectField.getJoinColumns())
        .thenReturn(List.of(joinColumn));

    var objectField = mock(PostgresObjectField.class);
    lenient().when(objectField.getMappedByObjectField())
        .thenReturn(mappedByObjectField);
    ObjectType postgresObjectType = mock(PostgresObjectType.class);
    lenient().when(objectField.getObjectType())
        .thenReturn(postgresObjectType);
    lenient().when(objectField.getJoinColumns())
        .thenReturn(List.of(joinColumn));

    joinBuilder.current(objectField);

    var table = mock(Table.class);
    joinBuilder.table(table);
    var relatedTable = mock(Table.class);
    joinBuilder.relatedTable(relatedTable);

    var result = joinBuilder.build();
    assertThat(result.get(0)
        .toString(), CoreMatchers.is("\"a\" = \"a\""));
  }

  @Test
  void build_returnsListCondition_forNormalJoinColumt() {
    JoinColumn joinColumn = mock(JoinColumn.class);
    when(joinColumn.getName()).thenReturn("a");
    when(joinColumn.getReferencedColumn()).thenReturn("a");

    var objectField = mock(PostgresObjectField.class);
    ObjectType postgresObjectType = mock(PostgresObjectType.class);
    lenient().when(objectField.getTargetType())
        .thenReturn(postgresObjectType);
    lenient().when(objectField.getJoinColumns())
        .thenReturn(List.of(joinColumn));

    joinBuilder.current(objectField);

    var table = mock(Table.class);
    joinBuilder.table(table);
    var relatedTable = mock(Table.class);
    joinBuilder.relatedTable(relatedTable);

    var result = joinBuilder.build();
    assertThat(result.get(0)
        .toString(), CoreMatchers.is("\"a\" = \"a\""));
  }
}
