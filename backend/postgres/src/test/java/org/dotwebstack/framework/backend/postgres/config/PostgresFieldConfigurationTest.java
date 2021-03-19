package org.dotwebstack.framework.backend.postgres.config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PostgresFieldConfigurationTest {

  @Test
  void isScalar_returnsTrue_forScalar() {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();

    assertThat(fieldConfiguration.isScalar(), equalTo(true));
  }

  @Test
  void isScalar_returnsFalse_forJoinColumn() {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setJoinColumns(List.of(new JoinColumn()));

    assertThat(fieldConfiguration.isScalar(), equalTo(false));
  }

  @Test
  void isScalar_returnsFalse_forJoinTable() {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setJoinTable(new JoinTable());

    assertThat(fieldConfiguration.isScalar(), equalTo(false));
  }

  @Test
  void isScalar_returnsFalse_forMappedBy() {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setMappedBy("test");

    assertThat(fieldConfiguration.isScalar(), equalTo(false));
  }

  @Test
  void isScalar_returnsFalse_forAggregation() {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setAggregationOf("test");

    assertThat(fieldConfiguration.isScalar(), equalTo(false));
  }

  @Test
  void findJoinColumns_returnsList_forJoinColumns() {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();
    fieldConfiguration.setJoinColumns(List.of(new JoinColumn()));

    assertThat(fieldConfiguration.findJoinColumns(), equalTo(fieldConfiguration.getJoinColumns()));
  }

  @Test
  void findJoinColumns_returnsList_forJoinTable() {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();

    JoinTable joinTable = new JoinTable();
    joinTable.setJoinColumns(List.of(new JoinColumn()));

    fieldConfiguration.setJoinTable(joinTable);

    assertThat(fieldConfiguration.findJoinColumns(), equalTo(joinTable.getJoinColumns()));
  }

  @Test
  void findJoinColumns_returnsEmptyList_forJoinTable() {
    PostgresFieldConfiguration fieldConfiguration = new PostgresFieldConfiguration();;

    assertThat(fieldConfiguration.findJoinColumns(), equalTo(List.of()));
  }
}
