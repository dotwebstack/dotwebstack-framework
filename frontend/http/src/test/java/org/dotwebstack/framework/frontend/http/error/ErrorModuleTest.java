package org.dotwebstack.framework.frontend.http.error;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import javax.ws.rs.HttpMethod;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ErrorModuleTest {

  @Captor
  private ArgumentCaptor<Resource> resourceCaptor;

  @Mock
  private HttpConfiguration httpConfigurationMock;

  private ErrorModule errorModule;

  @Before
  public void setUp() {
    errorModule = new ErrorModule();
  }

  @Test
  public void initialize_RegistersErrorResource_WhenCalled() {
    // Act
    errorModule.initialize(httpConfigurationMock);

    // Assert
    verify(httpConfigurationMock).registerResources(resourceCaptor.capture());
    assertThat(resourceCaptor.getAllValues(), hasSize(1));

    Resource resource = resourceCaptor.getValue();
    assertThat(resource.getPath(), equalTo("/{domain}/__errors/{statusCode:\\d{3}}"));
    assertThat(resource.getResourceMethods(), hasSize(1));

    ResourceMethod method = resource.getResourceMethods().get(0);
    assertThat(method.getHttpMethod(), CoreMatchers.equalTo(HttpMethod.GET));

    Object handler = resource.getHandlerInstances().iterator().next();
    assertThat(handler, instanceOf(ServletErrorHandler.class));
  }

}
