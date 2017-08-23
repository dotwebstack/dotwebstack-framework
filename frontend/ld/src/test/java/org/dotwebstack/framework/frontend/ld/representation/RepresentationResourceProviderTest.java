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
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.http.stage.StageResourceProvider;
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
  private InformationProductResourceProvider informationProductResourceProvider;

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
        informationProductResourceProvider, stageResourceProvider);

    when(configurationBackend.getRepository()).thenReturn(configurationRepository);
    when(configurationRepository.getConnection()).thenReturn(configurationRepositoryConnection);
    when(configurationRepositoryConnection.prepareGraphQuery(anyString())).thenReturn(graphQuery);

    when(informationProductResourceProvider.get(any())).thenReturn(informationProduct);
    when(stageResourceProvider.get(any())).thenReturn(stage);
  }

  @Test
  public void loadReprestentation() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.REPRESENTATION, RDF.TYPE,
                ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.REPRESENTATION, ELMO.INFORMATION_PRODUCT_PROP,
                DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.REPRESENTATION, ELMO.URL_PATTERN,
                DBEERPEDIA.URL_PATTERN),
            valueFactory.createStatement(DBEERPEDIA.REPRESENTATION, ELMO.STAGE_PROP,
                DBEERPEDIA.STAGE))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    assertThat(representationResourceProvider.getAll().entrySet(), hasSize(1));
    Representation representation = representationResourceProvider.get(DBEERPEDIA.REPRESENTATION);
    assertThat(representation, is(not(nullValue())));
    assertThat(representation.getInformationProduct(), equalTo(informationProduct));
    assertThat(representation.getUrlPattern(), equalTo(DBEERPEDIA.URL_PATTERN.stringValue()));
  }

  @Test
  public void loadMultipleRepresentations() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory.createStatement(DBEERPEDIA.REPRESENTATION, RDF.TYPE, ELMO.REPRESENTATION),
            valueFactory
                .createStatement(DBEERPEDIA.REPRESENTATION, ELMO.INFORMATION_PRODUCT_PROP,
                    DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory
                .createStatement(DBEERPEDIA.REPRESENTATION, ELMO.URL_PATTERN,
                    DBEERPEDIA.URL_PATTERN),
            valueFactory
                .createStatement(DBEERPEDIA.SECOND_REPRESENTATION, RDF.TYPE, ELMO.REPRESENTATION),
            valueFactory
                .createStatement(DBEERPEDIA.SECOND_REPRESENTATION, ELMO.INFORMATION_PRODUCT_PROP,
                    DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
            valueFactory.createStatement(DBEERPEDIA.SECOND_REPRESENTATION, ELMO.URL_PATTERN,
                DBEERPEDIA.URL_PATTERN))));

    // Act
    representationResourceProvider.loadResources();

    // Assert
    assertThat(representationResourceProvider.getAll().entrySet(), hasSize(2));
  }

  @Test
  public void expectsInformatieProduct() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory
                .createStatement(DBEERPEDIA.REPRESENTATION, RDF.TYPE, ELMO.REPRESENTATION))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String
        .format("No <%s> information product has been found for representation <%s>.",
            ELMO.INFORMATION_PRODUCT_PROP, DBEERPEDIA.REPRESENTATION));

    // Act
    representationResourceProvider.loadResources();
  }

  @Test
  public void expectsUrlPattern() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            valueFactory
                .createStatement(DBEERPEDIA.REPRESENTATION, RDF.TYPE, ELMO.REPRESENTATION),
            valueFactory.createStatement(DBEERPEDIA.REPRESENTATION, ELMO.INFORMATION_PRODUCT_PROP,
                DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String
        .format("No <%s> url pattern has been found for representation <%s>.",
            ELMO.URL_PATTERN, DBEERPEDIA.REPRESENTATION));

    // Act
    representationResourceProvider.loadResources();
  }
}
