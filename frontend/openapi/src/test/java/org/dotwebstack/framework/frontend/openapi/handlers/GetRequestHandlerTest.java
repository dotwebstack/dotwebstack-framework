package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
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
  private ApiOperation operationMock;

  @Mock
  private InformationProduct informationProductMock;

  @Mock
  private ContainerRequestContext containerRequestContextMock;

  @Mock
  private ApiRequestValidator apiRequestValidatorMock;

  @Mock
  private RequestParameterMapper requestParameterMapperMock;

  private GetRequestHandler getRequestHandler;

  @Mock
  private Swagger swaggerMock;

  @Before
  public void setUp() {
    getRequestHandler = new GetRequestHandler(operationMock, informationProductMock,
        ImmutableMap.of(), requestParameterMapperMock, apiRequestValidatorMock, swaggerMock);

    when(apiRequestValidatorMock.validate(operationMock, containerRequestContextMock)).thenReturn(
        new RequestParameters());
    when(operationMock.getOperation()).thenReturn(new Operation());

    // XXX (PvH) Je gebruikt hier any voor de Operation en RequestParameters. Maar verwacht je niet
    // de instanties die je hier boven hebt aangemaakt?
    when(requestParameterMapperMock.map(any(Operation.class), eq(informationProductMock),
        any(RequestParameters.class))).thenReturn(ImmutableMap.of());
  }

  @Test
  public void constructor_ThrowsException_WithMissingInformationProduct() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(operationMock, null, ImmutableMap.of(), requestParameterMapperMock,
        apiRequestValidatorMock, swaggerMock);
  }

  @Test
  public void constructor_ThrowsException_WithMissingSchemaMap() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(operationMock, informationProductMock, null, requestParameterMapperMock,
        apiRequestValidatorMock, swaggerMock);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApiOperation() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(null, informationProductMock, ImmutableMap.of(),
        requestParameterMapperMock, apiRequestValidatorMock, swaggerMock);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRequestParameterMapper() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(operationMock, informationProductMock, ImmutableMap.of(), null,
        apiRequestValidatorMock, swaggerMock);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApiRequestValidator() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(operationMock, informationProductMock, ImmutableMap.of(),
        requestParameterMapperMock, null, swaggerMock);
  }

  @Test
  public void constructor_ThrowsException_WithMissingSwagger() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new GetRequestHandler(operationMock, informationProductMock, ImmutableMap.of(),
        requestParameterMapperMock, apiRequestValidatorMock, null);
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
    when(containerRequestContextMock.getUriInfo()).thenReturn(uriInfo);
    TupleQueryResult result = mock(TupleQueryResult.class);
    final Map<String, Property> schemaMap = ImmutableMap.of();
    when(informationProductMock.getResult(ImmutableMap.of())).thenReturn(result);
    when(informationProductMock.getResultType()).thenReturn(ResultType.TUPLE);

    // Act
    Response response = getRequestHandler.apply(containerRequestContextMock);

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
    when(containerRequestContextMock.getUriInfo()).thenReturn(uriInfo);
    when(informationProductMock.getResultType()).thenReturn(ResultType.GRAPH);
    GraphQueryResult result = mock(GraphQueryResult.class);
    when(informationProductMock.getResult(ImmutableMap.of())).thenReturn(result);

    // Act
    Response response = getRequestHandler.apply(containerRequestContextMock);

    // Assert
    assertThat(response.getEntity(), instanceOf(GraphEntity.class));

  }

  @Test
  public void apply_ReturnsServerErrorResponseWithoutEntityObject_ForOtherResult() {
    // Arrange
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath()).thenReturn("/");
    when(containerRequestContextMock.getUriInfo()).thenReturn(uriInfo);

    // Act
    Response response = getRequestHandler.apply(containerRequestContextMock);

    // Assert
    assertThat(response.getStatus(),
        equalTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()));
    assertThat(response.getEntity(), nullValue());
  }
}
