package org.dotwebstack.framework.backend.postgres.model;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class JoinColumnTest {
  
  @Test
  void setProperties() {
    JoinColumn joinColumn = new JoinColumn();
    joinColumn.setName("aaa");
    
    assertThat(joinColumn.getName(), CoreMatchers.is("aaa"));
  }
}
