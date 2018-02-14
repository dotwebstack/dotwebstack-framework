package org.dotwebstack.framework.frontend.http.layout;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
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
public class LayoutResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private LayoutResourceProvider layoutResourceProvider;

  @Before
  public void setUp() {
    layoutResourceProvider =
        new LayoutResourceProvider(configurationBackend, applicationProperties);

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
    new LayoutResourceProvider(null, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new LayoutResourceProvider(configurationBackend, null);
  }

  @Test
  public void loadResources_LoadsLayout_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.LAYOUT, RDF.TYPE, ELMO.LAYOUT,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.LAYOUT, ELMO.LAYOUT, DBEERPEDIA.LAYOUT_VALUE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Act
    layoutResourceProvider.loadResources();

    // Assert
    assertThat(layoutResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(layoutResourceProvider.get(DBEERPEDIA.LAYOUT), is(not(nullValue())));
  }

  @Test
  public void loadResources_LoadsLayout_WithValidDataAndBNode() {
    // Arrange
    final BNode blankNode = valueFactory.createBNode();
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(blankNode, RDF.TYPE, ELMO.LAYOUT,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(blankNode, ELMO.LAYOUT, DBEERPEDIA.LAYOUT_VALUE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Act
    layoutResourceProvider.loadResources();

    // Assert
    assertThat(layoutResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(layoutResourceProvider.get(blankNode), is(not(nullValue())));
  }

  @Test
  public void loadResources_LoadsSeveralLayout_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.LAYOUT, RDF.TYPE, ELMO.LAYOUT),
            valueFactory.createStatement(DBEERPEDIA.LAYOUT, ELMO.LAYOUT, DBEERPEDIA.LAYOUT_VALUE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.LAYOUT_NL, RDF.TYPE, ELMO.LAYOUT,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.LAYOUT_NL, ELMO.LAYOUT, DBEERPEDIA.LAYOUT_VALUE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Act
    layoutResourceProvider.loadResources();

    // Assert
    assertThat(layoutResourceProvider.getAll().entrySet(), hasSize(2));
  }

}
