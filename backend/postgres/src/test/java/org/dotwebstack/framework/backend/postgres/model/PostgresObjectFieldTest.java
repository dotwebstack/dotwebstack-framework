package org.dotwebstack.framework.backend.postgres.model;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class PostgresObjectFieldTest {
  
  private PostgresObjectField objectField = new PostgresObjectField();
  
  @Test
  void getColumn_returnsColumn() {
    objectField.setColumn("bbb");
    
    var result = objectField.getColumn();
    
    assertThat(result, CoreMatchers.is("bbb"));
  }
  
  @Test
  void getColumn_returnsColumn_forName() {
    objectField.setName("aaa");
    
    var result = objectField.getColumn();
    assertThat(result, CoreMatchers.is("aaa"));
  }
  
  @Test
  void setProperties() {
    PostgresObjectField mappedByObjectField = mock(PostgresObjectField.class);
    objectField.setMappedByObjectField(mappedByObjectField);
    JoinColumn joinColumn = mock(JoinColumn.class);
    List<JoinColumn> list = List.of(joinColumn, joinColumn);
    objectField.setJoinColumns(List.of(joinColumn, joinColumn));
    JoinTable joinTable = mock(JoinTable.class);
    objectField.setJoinTable(joinTable);
    objectField.setMappedBy("fff");
    PostgresObjectType objectType = mock(PostgresObjectType.class);
    objectField.setTargetType(objectType);
    
    assertThat(objectField.getMappedByObjectField(), CoreMatchers.is(mappedByObjectField));
    assertThat(objectField.getJoinColumns(), CoreMatchers.is(list));
    assertThat(objectField.getJoinTable(), CoreMatchers.is(joinTable));
    assertThat(objectField.getMappedBy(), CoreMatchers.is("fff"));
    assertThat(objectField.getTargetType(), CoreMatchers.is(objectType));
  }
  
}
