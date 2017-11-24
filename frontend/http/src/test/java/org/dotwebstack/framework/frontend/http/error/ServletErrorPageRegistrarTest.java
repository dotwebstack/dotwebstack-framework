package org.dotwebstack.framework.frontend.http.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.servlet.ErrorPageRegistry;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class ServletErrorPageRegistrarTest {

  @Mock
  private ErrorPageRegistry registry;

  private ServletErrorPageRegistrar servletErrorPageRegistrar;

  @Before
  public void setUp() {
    servletErrorPageRegistrar = new ServletErrorPageRegistrar();
  }

  @Test
  public void registerErrorPages_RegistersErrorPageForAllStatusCodes_WhenCalled() {
    // Act
    servletErrorPageRegistrar.registerErrorPages(registry);

    // Assert
    verify(registry, times(HttpStatus.values().length)).addErrorPages(any());
  }

}
