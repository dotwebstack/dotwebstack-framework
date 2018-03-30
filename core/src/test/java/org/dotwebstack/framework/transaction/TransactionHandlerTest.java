package org.dotwebstack.framework.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.transaction.flow.Flow;
import org.dotwebstack.framework.transaction.flow.FlowExecutor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransactionHandlerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  Repository repository = new SailRepository(new MemoryStore());

  @Mock
  private Transaction transaction;

  @Mock
  private Flow flow;

  @Mock
  private FlowExecutor flowExecutor;

  private Model model = new LinkedHashModel();

  private Map<String, String> parameterValues = new HashMap<>();

  private TransactionHandler transactionHandler;

  @Before
  public void setUp() {
    transactionHandler = new TransactionHandler(repository, transaction, model);
  }

  @Test
  public void execute_Transaction_WithValidData() {
    // Arrange
    when(transaction.getFlow()).thenReturn(flow);
    when(flow.getExecutor(any())).thenReturn(flowExecutor);

    // Act
    transactionHandler.execute(parameterValues);

    // Assert
    verify(flowExecutor, times(1)).execute(any(), any());
  }

}
