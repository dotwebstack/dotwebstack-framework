package org.dotwebstack.framework.backend.postgres.model;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostgresObjectTypeTest {
  
  private PostgresObjectType postgresObjectType = new PostgresObjectType();
  
  @Test
  void isNested_returnsTrue() {
    assertTrue(postgresObjectType.isNested());
  }
  
  @Test
  void isNested_returnsFalse() {
    postgresObjectType.setTable("@@@");
    
    assertFalse(postgresObjectType.isNested());
    assertThat(postgresObjectType.getTable(), CoreMatchers.is("@@@"));
    assertEquals(postgresObjectType, postgresObjectType);
  }
}
