package org.dotwebstack.framework.backend.sparql;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.dotwebstack.framework.backend.BackendSource;
import org.dotwebstack.framework.backend.QueryType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
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
public class SparqlBackendSourceFactoryTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private QueryEvaluator queryEvaluator;

  @Mock
  private SparqlBackend backend;

  private SparqlBackendSourceFactory sourceFactory;

  @Before
  public void setUp() {
    sourceFactory = new SparqlBackendSourceFactory(queryEvaluator);
    when(backend.getIdentifier()).thenReturn(DBEERPEDIA.BACKEND);
  }

  @Test
  public void sourceIsCreated() {
    // Arrange
    Model statements =
        new ModelBuilder().add(DBEERPEDIA.BACKEND, ELMO.QUERY, DBEERPEDIA.SELECT_ALL_QUERY).build();

    // Act
    BackendSource backendSource = sourceFactory.create(backend, statements);

    // Assert
    assertThat(backendSource.getBackend(), equalTo(backend));
    assertThat(backendSource, instanceOf(SparqlBackendSource.class));
    assertThat(((SparqlBackendSource) backendSource).getQuery(),
        equalTo(DBEERPEDIA.SELECT_ALL_QUERY.stringValue()));
  }

  @Test
  public void queryIsMissing() {
    // Arrange
    Model statements = new ModelBuilder().build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for backend source <%s>.",
        ELMO.QUERY, DBEERPEDIA.BACKEND));

    // Act
    sourceFactory.create(backend, statements);
  }

  @Test
  public void determineCorrectQueryTypeForSelect() {
    // Arrange
    Model statements =
        new ModelBuilder().add(DBEERPEDIA.BACKEND, ELMO.QUERY, DBEERPEDIA.SELECT_ALL_QUERY).build();

    // Act
    BackendSource backendSource = sourceFactory.create(backend, statements);

    // Assert
    assertThat(backendSource.getQueryType(), equalTo(QueryType.TUPLE));
  }

  @Test
  public void determineCorrectQueryTypeForGraph() {
    // Arrange
    Model statements = new ModelBuilder().add(DBEERPEDIA.BACKEND, ELMO.QUERY,
        DBEERPEDIA.CONSTRUCT_ALL_QUERY).build();

    // Act
    BackendSource backendSource = sourceFactory.create(backend, statements);

    // Assert
    assertThat(backendSource.getQueryType(), equalTo(QueryType.GRAPH));
  }

  @Test
  public void queryIsNotSupported() {
    // Arrange
    Model statements =
        new ModelBuilder().add(DBEERPEDIA.BACKEND, ELMO.QUERY, DBEERPEDIA.ASK_ALL_QUERY).build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Type of query <ASK WHERE { ?s ?p ?o }> could not be determined. "
        + "Only SELECT and CONSTRUCT are supported.");

    // Act
    sourceFactory.create(backend, statements);
  }

  @Test
  public void queryIsMalformed() {
    // Arrange
    Model statements =
        new ModelBuilder().add(DBEERPEDIA.BACKEND, ELMO.QUERY, DBEERPEDIA.MALFORMED_QUERY).build();

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format(
        "Type of query <%s> could not be determined. Query is a malformed query and cannot be processed: "
            + "Encountered \" <VAR1> \"?s \"\" at line 1, column 11.",
        DBEERPEDIA.MALFORMED_QUERY.stringValue()));

    // Act
    sourceFactory.create(backend, statements);
  }
}
