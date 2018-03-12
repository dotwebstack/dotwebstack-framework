package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.flow.Flow;
import org.dotwebstack.framework.transaction.flow.FlowExecutor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

  private TransactionRequestHandler transactionRequestHandler;

  @Before
  public void setUp() {
    transactionRequestHandler = new TransactionRequestHandler(transaction);
    when(transaction.getFlow()).thenReturn(flow);
    when(flow.getExecutor(any())).thenReturn(flowExecutor);
  }

  @Test
  public void apply_ReturnOkResponse_WithValidData() {
    // Act
    Model transactionModel = new LinkedHashModel();
    Response response = transactionRequestHandler.apply(transactionModel);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    verify(flowExecutor, times(1)).execute();
  }

}
