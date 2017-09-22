package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.frontend.openapi.entity.Entity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetRequestHandlerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private ContainerRequestContext containerRequestContext;

  private GetRequestHandler getRequestHandler;

  @Before
  public void setUp() {
    getRequestHandler = new GetRequestHandler(informationProduct, ImmutableMap.of());
  }

  @Test
  public void constructor_ThrowsException_WithMissingInformationProduct() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(null, ImmutableMap.of());
  }

  @Test
  public void constructor_ThrowsException_WithMissingSchemaMap() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(informationProduct, null);
  }

  @Test
  public void apply_ReturnsResponseWithEntityObject_ForValidData() {
    // Arrange
    Object result = new Object();
    Map<String, Property> schemaMap = ImmutableMap.of();
    when(informationProduct.getResult()).thenReturn(result);
    UriInfo uriInfo = mock(UriInfo.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/");

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(Entity.class));
    assertThat(((Entity) response.getEntity()).getProperties(), equalTo(result));
    assertThat(((Entity) response.getEntity()).getSchemaMap(), equalTo(schemaMap));
  }

}
