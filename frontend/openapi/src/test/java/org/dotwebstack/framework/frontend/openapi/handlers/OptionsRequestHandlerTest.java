package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import java.util.List;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OptionsRequestHandlerTest {

  @Mock
  private ContainerRequestContext requestContext;

  @Mock
  private Path path;

  private OptionsRequestHandler optionsRequestHandler;

  @Before
  public void setUp() {
    optionsRequestHandler = new OptionsRequestHandler(path);
  }

  @Test
  public void apply_SetsPathProperty_WhenCalled() {
    // Arrange
    when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, mock(Operation.class)));

    // Act
    optionsRequestHandler.apply(requestContext);

    // Assert
    verify(requestContext).setProperty("path", path);
  }

  @Test
  public void apply_IncludesHeadAndOptionsMethods_WhenNotSpecified() {
    // Arrange
    when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, mock(Operation.class),
        HttpMethod.POST, mock(Operation.class)));

    // Act
    Response response = optionsRequestHandler.apply(requestContext);

    // Assert
    assertThat(response.getHeaders().containsKey(HttpHeaders.ALLOW), is(true));
    List<String> allowMethods =
        Splitter.on(",").splitToList(response.getHeaders().getFirst(HttpHeaders.ALLOW).toString());
    assertThat(allowMethods, containsInAnyOrder(HttpMethod.GET.toString(),
        HttpMethod.POST.toString(), HttpMethod.HEAD.toString(), HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void apply_DoesNotIncludeHeadMethod_WhenAlreadySpecified() {
    // Arrange
    when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, mock(Operation.class),
        HttpMethod.HEAD, mock(Operation.class)));

    // Act
    Response response = optionsRequestHandler.apply(requestContext);

    // Assert
    assertThat(response.getHeaders().containsKey(HttpHeaders.ALLOW), is(true));
    List<String> allowMethods =
        Splitter.on(",").splitToList(response.getHeaders().getFirst(HttpHeaders.ALLOW).toString());
    assertThat(allowMethods, containsInAnyOrder(HttpMethod.GET.toString(),
        HttpMethod.HEAD.toString(), HttpMethod.OPTIONS.toString()));
  }

  @Test
  public void apply_DoesNotIncludeOptionsMethod_WhenAlreadySpecified() {
    // Arrange
    when(path.getOperationMap()).thenReturn(ImmutableMap.of(HttpMethod.GET, mock(Operation.class),
        HttpMethod.OPTIONS, mock(Operation.class)));

    // Act
    Response response = optionsRequestHandler.apply(requestContext);

    // Assert
    assertThat(response.getHeaders().containsKey(HttpHeaders.ALLOW), is(true));
    List<String> allowMethods =
        Splitter.on(",").splitToList(response.getHeaders().getFirst(HttpHeaders.ALLOW).toString());
    assertThat(allowMethods, containsInAnyOrder(HttpMethod.GET.toString(),
        HttpMethod.HEAD.toString(), HttpMethod.OPTIONS.toString()));
  }

}
