package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Operation;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.TupleQueryResult;
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
  private Operation operationMock;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private ContainerRequestContext containerRequestContext;

  private GetRequestHandler getRequestHandler;

  @Before
  public void setUp() {
    getRequestHandler = new GetRequestHandler(operationMock, informationProduct, ImmutableMap.of());
  }

  @Test
  public void constructor_ThrowsException_WithMissingInformationProduct() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(operationMock, null, ImmutableMap.of());
  }

  @Test
  public void constructor_ThrowsException_WithMissingSchemaMap() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(operationMock, informationProduct, null);
  }

  @Test
  public void apply_ThrowsException_WithMissingContainerRequestContext() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    getRequestHandler.apply(null);
  }

  @Test
  public void apply_ReturnsOkResponseWithEntityObject_ForTupleResult() {
    // Arrange
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    TupleQueryResult result = mock(TupleQueryResult.class);
    Map<String, Property> schemaMap = ImmutableMap.of();
    when(informationProduct.getResult(null)).thenReturn(result);
    when(informationProduct.getResultType()).thenReturn(ResultType.TUPLE);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    assertThat(response.getEntity(), instanceOf(TupleEntity.class));
    assertThat(((TupleEntity) response.getEntity()).getResult(), equalTo(result));
    assertThat(((TupleEntity) response.getEntity()).getSchemaMap(), equalTo(schemaMap));
  }

  @Test
  public void apply_ReturnsServerErrorResponseWithoutEntityObject_ForGraphResult() {
    // Arrange
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(informationProduct.getResultType()).thenReturn(ResultType.GRAPH);

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    assertThat(response.getEntity(), nullValue());
  }

}
