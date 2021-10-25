package org.dotwebstack.framework.backend.postgres.model;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

public class JoinColumnTest {

  @Test
  void setProperties() {
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName("aaa");

    assertThat(joinColumn.getName(), CoreMatchers.is("aaa"));
  }
}
