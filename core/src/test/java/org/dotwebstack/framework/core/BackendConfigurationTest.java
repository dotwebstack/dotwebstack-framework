package org.dotwebstack.framework.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BackendConfigurationTest {

  private final BackendConfiguration configuration = new BackendConfiguration();

  @Test
  void backendRegistry_ReturnsRegistry_ForNoConfigurers() {
    // Act
    BackendRegistry backendRegistry = configuration.backendRegistry(ImmutableList.of());

    // Arrange
    assertThat(backendRegistry, is(notNullValue()));
  }

  @Test
  void backendRegistry_ReturnsRegistryWithBackends_ForGivenConfigurers() {
    // Arrange
    Backend backend = Mockito.mock(Backend.class);
    BackendConfigurer configurer = registry -> registry.register("foo", backend);

    // Act
    BackendRegistry backendRegistry = configuration.backendRegistry(ImmutableList.of(configurer));

    // Arrange
    assertThat(backendRegistry.get("foo"), is(equalTo(backend)));
  }

}
