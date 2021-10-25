package org.dotwebstack.framework.backend.postgres.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

public class JoinTableTest {

  @Test
  void setProperties() {
    JoinTable table = new JoinTable();
    table.setName("aaa");
    JoinColumn joinColumn = mock(JoinColumn.class);
    List<JoinColumn> list = List.of(joinColumn, joinColumn);
    table.setJoinColumns(list);
    table.setInverseJoinColumns(list);

    assertThat(table.getName(), CoreMatchers.is("aaa"));
    assertThat(table.getJoinColumns(), CoreMatchers.is(list));
    assertThat(table.getInverseJoinColumns(), CoreMatchers.is(list));
  }
}
