package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.flow.Flow;
import org.dotwebstack.framework.transaction.flow.FlowExecutor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

@RunWith(MockitoJUnitRunner.class)
public class TransactionRequestHandlerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Transaction transaction;

  @Mock
  private Flow flow;

  @Mock
  private FlowExecutor flowExecutor;

  @Mock
  private ContainerRequestContext containerRequestContext;

  @Mock
  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;

  @Mock
  private RepresentationRequestParameterMapper representationRequestParameterMapper;

  private TransactionRequestHandler transactionRequestHandler;

  @Before
  public void setUp() {
    transactionRequestHandler = new TransactionRequestHandler(transaction,
        supportedReaderMediaTypesScanner, representationRequestParameterMapper);
  }

  @Test
  public void apply_ExpectNotAcceptableResponse_WhenNoSupportedMediaTypes() {
    // Act
    Response response = transactionRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(HttpStatus.NOT_ACCEPTABLE.value()));
  }

}
