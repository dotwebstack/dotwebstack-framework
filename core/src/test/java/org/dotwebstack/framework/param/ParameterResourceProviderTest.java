package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.shapes.BooleanPropertyShape;
import org.dotwebstack.framework.param.shapes.IntegerPropertyShape;
import org.dotwebstack.framework.param.shapes.StringPropertyShape;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
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
public class ParameterResourceProviderTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private GraphQuery graphQueryMock;

  private ParameterResourceProvider provider;

  @Mock
  private TermParameterResourceFactory termParameterResourceFactory;

  private ConfigurationBackend configurationBackendMock;

  private ApplicationProperties applicationPropertiesMock;

  @Before
  public void setUp() {
    configurationBackendMock = mock(ConfigurationBackend.class);
    applicationPropertiesMock = mock(ApplicationProperties.class);


    when(termParameterResourceFactory.supports(any())).thenReturn(true);
    provider = new ParameterResourceProvider(configurationBackendMock, applicationPropertiesMock,
        ImmutableList.of(termParameterResourceFactory));

    SailRepository sailRepositoryMock = mock(SailRepository.class);
    when(configurationBackendMock.getRepository()).thenReturn(sailRepositoryMock);

    SailRepositoryConnection sailRepositoryConnectionMock = mock(SailRepositoryConnection.class);
    when(sailRepositoryMock.getConnection()).thenReturn(sailRepositoryConnectionMock);
    when(sailRepositoryConnectionMock.prepareGraphQuery(anyString())).thenReturn(graphQueryMock);

    when(applicationPropertiesMock.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void loadResources_GetResources_WithValidData() {
    // Arrange
    TermParameterDefinition termParameter = new TermParameterDefinition(
        SimpleValueFactory.getInstance().createIRI("http://dbeerpedia.org#nameParameter"), "name",
        Optional.empty());
    TermParameterDefinition termParameter2 = new TermParameterDefinition(
        SimpleValueFactory.getInstance().createIRI("http://dbeerpedia.org#placeParameter"), "place",
        Optional.empty());
    when(termParameterResourceFactory.create(any(), any())).thenReturn(termParameter,
        termParameter2);
    when(graphQueryMock.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            VALUE_FACTORY.createStatement(DBEERPEDIA.NAME_PARAMETER_ID, RDF.TYPE, ELMO.TERM_FILTER),
            VALUE_FACTORY.createStatement(DBEERPEDIA.NAME_PARAMETER_ID, ELMO.NAME_PROP,
                DBEERPEDIA.NAME_PARAMETER_VALUE),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PLACE_PARAMETER_ID, RDF.TYPE,
                ELMO.TERM_FILTER),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PLACE_PARAMETER_ID, ELMO.NAME_PROP,
                DBEERPEDIA.PLACE_PARAMETER_VALUE))));

    // Act
    provider.loadResources();

    // Assert
    assertThat(provider.getAll().entrySet(), hasSize(2));

    ParameterDefinition nameParamDef = provider.get(DBEERPEDIA.NAME_PARAMETER_ID);

    assertThat(nameParamDef.getIdentifier(), is(DBEERPEDIA.NAME_PARAMETER_ID));
    assertThat(nameParamDef.getName(), is(DBEERPEDIA.NAME_PARAMETER_VALUE_STRING));

    ParameterDefinition placeParamDef = provider.get(DBEERPEDIA.PLACE_PARAMETER_ID);

    assertThat(placeParamDef.getIdentifier(), is(DBEERPEDIA.PLACE_PARAMETER_ID));
    assertThat(placeParamDef.getName(), is(DBEERPEDIA.PLACE_PARAMETER_VALUE_STRING));
  }

  @Test
  public void loadResources_ThrowsException_TypeStatementMissing() {
    // Arrange
    TermParameterResourceFactory termParameterResourceFactory = new TermParameterResourceFactory();
    provider = new ParameterResourceProvider(configurationBackendMock, applicationPropertiesMock,
        ImmutableList.of(termParameterResourceFactory));
    when(graphQueryMock.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(VALUE_FACTORY.createStatement(DBEERPEDIA.NAME_PARAMETER_ID, RDF.TYPE,
            ELMO.TERM_FILTER))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> property found for <%s> of type <%s>",
        ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_ID, ELMO.TERM_FILTER));

    // Act
    provider.loadResources();
  }

  @Test
  public void createResources_UnsupportedShape() {
    // Arrange
    TermParameterDefinition termParameter = new TermParameterDefinition(
        SimpleValueFactory.getInstance().createIRI("http://"), "", Optional.empty());
    when(termParameterResourceFactory.create(any(), any())).thenReturn(termParameter);

    ModelBuilder modelBuilder = new ModelBuilder();

    BNode head = SimpleValueFactory.getInstance().createBNode();
    modelBuilder.add(DBEERPEDIA.NAME_PARAMETER_ID, RDF.TYPE, ELMO.TERM_FILTER);
    modelBuilder.add(DBEERPEDIA.NAME_PARAMETER_ID, ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE);
    modelBuilder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(ELMO.SHAPE_PROP, head).subject(head).add(
        SimpleValueFactory.getInstance().createIRI("http://"),
        SimpleValueFactory.getInstance().createIRI("http://"));
    Model model = modelBuilder.build();

    // Act
    ParameterDefinition parameterDefinition =
        provider.createResource(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertThat(parameterDefinition.getShapeTypes(), is(Optional.empty()));
  }

  @Test
  public void createResources_IntegerFilterRecognition() {
    // Arrange
    TermParameterDefinition termParameter =
        new TermParameterDefinition(SimpleValueFactory.getInstance().createIRI("http://"), "",
            Optional.of(new IntegerPropertyShape()));
    when(termParameterResourceFactory.create(any(), any())).thenReturn(termParameter);

    ModelBuilder modelBuilder = new ModelBuilder();

    BNode head = SimpleValueFactory.getInstance().createBNode();
    modelBuilder.add(DBEERPEDIA.NAME_PARAMETER_ID, RDF.TYPE, ELMO.TERM_FILTER);
    modelBuilder.add(DBEERPEDIA.NAME_PARAMETER_ID, ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE);
    modelBuilder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(ELMO.SHAPE_PROP, head).subject(head).add(
        ELMO.TERM_FILTER, XMLSchema.INTEGER);
    Model model = modelBuilder.build();

    // Act
    ParameterDefinition parameterDefinition =
        provider.createResource(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertTrue(parameterDefinition.getShapeTypes().get() instanceof IntegerPropertyShape);
  }

  @Test
  public void createResources_BooleanFilterRecognition() {
    // Arrange

    TermParameterDefinition termParameter =
        new TermParameterDefinition(SimpleValueFactory.getInstance().createIRI("http://"), "",
            Optional.of(new BooleanPropertyShape()));
    when(termParameterResourceFactory.create(any(), any())).thenReturn(termParameter);

    ModelBuilder modelBuilder = new ModelBuilder();

    BNode head = SimpleValueFactory.getInstance().createBNode();
    modelBuilder.add(DBEERPEDIA.NAME_PARAMETER_ID, RDF.TYPE, ELMO.TERM_FILTER);
    modelBuilder.add(DBEERPEDIA.NAME_PARAMETER_ID, ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE);
    modelBuilder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(ELMO.SHAPE_PROP, head).subject(head).add(
        ELMO.TERM_FILTER, XMLSchema.BOOLEAN);
    Model model = modelBuilder.build();

    // Act
    ParameterDefinition parameterDefinition =
        provider.createResource(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertTrue(parameterDefinition.getShapeTypes().get() instanceof BooleanPropertyShape);
  }

  @Test
  public void createResources_StringFilterRecognition() {
    // Arrange
    TermParameterDefinition termParameter =
        new TermParameterDefinition(SimpleValueFactory.getInstance().createIRI("http://"), "",
            Optional.of(new StringPropertyShape()));
    when(termParameterResourceFactory.create(any(), any())).thenReturn(termParameter);

    ModelBuilder modelBuilder = new ModelBuilder();

    BNode head = SimpleValueFactory.getInstance().createBNode();
    modelBuilder.add(DBEERPEDIA.NAME_PARAMETER_ID, RDF.TYPE, ELMO.TERM_FILTER);
    modelBuilder.add(DBEERPEDIA.NAME_PARAMETER_ID, ELMO.NAME_PROP, DBEERPEDIA.NAME_PARAMETER_VALUE);
    modelBuilder.subject(DBEERPEDIA.NAME_PARAMETER_ID).add(ELMO.SHAPE_PROP, head).subject(head).add(
        ELMO.TERM_FILTER, XMLSchema.STRING);
    Model model = modelBuilder.build();

    // Act
    ParameterDefinition parameterDefinition =
        provider.createResource(model, DBEERPEDIA.NAME_PARAMETER_ID);

    // Assert
    assertTrue(parameterDefinition.getShapeTypes().get() instanceof StringPropertyShape);
  }

}
