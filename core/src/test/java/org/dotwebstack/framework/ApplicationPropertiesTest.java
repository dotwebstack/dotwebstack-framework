package org.dotwebstack.framework;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPropertiesTest {

  @Mock
  private ApplicationProperties applicationProperties;

  @Test
  public void applicationProperty_SetDefaultValueForResourcePath_WhenInstantiated()
      throws Exception {
    // Arrange
    applicationProperties = new ApplicationProperties();

    // Assert
    assertThat(applicationProperties.getResourcePath(), is("file:src/main/resources"));
  }

  @Test
  public void applicationProperty_ResourcePathIsSet_WithValue() throws Exception {
    // Arrange
    when(applicationProperties.getResourcePath()).thenReturn(DBEERPEDIA.RESOURCE_PATH);

    // Assert
    assertThat(applicationProperties.getResourcePath(), is(DBEERPEDIA.RESOURCE_PATH));
  }

  @Test
  public void applicationProperty_SystemGraphIsSet_WithValue() throws Exception {
    // Arrange
    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);

    // Assert
    assertThat(applicationProperties.getSystemGraph(), is(DBEERPEDIA.SYSTEM_GRAPH_IRI));
  }

}
