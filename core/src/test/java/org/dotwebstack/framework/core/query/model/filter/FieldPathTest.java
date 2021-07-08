package org.dotwebstack.framework.core.query.model.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.core.config.AbstractFieldConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

class FieldPathTest {

  @Test
  void getName_returnsName_forLeaf() {
    var fieldConfiguration = mock(AbstractFieldConfiguration.class);

    when(fieldConfiguration.getName()).thenReturn("name");

    var fieldPath = FieldPath.builder()
        .fieldConfiguration(fieldConfiguration)
        .build();

    assertThat(fieldPath.getName(), CoreMatchers.equalTo("name"));
  }

  @Test
  void getName_returnsName_forNested() {
    var parentFieldConfiguration = mock(AbstractFieldConfiguration.class);
    when(parentFieldConfiguration.getName()).thenReturn("parent");

    var fieldConfiguration = mock(AbstractFieldConfiguration.class);

    when(fieldConfiguration.getName()).thenReturn("name");

    var fieldPath = FieldPath.builder()
        .fieldConfiguration(parentFieldConfiguration)
        .child(FieldPath.builder()
            .fieldConfiguration(fieldConfiguration)
            .build())
        .build();

    assertThat(fieldPath.getName(), CoreMatchers.equalTo("parent.name"));
  }

  @Test
  void getLeaf_returnsLeaf_forLeaf() {
    var fieldConfiguration = mock(AbstractFieldConfiguration.class);

    when(fieldConfiguration.getName()).thenReturn("name");

    var fieldPath = FieldPath.builder()
        .fieldConfiguration(fieldConfiguration)
        .build();

    assertThat(fieldPath.getLeaf(), CoreMatchers.equalTo(fieldPath));
  }

  @Test
  void getLeaf_returnsLeaf_forNested() {
    var parentFieldConfiguration = mock(AbstractFieldConfiguration.class);
    when(parentFieldConfiguration.getName()).thenReturn("parent");

    var fieldConfiguration = mock(AbstractFieldConfiguration.class);

    when(fieldConfiguration.getName()).thenReturn("name");

    var leaf = FieldPath.builder()
        .fieldConfiguration(fieldConfiguration)
        .build();

    var fieldPath = FieldPath.builder()
        .fieldConfiguration(parentFieldConfiguration)
        .child(leaf)
        .build();

    assertThat(fieldPath.getLeaf(), CoreMatchers.equalTo(leaf));
  }
}
