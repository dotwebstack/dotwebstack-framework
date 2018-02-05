package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
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
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterDefinitionResourceProviderTest {

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private GraphQuery graphQueryMock;

  @Mock
  private ParameterDefinitionFactory parameterDefinitionFactoryMock;

  private ParameterDefinitionResourceProvider provider;

  @Before
  public void setUp() {
    ConfigurationBackend configurationBackendMock = mock(ConfigurationBackend.class);
    ApplicationProperties applicationPropertiesMock = mock(ApplicationProperties.class);

    provider = new ParameterDefinitionResourceProvider(configurationBackendMock,
        applicationPropertiesMock, ImmutableList.of(parameterDefinitionFactoryMock));

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
    when(graphQueryMock.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            VALUE_FACTORY.createStatement(DBEERPEDIA.NAME_PARAMETER_ID, RDF.TYPE,
                ELMO.TERM_PARAMETER),
            VALUE_FACTORY.createStatement(DBEERPEDIA.NAME_PARAMETER_ID, ELMO.NAME_PROP,
                DBEERPEDIA.NAME_PARAMETER_VALUE),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PLACE_PARAMETER_ID, RDF.TYPE,
                ELMO.TERM_PARAMETER),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PLACE_PARAMETER_ID, ELMO.NAME_PROP,
                DBEERPEDIA.PLACE_PARAMETER_VALUE))));

    when(parameterDefinitionFactoryMock.supports(ELMO.TERM_PARAMETER)).thenReturn(true);

    ParameterDefinition nameParameterDefinition = mock(ParameterDefinition.class);
    when(parameterDefinitionFactoryMock.create(Mockito.any(),
        Mockito.eq(DBEERPEDIA.NAME_PARAMETER_ID))).thenReturn(nameParameterDefinition);

    ParameterDefinition placeParameterDefinition = mock(ParameterDefinition.class);
    when(parameterDefinitionFactoryMock.create(Mockito.any(),
        Mockito.eq(DBEERPEDIA.PLACE_PARAMETER_ID))).thenReturn(placeParameterDefinition);

    // Act
    provider.loadResources();

    // Assert
    assertThat(provider.getAll().entrySet(), hasSize(2));
    assertThat(provider.get(DBEERPEDIA.NAME_PARAMETER_ID), sameInstance(nameParameterDefinition));
    assertThat(provider.get(DBEERPEDIA.PLACE_PARAMETER_ID), sameInstance(placeParameterDefinition));
  }

  @Test
  public void loadResources_ReturnsNull_WhenFactoryDoesNotSupportParameterType() {
    // Arrange
    when(graphQueryMock.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            VALUE_FACTORY.createStatement(DBEERPEDIA.NAME_PARAMETER_ID, RDF.TYPE,
                ELMO.TERM_PARAMETER),
            VALUE_FACTORY.createStatement(DBEERPEDIA.NAME_PARAMETER_ID, ELMO.NAME_PROP,
                DBEERPEDIA.NAME_PARAMETER_VALUE))));

    when(parameterDefinitionFactoryMock.supports(ELMO.TERM_PARAMETER)).thenReturn(false);

    // Act
    provider.loadResources();

    // Assert
    assertThat(provider.getAll().entrySet(), hasSize(0));
  }

  @Test
  public void loadResources_ThrowsException_TypeStatementMissing() {
    // Arrange
    when(graphQueryMock.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(VALUE_FACTORY.createStatement(DBEERPEDIA.NAME_PARAMETER_ID, ELMO.NAME_PROP,
            DBEERPEDIA.NAME_PARAMETER_VALUE))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> statement has been found for parameter <%s>.",
        RDF.TYPE, DBEERPEDIA.NAME_PARAMETER_ID));

    // Act
    provider.loadResources();
  }

}
