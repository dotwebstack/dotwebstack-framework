package org.dotwebstack.framework;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.FileConfigurationBackend;
import org.dotwebstack.framework.validation.ShaclValidator;
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

  @Mock
  private Resource elmoShapesResource;

  @Mock
  private ShaclValidator shaclValidator;

  private ApplicationConfiguration applicationConfiguration;

  @Before
  public void setUp() {
    applicationConfiguration = new ApplicationConfiguration();
  }

  @Test
  public void configurationBackend_ReturnsBackend_WhenInstantiated() {
    // Act
    ConfigurationBackend backend =
        applicationConfiguration
            .configurationBackend(resource, "file:src/main/resources",
                elmoShapesResource, shaclValidator);
    // Assert
    assertThat(backend, instanceOf(FileConfigurationBackend.class));
  }

}
