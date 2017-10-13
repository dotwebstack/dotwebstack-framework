package org.dotwebstack.framework;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPropertiesTest {

  private ApplicationProperties applicationProperties;

  @Before
  public void setUp() {
    // Arrange
    applicationProperties = new ApplicationProperties();
  }

  @Test
  public void applicationProperty_SetDefaultValueForResourcePath_WhenInstantiated()
      throws Exception {

    // Assert
    assertThat(applicationProperties.getResourcePath(), is("file:src/main/resources"));
  }

  @Test
  public void applicationProperty_ResourcePathIsSet_WithValue() throws Exception {
    // Arrange
    applicationProperties.setResourcePath(DBEERPEDIA.RESOURCE_PATH);

    // Assert
    assertThat(applicationProperties.getResourcePath(), is(DBEERPEDIA.RESOURCE_PATH));
  }

  @Test
  public void applicationProperty_SystemGraphIsSet_WithValue() throws Exception {
    // Arrange
    applicationProperties.setSystemGraph(DBEERPEDIA.SYSTEM_GRAPH);

    // Assert
    assertThat(applicationProperties.getSystemGraph(), is(DBEERPEDIA.SYSTEM_GRAPH_IRI));
  }

}
