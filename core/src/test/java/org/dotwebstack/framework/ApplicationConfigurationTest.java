package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.junit.Before;
import org.junit.Test;

public class ApplicationConfigurationTest {

  ApplicationConfiguration applicationConfiguration;

  @Before
  public void setUp() {
    applicationConfiguration = new ApplicationConfiguration();
  }

  @Test
  public void getConfigurationBackend() {
    // Act
    ConfigurationBackend backend = applicationConfiguration.configurationBackend();

    // Assert
    assertThat(backend, instanceOf(FileConfigurationBackend.class));
  }

}
