package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendLoader;
import org.dotwebstack.framework.backend.BackendSource;
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
public class InformationProductLoaderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private BackendLoader backendLoader;

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

  @Mock
  private BackendSource backendSource;

  private ValueFactory valueFactory = SimpleValueFactory.getInstance();

  private InformationProductLoader informationProductLoader;

  @Before
  public void setUp() {
    informationProductLoader = new InformationProductLoader(backendLoader, configurationBackend);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);
  }

  @Test
  public void loadInformationProduct() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, ELMO.BACKEND_PROP,
                DBEERPEDIA.BACKEND))));

    when(backendLoader.getBackend(any())).thenReturn(backend);
    when(backend.createSource(any())).thenReturn(backendSource);

    // Act
    informationProductLoader.load();

    // Assert
    assertThat(informationProductLoader.getNumberOfInformationProducts(), equalTo(1));
    InformationProduct product =
        informationProductLoader.getInformationProduct(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT);
    assertThat(product, is(not(nullValue())));
  }

  @Test
  public void loadsSeveralInformationProducts() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, ELMO.BACKEND_PROP,
                DBEERPEDIA.BACKEND),
            valueFactory.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.BACKEND_PROP,
                DBEERPEDIA.BACKEND))));

    when(backendLoader.getBackend(any())).thenReturn(backend);
    when(backend.createSource(any())).thenReturn(backendSource);

    // Act
    informationProductLoader.load();

    // Assert
    assertThat(informationProductLoader.getNumberOfInformationProducts(), equalTo(2));
  }

  @Test
  public void fillsInformationProduct() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, ELMO.BACKEND_PROP,
                DBEERPEDIA.BACKEND),
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDFS.LABEL,
                DBEERPEDIA.BREWERIES_LABEL))));

    when(backendLoader.getBackend(DBEERPEDIA.BACKEND)).thenReturn(backend);
    when(backend.createSource(any())).thenReturn(backendSource);

    // Act
    informationProductLoader.load();

    // Assert
    InformationProduct product = informationProductLoader.getInformationProduct(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT);
    assertThat(product.getBackendSource(), equalTo(backendSource));
    assertThat(product.getIdentifier(), equalTo(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT));
    assertThat(product.getLabel(), equalTo(DBEERPEDIA.BREWERIES_LABEL.stringValue()));
  }


  @Test
  public void expectsBackendParameter() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("No <%s> backend has been found for information product <%s>.", ELMO.BACKEND_PROP,
            DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT));

    // Act
    informationProductLoader.load();
  }
}
