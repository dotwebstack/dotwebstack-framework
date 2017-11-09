package org.dotwebstack.framework.param;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
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
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ParameterResourceProviderTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Mock
  private GraphQuery graphQueryMock;

  private ParameterResourceProvider provider;

  @Before
  public void setUp() {
    ConfigurationBackend configurationBackendMock = mock(ConfigurationBackend.class);
    ApplicationProperties applicationPropertiesMock = mock(ApplicationProperties.class);

    provider = new ParameterResourceProvider(configurationBackendMock, applicationPropertiesMock);

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
            VALUE_FACTORY.createStatement(DBEERPEDIA.PARAMETER, RDF.TYPE, ELMO.TERM_FILTER),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PARAMETER, ELMO.NAME_PROP,
                DBEERPEDIA.PARAMETER_NAME_VALUE))));

    // Act
    provider.loadResources();

    // Assert
    assertThat(provider.getAll().entrySet(), hasSize(1));

    Parameter<?> result = provider.get(DBEERPEDIA.PARAMETER);

    assertThat(result, instanceOf(TermParameter.class));
    assertThat(result.getIdentifier(), is(DBEERPEDIA.PARAMETER));
    assertThat(result.getName(), is(DBEERPEDIA.PARAMETER_NAME_VALUE.stringValue()));
  }

  @Test
  public void loadResources_ThrowsException_TypeStatementMissing() {
    // Arrange
    when(graphQueryMock.evaluate()).thenReturn(
        new IteratingGraphQueryResult(ImmutableMap.of(), ImmutableList.of(
            VALUE_FACTORY.createStatement(DBEERPEDIA.PARAMETER, RDF.TYPE, ELMO.TERM_FILTER))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No <%s> property found for <%s> of type <%s>",
        ELMO.NAME_PROP, DBEERPEDIA.PARAMETER, ELMO.TERM_FILTER));

    // Act
    provider.loadResources();
  }

}
