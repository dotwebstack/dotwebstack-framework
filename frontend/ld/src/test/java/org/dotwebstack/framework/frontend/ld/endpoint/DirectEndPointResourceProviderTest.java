package org.dotwebstack.framework.frontend.ld.endpoint;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.frontend.ld.service.Service;
import org.dotwebstack.framework.frontend.ld.service.ServiceResourceProvider;
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
public class DirectEndPointResourceProviderTest {

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
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private StageResourceProvider stageResourceProvider;

  @Mock
  private DirectEndPointResourceProvider endPointResourceProvider;

  @Mock
  private ServiceResourceProvider serviceResourceProvider;

  @Mock
  private Stage stage;

  @Mock
  private Representation representation;

  @Mock
  private GraphQuery graphQuery;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    endPointResourceProvider =
        new DirectEndPointResourceProvider(configurationBackend, applicationProperties,
            stageResourceProvider, representationResourceProvider, serviceResourceProvider);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);
    when(stageResourceProvider.get(any())).thenReturn(stage);
    when(representationResourceProvider.get(any())).thenReturn(representation);
    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
    when(serviceResourceProvider.get(any())).thenReturn(mock(Service.class));
  }

  @Test
  public void loadResources_LoadEndPoint_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, RDF.TYPE, ELMO.ENDPOINT),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.PATH_PATTERN,
                DBEERPEDIA.PATH_PATTERN))));

    // Act
    endPointResourceProvider.loadResources();

    // Assert
    assertThat(endPointResourceProvider.getAll().entrySet(), hasSize(1));
    AbstractEndPoint endPoint = endPointResourceProvider.get(DBEERPEDIA.DOC_ENDPOINT);
    assertThat(endPoint, is(not(nullValue())));
    assertThat(endPoint.getPathPattern(), equalTo(DBEERPEDIA.PATH_PATTERN.toString()));
  }

  @Test
  public void loadResources_LoadEndPointComplete_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, RDF.TYPE, ELMO.ENDPOINT),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.PATH_PATTERN,
                DBEERPEDIA.PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, RDFS.LABEL,
                DBEERPEDIA.BREWERIES_LABEL),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.STAGE_PROP,
                DBEERPEDIA.SECOND_STAGE),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.GET_REPRESENTATION_PROP,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.POST_REPRESENTATION_PROP,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.SERVICE_POST_PROP,
                DBEERPEDIA.SERVICE_POST),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.SERVICE_DELETE_PROP,
                DBEERPEDIA.SERVICE_DELETE),
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, ELMO.SERVICE_PUT_PROP,
                DBEERPEDIA.SERVICE_PUT))));

    // Act
    endPointResourceProvider.loadResources();

    // Assert
    assertThat(endPointResourceProvider.getAll().entrySet(), hasSize(1));
    DirectEndPoint endPoint =
        (DirectEndPoint) endPointResourceProvider.get(DBEERPEDIA.DOC_ENDPOINT);
    assertThat(endPoint, is(not(nullValue())));
    assertThat(endPoint.getPathPattern(), equalTo(DBEERPEDIA.PATH_PATTERN.toString()));
    assertThat(endPoint.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
    assertThat(endPoint.getStage(), equalTo(stage));
    assertThat(endPoint.getGetRepresentation(), equalTo(representation));
    assertThat(endPoint.getPostRepresentation(), equalTo(representation));
    assertThat(endPoint.getDeleteService(), not(nullValue()));
    assertThat(endPoint.getPostService(), not(nullValue()));
    assertThat(endPoint.getPutService(), not(nullValue()));
  }

  @Test
  public void loadResources_LoadDirectEndPoint_MissingPathPattern() {
    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for pathPattern <%s>.",
        ELMO.PATH_PATTERN, DBEERPEDIA.DOC_ENDPOINT));

    // Arrange
    when(graphQuery.evaluate()).thenReturn(
        new IteratingGraphQueryResult(ImmutableMap.of(), ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.DOC_ENDPOINT, RDF.TYPE, ELMO.ENDPOINT))));

    // Act
    endPointResourceProvider.loadResources();
  }

}
