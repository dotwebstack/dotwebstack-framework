package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.filter.Filter;
import org.dotwebstack.framework.filter.FilterResourceProvider;
import org.dotwebstack.framework.filter.StringFilter;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
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
public class InformationProductResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private FilterResourceProvider filterResourceProviderMock;

  @Mock
  private ConfigurationBackend configurationBackend;

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private SailRepository configurationRepository;

  @Mock
  private SailRepositoryConnection configurationRepositoryConnection;

  @Mock
  private GraphQuery graphQuery;

  @Mock
  private Backend backend;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private InformationProductResourceProvider informationProductResourceProvider;

  @Before
  public void setUp() {
    informationProductResourceProvider =
        new InformationProductResourceProvider(configurationBackend, backendResourceProvider,
            filterResourceProviderMock, applicationProperties);

    when(backendResourceProvider.get(any())).thenReturn(backend);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(applicationProperties.getSystemGraph()).thenReturn(DBEERPEDIA.SYSTEM_GRAPH_IRI);
  }

  @Test
  public void constructor_ThrowsException_WithMissingConfigurationBackend() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new InformationProductResourceProvider(null, backendResourceProvider,
        filterResourceProviderMock, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingBackendResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new InformationProductResourceProvider(configurationBackend, null, filterResourceProviderMock,
        applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new InformationProductResourceProvider(configurationBackend, backendResourceProvider,
        filterResourceProviderMock, null);
  }

  @Test
  public void loadResources_LoadInformationProduct_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND))));

    InformationProduct informationProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT), eq(null),
        eq(ImmutableList.of()), eq(ImmutableList.of()), any())).thenReturn(informationProduct);

    // Act
    informationProductResourceProvider.loadResources();

    // Assert
    assertThat(informationProductResourceProvider.getAll().entrySet(), hasSize(1));
    InformationProduct product =
        informationProductResourceProvider.get(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT);
    assertThat(product, equalTo(informationProduct));
  }

  @Test
  public void loadResources_LoadsSeveralInformationProducts_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
            valueFactory.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.BACKEND_PROP,
                DBEERPEDIA.BACKEND))));

    // Act
    informationProductResourceProvider.loadResources();

    // Assert
    assertThat(informationProductResourceProvider.getAll().entrySet(), hasSize(2));
  }

  @Test
  public void get_ThrowsException_ResourceNotFound_WithMultipleOtherInformationProducts() {
    IRI unknownResource = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "foo");

    // Assert
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(
        String.format("Resource <%s> not found. Available resources: [<%s>, <%s>]", unknownResource,
            DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT));
    thrown.expectMessage(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT.toString());
    thrown.expectMessage(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT.toString());

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
            valueFactory.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.BACKEND_PROP,
                DBEERPEDIA.BACKEND))));

    informationProductResourceProvider.loadResources();

    // Act
    informationProductResourceProvider.get(unknownResource);
  }

  @Test
  public void loadResources_CreatesInformationProduct_WithCorrectValues() {
    IRI parameter1Id = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "parameter1");
    IRI parameter2Id = valueFactory.createIRI(DBEERPEDIA.NAMESPACE, "parameter2");

    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDFS.LABEL,
                DBEERPEDIA.BREWERIES_LABEL),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.REQUIRED_PARAMETER_PROP, parameter1Id),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.REQUIRED_PARAMETER_PROP, parameter2Id))));

    Filter filter1 = new StringFilter(parameter1Id, "param1");
    when(filterResourceProviderMock.get(parameter1Id)).thenReturn(filter1);

    Filter filter2 = new StringFilter(parameter2Id, "param2");
    when(filterResourceProviderMock.get(parameter2Id)).thenReturn(filter2);

    InformationProduct informationProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
        eq(DBEERPEDIA.BREWERIES_LABEL.stringValue()), eq(ImmutableList.of(filter1, filter2)),
        eq(ImmutableList.of()), any())).thenReturn(informationProduct);

    // Act
    informationProductResourceProvider.loadResources();
  }

  @Test
  public void loadResources_ThrowsException_WithMissingBackendParameter() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
            RDF.TYPE, ELMO.INFORMATION_PRODUCT))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("No <%s> statement has been found for information product <%s>.",
            ELMO.BACKEND_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT));

    // Act
    informationProductResourceProvider.loadResources();
  }

}
