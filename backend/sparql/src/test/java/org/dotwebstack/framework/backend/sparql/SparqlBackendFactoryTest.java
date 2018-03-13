package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.backend.sparql.informationproduct.SparqlBackendInformationProductFactory;
import org.dotwebstack.framework.backend.sparql.persistencestep.SparqlBackendPersistenceStepFactory;
import org.dotwebstack.framework.backend.sparql.updatestep.SparqlBackendUpdateStepFactory;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendFactoryTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private SparqlBackendInformationProductFactory informationProductFactory;

  @Mock
  private SparqlBackendPersistenceStepFactory persistenceStepFactory;

  @Mock
  private SparqlBackendUpdateStepFactory updateStepFactory;

  private SparqlBackendFactory backendFactory;

  @Before
  public void setUp() {
    backendFactory = new SparqlBackendFactory(informationProductFactory, persistenceStepFactory,
        updateStepFactory);
  }

  @Test
  public void constructor_ThrowsException_WithMissingInformationProductFactory() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SparqlBackendFactory(null, persistenceStepFactory, updateStepFactory);
  }

  @Test
  public void create_BackendIsCreated_WithValidData() {
    // Arrange
    Model backendModel =
        new ModelBuilder().add(DBEERPEDIA.BACKEND, ELMO.ENDPOINT_PROP, DBEERPEDIA.ENDPOINT).build();

    // Act
    SparqlBackend backend = (SparqlBackend) backendFactory.create(backendModel, DBEERPEDIA.BACKEND);

    // Assert
    assertThat(backend.getIdentifier(), equalTo(DBEERPEDIA.BACKEND));
    assertThat(backend.getRepository(), notNullValue());
    assertThat(backend.getRepository().isInitialized(), equalTo(true));
  }

  @Test
  public void create_ThrowsException_WithMissingBackend() {
    // Arrange
    Model backendModel = new ModelBuilder().build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for backend <%s>.",
        ELMO.ENDPOINT_PROP, DBEERPEDIA.BACKEND));

    // Act
    backendFactory.create(backendModel, DBEERPEDIA.BACKEND);
  }

  @Test
  public void create_ThrowsException_WithInvalidDataType() {
    // Arrange
    Literal endpointAsString =
        SimpleValueFactory.getInstance().createLiteral(DBEERPEDIA.ENDPOINT.stringValue());
    Model backendModel =
        new ModelBuilder().add(DBEERPEDIA.BACKEND, ELMO.ENDPOINT_PROP, endpointAsString).build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Object <%s> for backend <%s> must be of datatype <%s>.",
        ELMO.ENDPOINT_PROP, DBEERPEDIA.BACKEND, XMLSchema.ANYURI));

    // Act
    backendFactory.create(backendModel, DBEERPEDIA.BACKEND);
  }

  @Test
  public void supports_SparqlBackendSupported_ForELMOSparqlBackend() {
    // Act
    boolean isSupported = backendFactory.supports(ELMO.SPARQL_BACKEND);

    // Assert
    assertThat(isSupported, equalTo(true));
  }

}
