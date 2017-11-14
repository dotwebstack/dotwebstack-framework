package org.dotwebstack.framework.frontend.ld.appearance;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppearanceResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Mock
  private ApplicationProperties applicationProperties;

  private AppearanceResourceProvider appearanceResourceProvider;

  @Before
  public void setUp() {
    appearanceResourceProvider =
        new AppearanceResourceProvider(configurationBackend, applicationProperties);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new AppearanceResourceProvider(null, applicationProperties);
  }


  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new AppearanceResourceProvider(configurationBackend, null);
  }

  @Test
  public void loadResources_LoadRepresentation_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.BREWERY_APPEARANCE, RDF.TYPE,
                ELMO.RESOURCE_APPEARANCE),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_APPEARANCE,
                DBEERPEDIA.CUSTOM_APPEARANCE_PROP, valueFactory.createLiteral(true)))));

    // Act
    appearanceResourceProvider.loadResources();

    // Assert
    assertThat(appearanceResourceProvider.getAll().entrySet(), hasSize(1));

    Appearance appearance = appearanceResourceProvider.get(DBEERPEDIA.BREWERY_APPEARANCE);
    assertThat(appearance, not(nullValue()));
    assertThat(appearance.getIdentifier(), equalTo(DBEERPEDIA.BREWERY_APPEARANCE));
    assertThat(appearance.getType(), equalTo(ELMO.RESOURCE_APPEARANCE));
  }

  @Test
  public void loadResources_ThrowsException_WithMissingTypeStatement() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.BREWERY_APPEARANCE,
            DBEERPEDIA.CUSTOM_APPEARANCE_PROP, valueFactory.createLiteral(true)))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for appearance <%s>.",
        RDF.TYPE, DBEERPEDIA.BREWERY_APPEARANCE));

    // Act
    appearanceResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_RepositoryConnectionError() {
    // Arrange
    when(configurationRepository.getConnection()).thenThrow(RepositoryException.class);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while getting repository connection.");

    // Act
    appearanceResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_QueryEvaluationError() {
    // Arrange
    when(graphQuery.evaluate()).thenThrow(QueryEvaluationException.class);

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage("Error while evaluating SPARQL query.");

    // Act
    appearanceResourceProvider.loadResources();
  }

}
