package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
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
public class InformationProductResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private ConfigurationBackend configurationBackend;

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
        new InformationProductResourceProvider(configurationBackend, backendResourceProvider);

    when(backendResourceProvider.get(any())).thenReturn(backend);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);
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
        any())).thenReturn(informationProduct);

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
  public void loadResources_CreatesInformationProduct_WithCorrectValues() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDFS.LABEL,
                DBEERPEDIA.BREWERIES_LABEL))));

    InformationProduct informationProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
        eq(DBEERPEDIA.BREWERIES_LABEL.stringValue()), any())).thenReturn(informationProduct);

    // Act
    informationProductResourceProvider.loadResources();

    // Assert
    verify(backend).createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
        eq(DBEERPEDIA.BREWERIES_LABEL.stringValue()), any());
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
