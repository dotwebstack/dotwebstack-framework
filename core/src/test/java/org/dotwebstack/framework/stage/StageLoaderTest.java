package org.dotwebstack.framework.stage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.site.Site;
import org.dotwebstack.framework.site.SiteLoader;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StageLoaderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private SiteLoader siteLoader;

  @Mock
  private Site site;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private StageLoader stageLoader;

  @Before
  public void setUp() {
    stageLoader = new StageLoader(siteLoader, configurationBackend);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(siteLoader.getSite(any())).thenReturn(site);
  }

  @Test
  public void loadStage() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.STAGE, RDF.TYPE, ELMO.STAGE),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.SITE, DBEERPEDIA.SITE),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.BASE_PATH, DBEERPEDIA.BASE_PATH))));

    // Act
    stageLoader.load();

    // Assert
    assertThat(stageLoader.getNumberOfStages(), equalTo(1));
    Stage stage = stageLoader.getStage(DBEERPEDIA.STAGE);
    assertThat(stage, is(not(nullValue())));
    assertThat(stage.getSite(), equalTo(site));
    assertThat(stage.getBasePath(), equalTo(DBEERPEDIA.BASE_PATH.stringValue()));
  }

  @Test
  public void loadMultipleStages() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.STAGE, RDF.TYPE, ELMO.STAGE),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.SITE, DBEERPEDIA.SITE),
            valueFactory.createStatement(DBEERPEDIA.STAGE, ELMO.BASE_PATH, DBEERPEDIA.BASE_PATH),
            valueFactory.createStatement(DBEERPEDIA.SECOND_STAGE, RDF.TYPE, ELMO.STAGE),
            valueFactory.createStatement(DBEERPEDIA.SECOND_STAGE, ELMO.SITE, DBEERPEDIA.SITE))));

    // Act
    stageLoader.load();

    // Assert
    assertThat(stageLoader.getNumberOfStages(), equalTo(2));
  }

  @Test
  public void expectsSite() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.STAGE, RDF.TYPE, ELMO.STAGE))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> site has been found for stage <%s>.", ELMO.SITE, DBEERPEDIA.STAGE));

    // Act
    stageLoader.load();

  }
}
