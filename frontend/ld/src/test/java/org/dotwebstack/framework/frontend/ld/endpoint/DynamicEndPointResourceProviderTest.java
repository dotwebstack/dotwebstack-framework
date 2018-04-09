package org.dotwebstack.framework.frontend.ld.endpoint;

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
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapperResourceProvider;
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

@RunWith(MockitoJUnitRunner.class)
public class DynamicEndPointResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private ParameterMapperResourceProvider parameterMapperResourceProvider;

  @Mock
  private StageResourceProvider stageResourceProvider;

  @Mock
  private DynamicEndPointResourceProvider dynamicEndPointResourceProvider;

  @Mock
  private Stage stage;

  @Mock
  private ParameterMapper parameterMapper;

  @Mock
  private GraphQuery graphQuery;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    dynamicEndPointResourceProvider = new DynamicEndPointResourceProvider(configurationBackend,
        applicationProperties, parameterMapperResourceProvider, stageResourceProvider);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);
    when(stageResourceProvider.get(any())).thenReturn(stage);
    when(parameterMapperResourceProvider.get(any())).thenReturn(parameterMapper);
    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void loadResources_LoadEndPoint_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, RDF.TYPE, ELMO.DYNAMIC_ENDPOINT),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.PATH_PATTERN,
                DBEERPEDIA.PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.PARAMETER_MAPPER_PROP,
                DBEERPEDIA.SUBJECT_FROM_URL))));

    // Act
    dynamicEndPointResourceProvider.loadResources();

    // Assert
    assertThat(dynamicEndPointResourceProvider.getAll().entrySet(), hasSize(1));
    DynamicEndPoint dynamicEndPoint = dynamicEndPointResourceProvider.get(DBEERPEDIA.DOC_ENDPOINT);
    assertThat(dynamicEndPoint, is(not(nullValue())));
    assertThat(dynamicEndPoint.getPathPattern(), equalTo(DBEERPEDIA.PATH_PATTERN.toString()));
    assertThat(dynamicEndPoint.getParameterMapper(), equalTo(parameterMapper));
  }

  @Test
  public void loadResources_LoadDynamicEndPoint_MissingPathPattern() {
    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for pathPattern <%s>.",
        ELMO.PATH_PATTERN, DBEERPEDIA.DOC_ENDPOINT));

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, RDF.TYPE, ELMO.DYNAMIC_ENDPOINT),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.PARAMETER_MAPPER_PROP,
                DBEERPEDIA.SUBJECT_FROM_URL))));

    // Act
    dynamicEndPointResourceProvider.loadResources();
  }

  @Test
  public void loadResources_LoadEndPointComplete_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, RDF.TYPE, ELMO.DYNAMIC_ENDPOINT),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.PATH_PATTERN,
                DBEERPEDIA.PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.PARAMETER_MAPPER_PROP,
                DBEERPEDIA.SUBJECT_FROM_URL),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, RDFS.LABEL,
                DBEERPEDIA.BREWERIES_LABEL),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.STAGE_PROP,
                DBEERPEDIA.SECOND_STAGE))));

    // Act
    dynamicEndPointResourceProvider.loadResources();

    // Assert
    assertThat(dynamicEndPointResourceProvider.getAll().entrySet(), hasSize(1));
    DynamicEndPoint dynamicEndPoint = dynamicEndPointResourceProvider.get(DBEERPEDIA.DOC_ENDPOINT);
    assertThat(dynamicEndPoint, is(not(nullValue())));
    assertThat(dynamicEndPoint.getPathPattern(), equalTo(DBEERPEDIA.PATH_PATTERN.toString()));
    assertThat(dynamicEndPoint.getParameterMapper(), equalTo(parameterMapper));
    assertThat(dynamicEndPoint.getStage(), equalTo(stage));
    assertThat(dynamicEndPoint.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }

}
