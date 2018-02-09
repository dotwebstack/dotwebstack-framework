package org.dotwebstack.framework.frontend.http.site;

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
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.layout.LayoutResourceProvider;
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
public class SiteResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private LayoutResourceProvider layoutResourceProvider;

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

  private SiteResourceProvider siteResourceProvider;

  @Before
  public void setUp() {
    siteResourceProvider = new SiteResourceProvider(configurationBackend, layoutResourceProvider,
        applicationProperties);

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
    new SiteResourceProvider(null, layoutResourceProvider, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SiteResourceProvider(configurationBackend, layoutResourceProvider, null);
  }

  @Test
  public void constructor_ThrowsException_WithMissingLayoutResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new SiteResourceProvider(configurationBackend, null, applicationProperties);
  }

  @Test
  public void loadResources_LoadsSite_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.SITE, ELMO.DOMAIN, DBEERPEDIA.DOMAIN,
                DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Act
    siteResourceProvider.loadResources();

    // Assert
    assertThat(siteResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(siteResourceProvider.get(DBEERPEDIA.SITE), is(not(nullValue())));
  }

  @Test
  public void loadResources_LoadsSite_WithValidDataAndBNode() {
    // Arrange
    final BNode blankNode = valueFactory.createBNode();
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(blankNode, RDF.TYPE, ELMO.SITE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(blankNode, ELMO.DOMAIN, DBEERPEDIA.DOMAIN,
                DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Act
    siteResourceProvider.loadResources();

    // Assert
    assertThat(siteResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(siteResourceProvider.get(blankNode), is(not(nullValue())));
  }

  @Test
  public void loadResources_LoadsSeveralSite_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE),
            valueFactory.createStatement(DBEERPEDIA.SITE, ELMO.DOMAIN, DBEERPEDIA.DOMAIN,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.SITE_NL, RDF.TYPE, ELMO.SITE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.SITE_NL, ELMO.DOMAIN, DBEERPEDIA.DOMAIN_NL,
                DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Act
    siteResourceProvider.loadResources();

    // Assert
    assertThat(siteResourceProvider.getAll().entrySet(), hasSize(2));
  }

  @Test
  public void loadResources_LoadCatchAllSite_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE,
            DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Act
    siteResourceProvider.loadResources();

    // Assert
    assertThat(siteResourceProvider.getAll().entrySet(), hasSize(1));
    assertThat(siteResourceProvider.get(DBEERPEDIA.SITE), is(not(nullValue())));
  }

  @Test
  public void loadResources_ThrowsException_WithSitesWithSimularDomain() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.SITE, ELMO.DOMAIN, DBEERPEDIA.DOMAIN,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.SITE_NL, RDF.TYPE, ELMO.SITE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.SITE_NL, ELMO.DOMAIN, DBEERPEDIA.DOMAIN,
                DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("Domain <%s> found for multiple sites.", DBEERPEDIA.DOMAIN.stringValue()));

    // Act
    siteResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_WithMultipleCatchAllDomains() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI),
            valueFactory.createStatement(DBEERPEDIA.SITE_NL, RDF.TYPE, ELMO.SITE,
                DBEERPEDIA.SYSTEM_GRAPH_IRI))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("Catch all domain found for multiple sites"));

    // Act
    siteResourceProvider.loadResources();
  }

}
