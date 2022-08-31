package org.dotwebstack.framework.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class DummyTest {

  @Test
  void foo_returnsTrue_forDefault() {
    assertThat(Dummy.foo(), equalTo(true));
  }
}
