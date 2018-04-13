package org.dotwebstack.framework.frontend.ld.representation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
import org.dotwebstack.framework.frontend.ld.appearance.Appearance;
import org.dotwebstack.framework.frontend.ld.appearance.AppearanceResourceProvider;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapper;
import org.dotwebstack.framework.frontend.ld.parameter.ParameterMapperResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.informationproduct.InformationProductResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.TransactionResourceProvider;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
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
  private TransactionResourceProvider transactionResourceProvider;

  @Mock
  private AppearanceResourceProvider appearanceResourceProvider;

  @Mock
  private StageResourceProvider stageResourceProvider;

  @Mock
  private ParameterMapperResourceProvider parameterMapperResourceProvider;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private Transaction transaction;

  @Mock
  private Stage stage;

  @Mock
  private Appearance appearance;

  @Mock
  private ParameterMapper parameterMapper;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  private final ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private RepresentationResourceProvider representationResourceProvider;

  @Before
  public void setUp() {
    representationResourceProvider = new RepresentationResourceProvider(configurationBackend,
        informationProductResourceProvider, transactionResourceProvider, appearanceResourceProvider,
        stageResourceProvider, parameterMapperResourceProvider, applicationProperties);
    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(informationProductResourceProvider.get(any())).thenReturn(informationProduct);
    when(stageResourceProvider.get(any())).thenReturn(stage);
    when(appearanceResourceProvider.get(any())).thenReturn(appearance);
    when(parameterMapperResourceProvider.get(any())).thenReturn(parameterMapper);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(null, informationProductResourceProvider,
        transactionResourceProvider, appearanceResourceProvider, stageResourceProvider,
        parameterMapperResourceProvider, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingInformationProductResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(configurationBackend, null, null, null, null, null,
        applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingAppearanceResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(configurationBackend, informationProductResourceProvider,
        transactionResourceProvider, null, stageResourceProvider, parameterMapperResourceProvider,
        applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingStageResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(configurationBackend, informationProductResourceProvider,
        transactionResourceProvider, appearanceResourceProvider, null,
        parameterMapperResourceProvider, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationResourceProvider(configurationBackend, informationProductResourceProvider,
        transactionResourceProvider, appearanceResourceProvider, stageResourceProvider,
        parameterMapperResourceProvider, null);
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
                ELMO.APPLIES_TO_PROP, DBEERPEDIA.APPLIES_TO),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.APPEARANCE_PROP, DBEERPEDIA.BREWERY_APPEARANCE),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.PARAMETER_MAPPER_PROP, DBEERPEDIA.SUBJECT_FROM_URL))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    assertThat(representationResourceProvider.getAll().entrySet(), hasSize(1));
    Representation representation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(representation, is(not(nullValue())));
    assertThat(representation.getInformationProduct(), equalTo(informationProduct));
    assertThat(representation.getAppliesTo().toArray()[0],
        equalTo(DBEERPEDIA.APPLIES_TO.stringValue()));
    assertThat(representation.getStage(), equalTo(stage));
    assertThat(representation.getAppearance(), equalTo(appearance));
    assertThat(representation.getParameterMappers(), contains(parameterMapper));
  }

  @Test
  public void loadResources_LoadMultiplePathPatterns_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.APPLIES_TO_PROP, valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "helloWorld")),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.APPLIES_TO_PROP,
                valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "this/is/sparta")),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.APPEARANCE_PROP, DBEERPEDIA.BREWERY_APPEARANCE))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    assertThat(representationResourceProvider.getAll().entrySet(), hasSize(1));
    Representation representation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(representation, is(not(nullValue())));
    assertThat(representation.getInformationProduct(), equalTo(informationProduct));
    assertThat(representation.getAppliesTo().toArray()[1],
        equalTo(DBEERPEDIA.NAMESPACE + "helloWorld"));
    assertThat(representation.getAppliesTo().toArray()[0],
        equalTo(DBEERPEDIA.NAMESPACE + "this/is/sparta"));
    assertThat(representation.getStage(), equalTo(stage));
    assertThat(representation.getAppearance(), equalTo(appearance));
  }

  @Test
  public void loadResources_LoadMultipleSubRepresentations_WithValidData() {
    // Arrange
    final IRI subRepresentationName =
        valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "subrepresentation");
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            // first subrepresentation
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            // second subrepresentation
            valueFactory.createStatement(subRepresentationName, RDF.TYPE, ELMO.REPRESENTATION),
            valueFactory.createStatement(subRepresentationName, ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(subRepresentationName, ELMO.INFORMATION_PRODUCT_PROP,
                DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            // main representation
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION,
                ELMO.CONTAINS_PROP, DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION,
                ELMO.CONTAINS_PROP, subRepresentationName))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    Representation graphBreweryRepresentation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(graphBreweryRepresentation.getAppliesTo(), is(Collections.EMPTY_LIST));
    assertThat(graphBreweryRepresentation.getStage(), not(nullValue()));
    assertThat(graphBreweryRepresentation.getInformationProduct(), not(nullValue()));
    Representation tupleBreweryRepresentation =
        representationResourceProvider.get(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION);
    assertThat(tupleBreweryRepresentation.getAppliesTo(), is(Collections.EMPTY_LIST));
    assertThat(tupleBreweryRepresentation.getStage(), not(nullValue()));
    assertThat(tupleBreweryRepresentation.getInformationProduct(), not(nullValue()));
    Representation subRepresentation = representationResourceProvider.get(subRepresentationName);
    assertThat(tupleBreweryRepresentation.getSubRepresentations(),
        is(ImmutableList.of(graphBreweryRepresentation, subRepresentation)));
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
                ELMO.PATH_PATTERN, DBEERPEDIA.PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.BREWERY_REPRESENTATION, ELMO.PATH_PATTERN,
                DBEERPEDIA.PATH_PATTERN),
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
                ELMO.PATH_PATTERN, DBEERPEDIA.PATH_PATTERN),
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
  public void loadResources_LoadRepresentation_WithMissingStageAndAppearance() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.PATH_PATTERN, DBEERPEDIA.PATH_PATTERN))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    Representation representation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(representation.getStage(), is(nullValue()));
    assertThat(representation.getAppearance(), is(nullValue()));
  }

  @Test
  public void loadResources_LoadRepresentation_WithMissingAppliesTo() {
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
    assertThat(representation.getAppliesTo(), is(Collections.EMPTY_LIST));
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
    assertThat(representation.getAppliesTo(), is(Collections.EMPTY_LIST));
    assertThat(representation.getStage(), is(nullValue()));
    assertThat(representation.getInformationProduct(), is(nullValue()));
  }

  @Test
  public void loadResources_LoadRepresentation_WithSubRepresenatation() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION,
                ELMO.STAGE_PROP, DBEERPEDIA.STAGE),
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION,
                ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION,
                ELMO.CONTAINS_PROP, DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    Representation graphBreweryRepresentation =
        representationResourceProvider.get(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION);
    assertThat(graphBreweryRepresentation.getAppliesTo(), is(Collections.EMPTY_LIST));
    assertThat(graphBreweryRepresentation.getStage(), not(nullValue()));
    assertThat(graphBreweryRepresentation.getInformationProduct(), not(nullValue()));
    Representation tupleBreweryRepresentation =
        representationResourceProvider.get(DBEERPEDIA.TUPLE_BREWERY_LIST_REPRESENTATION);
    assertThat(tupleBreweryRepresentation.getAppliesTo(), is(Collections.EMPTY_LIST));
    assertThat(tupleBreweryRepresentation.getStage(), not(nullValue()));
    assertThat(tupleBreweryRepresentation.getInformationProduct(), not(nullValue()));
    assertThat(tupleBreweryRepresentation.getSubRepresentations(),
        is(ImmutableList.of(graphBreweryRepresentation)));
  }
}
