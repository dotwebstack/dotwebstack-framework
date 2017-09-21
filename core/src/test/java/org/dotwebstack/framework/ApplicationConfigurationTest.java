package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationConfigurationTest {

  @Mock
  private Resource resource;

  private ApplicationConfiguration applicationConfiguration;

  @Before
  public void setUp() {
    applicationConfiguration = new ApplicationConfiguration();
  }

  @Test
  public void configurationBackend_ReturnsBackend_WhenInstantiated() {
    // Act
    ConfigurationBackend backend =
        applicationConfiguration.configurationBackend(resource, "file:.");

    // Assert
    assertThat(backend, instanceOf(FileConfigurationBackend.class));
  }

}
