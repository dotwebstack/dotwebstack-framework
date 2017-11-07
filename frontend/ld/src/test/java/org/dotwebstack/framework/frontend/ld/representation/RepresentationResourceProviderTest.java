package org.dotwebstack.framework.frontend.ld.representation;

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
import java.util.Collections;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
import org.dotwebstack.framework.frontend.ld.appearance.AppearanceResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
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
public class RepresentationResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private InformationProductResourceProvider informationProductResourceProvider;

  @Mock
  private AppearanceResourceProvider appearanceResourceProvider;

  @Mock
  private StageResourceProvider stageResourceProvider;

  @Mock
  private InformationProduct informationProduct;

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

  private RepresentationResourceProvider representationResourceProvider;

  @Before
  public void setUp() {
    representationResourceProvider = new RepresentationResourceProvider(configurationBackend,
        informationProductResourceProvider, appearanceResourceProvider,
        stageResourceProvider, applicationProperties);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(informationProductResourceProvider.get(any())).thenReturn(informationProduct);
    when(stageResourceProvider.get(any())).thenReturn(stage);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(null, informationProductResourceProvider,
        appearanceResourceProvider, stageResourceProvider, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingInformationProductResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(configurationBackend, null, null, null,
        applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingAppearanceResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(configurationBackend, informationProductResourceProvider,
        null, stageResourceProvider, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingStageResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(configurationBackend, informationProductResourceProvider,
        appearanceResourceProvider, null, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(configurationBackend, informationProductResourceProvider,
        appearanceResourceProvider, stageResourceProvider, null);
  }

  @Test
  public void loadResources_LoadRepresentation_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.URL_PATTERN, DBEERPEDIA.URL_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    assertThat(representationResourceProvider.getAll().entrySet(), hasSize(1));
    Representation representation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(representation, is(not(nullValue())));
    assertThat(representation.getInformationProduct(), equalTo(informationProduct));
    assertThat(representation.getUrlPatterns().toArray()[0],
        equalTo(DBEERPEDIA.URL_PATTERN.stringValue()));
    assertThat(representation.getStage(), equalTo(stage));
  }

  @Test
  public void loadResources_LoadMultipleRepresentation_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.URL_PATTERN, DBEERPEDIA.URL_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, ELMO.URL_PATTERN,
                DBEERPEDIA.URL_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, ELMO.STAGE_PROP,
                DBEERPEDIA.STAGE))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    assertThat(representationResourceProvider.getAll().entrySet(), hasSize(2));
  }

  @Test
  public void loadResources_LoadRepresentation_WithMissingInformationProduct() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.URL_PATTERN, DBEERPEDIA.URL_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE))));
    // Act
    representationResourceProvider.loadResources();

    // Assert
    Representation representation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(representation.getInformationProduct(), is(nullValue()));
  }

  @Test
  public void loadResources_LoadRepresentation_WithMissingStage() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.URL_PATTERN, DBEERPEDIA.URL_PATTERN))));
    // Act
    representationResourceProvider.loadResources();

    // Assert
    Representation representation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(representation.getStage(), is(nullValue()));
  }

  @Test
  public void loadResources_LoadRepresentation_WithMissingUrlPatterns() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT))));
    // Act
    representationResourceProvider.loadResources();

    // Assert
    Representation representation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(representation.getUrlPatterns(), is(Collections.EMPTY_LIST));
    assertThat(representation.getStage(), not(nullValue()));
    assertThat(representation.getInformationProduct(), not(nullValue()));
  }

  @Test
  public void loadResources_LoadRepresentation_WithSimpleData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
            RDF.TYPE, ELMO.REPRESENTATION))));
    // Act
    representationResourceProvider.loadResources();

    // Assert
    Representation representation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(representation.getUrlPatterns(), is(Collections.EMPTY_LIST));
    assertThat(representation.getStage(), is(nullValue()));
    assertThat(representation.getInformationProduct(), is(nullValue()));
  }
}
