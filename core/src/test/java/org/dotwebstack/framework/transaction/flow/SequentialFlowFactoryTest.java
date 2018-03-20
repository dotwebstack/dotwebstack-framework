package org.dotwebstack.framework.transaction.flow;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SequentialFlowFactoryTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private SequentialFlowResourceProvider sequentialFlowResourceProvider;

  @Mock
  private Step step;

  private FlowFactory flowFactory;

  @Before
  public void setUp() {
    flowFactory = new SequentialFlowFactory(sequentialFlowResourceProvider);
  }

  @Test
  public void supports_ReturnsTrue_ForSupportedIri() {
    // Act
    boolean result = flowFactory.supports(ELMO.SEQUENTIAL_FLOW_PROP);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForUnsupportedIri() {
    // Arrange
    IRI unsupported = VALUE_FACTORY.createIRI("http://unsupported#", "Flow");

    // Act
    boolean result = flowFactory.supports(unsupported);

    // Assert
    assertThat(result, is(false));
  }

}
