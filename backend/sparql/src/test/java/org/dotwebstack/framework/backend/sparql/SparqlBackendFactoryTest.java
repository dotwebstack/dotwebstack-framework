package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
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

  @Mock
  IRI identifier;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private SparqlBackendFactory backendFactory;

  @Before
  public void setUp() {
    backendFactory = new SparqlBackendFactory();
  }

  @Test
  public void backendIsCreated() {
    // Arrange
    Model backendModel =
        new ModelBuilder().add(identifier, ELMO.ENDPOINT, DBEERPEDIA.ENDPOINT).build();

    // Act
    SparqlBackend backend = (SparqlBackend) backendFactory.create(backendModel, identifier);

    // Assert
    assertThat(backend.getIdentifier(), equalTo(identifier));
    assertThat(backend.getEndpoint(), equalTo(DBEERPEDIA.ENDPOINT.stringValue()));
  }

  @Test
  public void endpointIsMissing() {
    // Arrange
    Model backendModel = new ModelBuilder().build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for backend <%s>.",
        ELMO.ENDPOINT, identifier));

    // Act
    backendFactory.create(backendModel, identifier);
  }

  @Test
  public void endpointHasInvalidDataType() {
    // Arrange
    Literal endpointAsString =
        SimpleValueFactory.getInstance().createLiteral(DBEERPEDIA.ENDPOINT.stringValue());
    Model backendModel =
        new ModelBuilder().add(identifier, ELMO.ENDPOINT, endpointAsString).build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Object <%s> for backend <%s> must be of datatype <%s>.",
        ELMO.ENDPOINT, identifier, XMLSchema.ANYURI));

    // Act
    backendFactory.create(backendModel, identifier);
  }

  @Test
  public void sparqlBackendIsSupported() {
    // Act
    boolean isSupported = backendFactory.supports(ELMO.SPARQL_BACKEND);

    // Assert
    assertThat(isSupported, equalTo(true));
  }

}
