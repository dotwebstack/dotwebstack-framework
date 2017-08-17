package org.dotwebstack.framework;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.informationproduct.InformationProductLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private BackendLoader backendLoader;

  @Mock
  private InformationProductLoader informationProductLoader;

  private Application application;

  @Before
  public void setUp() {
    application = new Application(configurationBackend, backendLoader, informationProductLoader);
  }

  @Test
  public void loaderMethodsCalled() throws IOException {
    // Act
    application.load();

    // Assert
    verify(configurationBackend).initialize();
    verify(backendLoader).load();
    verify(informationProductLoader).load();
  }

}
