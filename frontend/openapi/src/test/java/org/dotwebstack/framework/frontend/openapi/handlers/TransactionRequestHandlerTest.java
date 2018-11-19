package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionHandler;
import org.dotwebstack.framework.transaction.TransactionHandlerFactory;
import org.dotwebstack.framework.transaction.flow.step.StepFailureException;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionRequestHandlerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private ApiOperation apiOperationMock;

  @Mock
  private Transaction transactionMock;

  @Mock
  private ContainerRequest containerRequestMock;

  @Mock
  private ApiRequestValidator apiRequestValidatorMock;

  @Mock
  private TransactionRequestParameterMapper requestParameterMapperMock;

  @Mock
  private TransactionRequestBodyMapper transactionRequestBodyMapper;

  @Mock
  private TransactionHandlerFactory transactionHandlerFactory;

  @Mock
  private TransactionHandler transactionHandler;

  @Mock
  private ExtendedUriInfo uriInfo;

  private TransactionRequestHandler transactionRequestHandler;

  @Before
  public void setUp() {
    transactionRequestHandler = new TransactionRequestHandler(apiOperationMock,
        transactionMock, requestParameterMapperMock, transactionRequestBodyMapper,
        apiRequestValidatorMock, transactionHandlerFactory);

    RequestParameters requestParameters = new RequestParameters();
    when(apiRequestValidatorMock.validate(apiOperationMock, containerRequestMock)) //
        .thenReturn(requestParameters);

    Operation operation = new Operation();
    when(apiOperationMock.getOperation()).thenReturn(operation);

    when(requestParameterMapperMock.map(same(operation), eq(transactionMock),
        same(requestParameters))).thenReturn(ImmutableMap.of());
    when(transactionRequestBodyMapper.map(any(), any())).thenReturn(new LinkedHashModel());
    when(transactionHandlerFactory.newTransactionHandler(any(), any())) //
        .thenReturn(transactionHandler);

    when(containerRequestMock.getUriInfo()).thenReturn(uriInfo);
  }

  @Test
  public void apply_ReturnsOkResponse_WithValidData() {
    // Arrange
    when(uriInfo.getPath()).thenReturn("/");

    // Act
    Response response = transactionRequestHandler.apply(containerRequestMock);

    // Assert
    assertThat(response.getStatus(), equalTo(Status.OK.getStatusCode()));
    verify(containerRequestMock).setProperty("operation", apiOperationMock.getOperation());
    verify(transactionHandler, times(1)).execute(any());
  }

  @Test
  public void apply_ThrowBadRequestException_WhenTransactionStepFails() {
    // Arrange
    Operation operation = new Operation() //
        .responses(new ApiResponses() //
            .addApiResponse(Status.OK.toString(), new ApiResponse()));
    when(apiOperationMock.getOperation()).thenReturn(operation);
    Mockito.doThrow(new StepFailureException(null)).when(transactionHandler).execute(any());

    // Assert
    thrown.expect(BadRequestException.class);

    // Act
    transactionRequestHandler.apply(containerRequestMock);
  }

}
