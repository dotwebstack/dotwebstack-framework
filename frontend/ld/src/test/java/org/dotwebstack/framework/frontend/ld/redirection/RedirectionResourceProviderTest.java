package org.dotwebstack.framework.frontend.ld.redirection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
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

@RunWith(MockitoJUnitRunner.class)
public class RedirectionResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private InformationProductResourceProvider informationProductResourceProvider;

  @Mock
  private StageResourceProvider stageResourceProvider;

  @Mock
  private Stage stage;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private RedirectionResourceProvider redirectionResourceProvider;

  @Before
  public void setUp() {
    redirectionResourceProvider = new RedirectionResourceProvider(configurationBackend,
        stageResourceProvider, applicationProperties);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(stageResourceProvider.get(any())).thenReturn(stage);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RedirectionResourceProvider(null, stageResourceProvider, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingStageResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RedirectionResourceProvider(configurationBackend, null, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RedirectionResourceProvider(configurationBackend, stageResourceProvider, null);
  }

  @Test
  public void loadResources_LoadRedirection_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, RDF.TYPE, ELMO.REDIRECTION),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.PATH_PATTERN,
                DBEERPEDIA.ID2DOC_PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.STAGE_PROP,
                DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.REDIRECT_TEMPLATE,
                DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE))));

    // Act
    redirectionResourceProvider.loadResources();

    // Assert
    assertThat(redirectionResourceProvider.getAll().entrySet(), hasSize(1));
    Redirection redirection = redirectionResourceProvider.get(DBEERPEDIA.ID2DOC_REDIRECTION);
    assertThat(redirection, is(not(nullValue())));
    assertThat(redirection.getPathPattern(), equalTo(DBEERPEDIA.ID2DOC_PATH_PATTERN.stringValue()));
    assertThat(redirection.getStage(), equalTo(stage));
    assertThat(redirection.getRedirectTemplate(),
        equalTo(DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE.stringValue()));
  }

  @Test
  public void loadResources_ThrowsException_WithMissingPathPattern() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, RDF.TYPE, ELMO.REDIRECTION),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.STAGE_PROP,
                DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.REDIRECT_TEMPLATE,
                DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE))));

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    redirectionResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_WithMissingStageProp() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, RDF.TYPE, ELMO.REDIRECTION),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.PATH_PATTERN,
                DBEERPEDIA.ID2DOC_PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.REDIRECT_TEMPLATE,
                DBEERPEDIA.ID2DOC_REDIRECT_TEMPLATE))));

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    redirectionResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_WithMissingRedirectTemplate() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, RDF.TYPE, ELMO.REDIRECTION),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.PATH_PATTERN,
                DBEERPEDIA.ID2DOC_PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.ID2DOC_REDIRECTION, ELMO.STAGE_PROP,
                DBEERPEDIA.STAGE))));

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    redirectionResourceProvider.loadResources();
  }

}
