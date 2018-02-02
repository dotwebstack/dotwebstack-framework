package org.dotwebstack.framework.frontend.ld.parameter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.parameter.source.ParameterSourceFactory;
import org.dotwebstack.framework.frontend.ld.parameter.target.TargetFactory;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.dotwebstack.framework.param.ParameterDefinitionResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.dotwebstack.framework.vocabulary.HTTP;
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
public class ParameterMapperResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ParameterDefinitionResourceProvider parameterDefinitionResourceProvider;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private ParameterDefinition parameterDefinition;

  private ParameterMapperResourceProvider parameterMapperResourceProvider;

  private ParameterMapperFactory parameterMapperFactory;

  private ParameterSourceFactory parameterSourceFactory;

  private TargetFactory targetFactory;

  @Mock
  private GraphQuery graphQuery;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  @Before
  public void setUp() {
    parameterSourceFactory = new ParameterSourceFactory();
    targetFactory = new TargetFactory(parameterDefinitionResourceProvider);
    parameterMapperFactory = new ParameterMapperFactory(parameterSourceFactory, targetFactory);
    parameterMapperResourceProvider = new ParameterMapperResourceProvider(configurationBackend,
        parameterMapperFactory, applicationProperties);

    when(parameterDefinitionResourceProvider.get(DBEERPEDIA.SUBJECT_PARAMETER)).thenReturn(
        parameterDefinition);
    // when(parameterDefinition.getName()).thenReturn(DBEERPEDIA.SUBJECT_PARAMETER_NAME);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void loadResources_LoadUriParameterMapper_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, RDF.TYPE,
                ELMO.URI_PARAMETER_MAPPER),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.SOURCE_PROP,
                HTTP.REQUEST_URI),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.PATTERN_PROP,
                DBEERPEDIA.SUBJECT_FROM_PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TEMPLATE_PROP,
                DBEERPEDIA.SUBJECT_FROM_URL_TEMPLATE),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TARGET_PROP,
                DBEERPEDIA.SUBJECT_PARAMETER))));

    // Act
    parameterMapperResourceProvider.loadResources();

    // Assert
    assertThat(parameterMapperResourceProvider.getAll().entrySet(), hasSize(1));
    ParameterMapper parameterMapper =
        parameterMapperResourceProvider.get(DBEERPEDIA.SUBJECT_FROM_URL);
    assertThat(parameterMapper, is(not(nullValue())));
  }

  @Test
  public void loadResources_ThrowException_WithMissingSource() {
    // Assert
    thrown.expect(ConfigurationException.class);

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, RDF.TYPE,
                ELMO.URI_PARAMETER_MAPPER),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.PATTERN_PROP,
                DBEERPEDIA.SUBJECT_FROM_PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TEMPLATE_PROP,
                DBEERPEDIA.SUBJECT_FROM_URL_TEMPLATE),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TARGET_PROP,
                DBEERPEDIA.SUBJECT_PARAMETER))));

    // Act
    parameterMapperResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowException_WithMissingPattern() {
    // Assert
    thrown.expect(ConfigurationException.class);

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, RDF.TYPE,
                ELMO.URI_PARAMETER_MAPPER),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.SOURCE_PROP,
                HTTP.REQUEST_URI),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TEMPLATE_PROP,
                DBEERPEDIA.SUBJECT_FROM_URL_TEMPLATE),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TARGET_PROP,
                DBEERPEDIA.SUBJECT_PARAMETER))));

    // Act
    parameterMapperResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowException_WithMissingTemplate() {
    // Assert
    thrown.expect(ConfigurationException.class);

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, RDF.TYPE,
                ELMO.URI_PARAMETER_MAPPER),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.SOURCE_PROP,
                HTTP.REQUEST_URI),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.PATTERN_PROP,
                DBEERPEDIA.SUBJECT_FROM_PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TARGET_PROP,
                DBEERPEDIA.SUBJECT_PARAMETER))));

    // Act
    parameterMapperResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowException_WithMissingTarget() {
    // Assert
    thrown.expect(ConfigurationException.class);

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, RDF.TYPE,
                ELMO.URI_PARAMETER_MAPPER),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.SOURCE_PROP,
                HTTP.REQUEST_URI),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.PATTERN_PROP,
                DBEERPEDIA.SUBJECT_FROM_PATH_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TEMPLATE_PROP,
                DBEERPEDIA.SUBJECT_FROM_URL_TEMPLATE))));

    // Act
    parameterMapperResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowException_WithUnknownParameterMapper() {
    // Assert
    thrown.expect(ConfigurationException.class);

    // Arrange
    ValueFactory valueFactory = SimpleValueFactory.getInstance();
    final IRI unknownParameterMapper =
        valueFactory.createIRI("http://dotwebstack.org/def/elmo", "UnknownParameterMapper");

    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, RDF.TYPE,
                unknownParameterMapper),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.SOURCE_PROP,
                HTTP.REQUEST_URI),
            valueFactory.createStatement(DBEERPEDIA.SUBJECT_FROM_URL, ELMO.TARGET_PROP,
                DBEERPEDIA.SUBJECT_PARAMETER))));

    // Act
    parameterMapperResourceProvider.loadResources();
  }

}
