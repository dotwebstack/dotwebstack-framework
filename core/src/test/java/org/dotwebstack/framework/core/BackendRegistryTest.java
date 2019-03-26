package org.dotwebstack.framework.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BackendRegistryTest {

  private BackendRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new BackendRegistry();
  }

  @Test
  void register_addsBackendToRegistry() {
    // Arrange
    Backend backend = Mockito.mock(Backend.class);

    // Act
    registry.register("foo", backend);

    // Assert
    assertThat(registry.get("foo"), is(equalTo(registry.get("foo"))));
  }

  @Test
  void get_returnsNull_ForAbsentBackend() {
    // Act
    Backend backend = registry.get("foo");

    // Assert
    assertThat(backend, is(nullValue()));
  }

}
