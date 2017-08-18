package org.dotwebstack.framework.site;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SiteLoaderTest {

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

  private SiteLoader siteLoader;

  @Before
  public void setUp() {
    siteLoader = new SiteLoader(configurationBackend);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);
  }

  @Test
  public void loadSite() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE),
            valueFactory.createStatement(DBEERPEDIA.SITE, ELMO.DOMAIN, DBEERPEDIA.DOMAIN))));

    // Act
    siteLoader.load();

    // Assert
    assertThat(siteLoader.getNumberOfSites(), equalTo(1));
    assertThat(siteLoader.getSite(DBEERPEDIA.SITE), is(not(nullValue())));
  }

  @Test
  public void loadsSeveralSites() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE),
            valueFactory.createStatement(DBEERPEDIA.SITE, ELMO.DOMAIN, DBEERPEDIA.DOMAIN),
            valueFactory.createStatement(DBEERPEDIA.SITE_NL, RDF.TYPE, ELMO.SITE),
            valueFactory.createStatement(DBEERPEDIA.SITE_NL, ELMO.DOMAIN, DBEERPEDIA.DOMAIN_NL))));

    // Act
    siteLoader.load();

    // Assert
    assertThat(siteLoader.getNumberOfSites(), equalTo(2));
  }

  @Test
  public void loadCatchAllSite() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SITE, RDF.TYPE, ELMO.SITE))));

    // Act
    siteLoader.load();

    // Assert
    assertThat(siteLoader.getNumberOfSites(), equalTo(1));
    assertThat(siteLoader.getSite(DBEERPEDIA.SITE), is(not(nullValue())));
  }
}
