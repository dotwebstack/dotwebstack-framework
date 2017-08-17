package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SparqlBackendCreateSourceTest {

  @Mock
  IRI identifier;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private SparqlBackend backend;

  @Before
  public void setUp() {
    backend = new SparqlBackend.Builder(identifier, "endpoint").build();
  }

  @Test
  public void sourceIsCreated() {
    // Arrange
    Model backendSourceModel =
        new ModelBuilder().add(identifier, ELMO.QUERY, "myQuery").build();

    // Act
    BackendSource backendSource = backend.createSource(backendSourceModel);

    // Assert
    assertThat(backendSource.getBackend(), equalTo(backend));
    assertThat(backendSource, instanceOf(SparqlBackendSource.class));
    assertThat(((SparqlBackendSource)backendSource).getQuery(), equalTo("myQuery"));
  }

  @Test
  public void queryIsMissing() {
    // Arrange
    Model backendSourceModel =
        new ModelBuilder().build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> query has been found for backend source <%s>.",
        ELMO.QUERY, identifier));

    // Act
    backend.createSource(backendSourceModel);
  }
}
