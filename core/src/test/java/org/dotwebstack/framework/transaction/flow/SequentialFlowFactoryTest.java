package org.dotwebstack.framework.transaction.flow;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.flow.step.Step;
import org.dotwebstack.framework.transaction.flow.step.StepResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

public class SequentialFlowFactoryTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Mock
  private StepResourceProvider stepResourceProvider;

  @Mock
  private Step step;

  private FlowFactory flowFactory;

  @Before
  public void setUp() {
    flowFactory = new SequentialFlowFactory(stepResourceProvider);
    when(stepResourceProvider.get(any())).thenReturn(step);
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

  @Test
  public void create_createsTermParameterDefinition_ForStringShape() {
    // Arrange
    ModelBuilder builder = new ModelBuilder();
    BNode blankNode = VALUE_FACTORY.createBNode();

    builder.subject(blankNode).add(ELMO.SEQUENTIAL_FLOW_PROP, DBEERPEDIA.PERSISTENCE_STEP);

    Model model = builder.build();

    // Act
    Flow result = flowFactory.create(model, blankNode);

    // Assert
    assertThat(result, instanceOf(SequentialFlowFactory.class));
  }

}
