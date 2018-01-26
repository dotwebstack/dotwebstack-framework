package org.dotwebstack.framework.informationproduct;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.dotwebstack.framework.ApplicationProperties;
import org.dotwebstack.framework.backend.Backend;
import org.dotwebstack.framework.backend.BackendResourceProvider;
import org.dotwebstack.framework.config.ConfigurationBackend;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.dotwebstack.framework.param.ParameterDefinitionResourceProvider;
import org.dotwebstack.framework.param.ShaclShape;
import org.dotwebstack.framework.param.TermParameterDefinition;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.dotwebstack.framework.vocabulary.ELMO;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.impl.IteratingGraphQueryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InformationProductResourceProviderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  @Mock
  private BackendResourceProvider backendResourceProvider;

  @Mock
  private ParameterDefinitionResourceProvider parameterDefinitionResourceProviderMock;

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

  private InformationProductResourceProvider informationProductResourceProvider;

  private static ParameterMatcher equalToParameter(IRI identifier, String name) {
    return new ParameterMatcher(identifier, name);
  }

  @Before
  public void setUp() {
    informationProductResourceProvider =
        new InformationProductResourceProvider(configurationBackend, backendResourceProvider,
            parameterDefinitionResourceProviderMock, applicationProperties);

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
        parameterDefinitionResourceProviderMock, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingBackendResourceProvider() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new InformationProductResourceProvider(configurationBackend, null,
        parameterDefinitionResourceProviderMock, applicationProperties);
  }

  @Test
  public void constructor_ThrowsException_WithMissingApplicationProperties() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new InformationProductResourceProvider(configurationBackend, backendResourceProvider,
        parameterDefinitionResourceProviderMock, null);
  }

  @Test
  public void loadResources_LoadInformationProduct_WithValidData() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND))));

    InformationProduct informationProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT), eq(null),
        eq(ImmutableList.of()), any())).thenReturn(informationProduct);

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
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            VALUE_FACTORY.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
            VALUE_FACTORY.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.BACKEND_PROP,
                DBEERPEDIA.BACKEND))));

    InformationProduct percentagesProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT), eq(null),
        eq(ImmutableList.of()), any())).thenReturn(percentagesProduct);

    InformationProduct originProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT), eq(null),
        eq(ImmutableList.of()), any())).thenReturn(originProduct);

    // Act
    informationProductResourceProvider.loadResources();

    // Assert
    assertThat(informationProductResourceProvider.getAll().entrySet(), hasSize(2));
  }

  @Test
  public void get_ThrowsException_ResourceNotFound_WithMultipleOtherInformationProducts() {
    IRI unknownResource = VALUE_FACTORY.createIRI(DBEERPEDIA.NAMESPACE, "foo");

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
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            VALUE_FACTORY.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
            VALUE_FACTORY.createStatement(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT, ELMO.BACKEND_PROP,
                DBEERPEDIA.BACKEND))));

    InformationProduct percentagesProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT), eq(null),
        eq(ImmutableList.of()), any())).thenReturn(percentagesProduct);

    InformationProduct originProduct = mock(InformationProduct.class);
    when(backend.createInformationProduct(eq(DBEERPEDIA.ORIGIN_INFORMATION_PRODUCT), eq(null),
        eq(ImmutableList.of()), any())).thenReturn(originProduct);

    informationProductResourceProvider.loadResources();

    // Act
    informationProductResourceProvider.get(unknownResource);
  }

  @Test
  public void loadResources_CreatesInformationProduct_WithCorrectValues() {

    // Arrange
    IRI reqParam1Id = VALUE_FACTORY.createIRI(DBEERPEDIA.NAMESPACE, "required1");
    IRI reqParam2Id = VALUE_FACTORY.createIRI(DBEERPEDIA.NAMESPACE, "required2");
    IRI optParam1Id = VALUE_FACTORY.createIRI(DBEERPEDIA.NAMESPACE, "optional1");
    IRI optParam2Id = VALUE_FACTORY.createIRI(DBEERPEDIA.NAMESPACE, "optional2");

    ShaclShape shaclShape =
        new ShaclShape(XMLSchema.STRING, VALUE_FACTORY.createLiteral("string"), ImmutableList.of());

    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDF.TYPE,
                ELMO.INFORMATION_PRODUCT),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.BACKEND_PROP, DBEERPEDIA.BACKEND),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT, RDFS.LABEL,
                DBEERPEDIA.BREWERIES_LABEL),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.REQUIRED_PARAMETER_PROP, reqParam1Id),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.REQUIRED_PARAMETER_PROP, reqParam2Id),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.OPTIONAL_PARAMETER_PROP, optParam1Id),
            VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
                ELMO.OPTIONAL_PARAMETER_PROP, optParam2Id))));

    ParameterDefinition reqParam1Def =
        new TermParameterDefinition(reqParam1Id, "reqParam1Name", shaclShape);
    when(parameterDefinitionResourceProviderMock.get(reqParam1Id)).thenReturn(reqParam1Def);

    ParameterDefinition reqParam2Def =
        new TermParameterDefinition(reqParam2Id, "reqParam2Name", shaclShape);
    when(parameterDefinitionResourceProviderMock.get(reqParam2Id)).thenReturn(reqParam2Def);

    ParameterDefinition optParam1Def =
        new TermParameterDefinition(optParam1Id, "optParam1Name", shaclShape);
    when(parameterDefinitionResourceProviderMock.get(optParam1Id)).thenReturn(optParam1Def);

    ParameterDefinition optParam2Def =
        new TermParameterDefinition(optParam2Id, "optParam2Name", shaclShape);
    when(parameterDefinitionResourceProviderMock.get(optParam2Id)).thenReturn(optParam2Def);

    ArgumentCaptor<List<Parameter>> captureParameters = ArgumentCaptor.forClass(List.class);
    InformationProduct informationProduct = mock(InformationProduct.class);

    when(backend.createInformationProduct(eq(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT),
        eq(DBEERPEDIA.BREWERIES_LABEL.stringValue()), captureParameters.capture(),
        any())).thenReturn(informationProduct);

    // Act
    informationProductResourceProvider.loadResources();

    // Assert
    assertThat(captureParameters.getValue(),
        containsInAnyOrder(equalToParameter(reqParam1Id, "reqParam1Name"),
            equalToParameter(reqParam2Id, "reqParam2Name"),
            equalToParameter(optParam1Id, "optParam1Name"),
            equalToParameter(optParam2Id, "optParam2Name")));
  }

  @Test
  public void loadResources_ThrowsException_WithMissingBackendParameter() {
    // Arrange
    when(graphQuery.evaluate()).thenReturn(new IteratingGraphQueryResult(ImmutableMap.of(),
        ImmutableList.of(VALUE_FACTORY.createStatement(DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT,
            RDF.TYPE, ELMO.INFORMATION_PRODUCT))));

    // Assert
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("No <%s> statement has been found for information product <%s>.",
            ELMO.BACKEND_PROP, DBEERPEDIA.PERCENTAGES_INFORMATION_PRODUCT));

    // Act
    informationProductResourceProvider.loadResources();
  }

  private static final class ParameterMatcher extends TypeSafeMatcher<Parameter> {

    private final IRI identifier;

    private final String name;

    private ParameterMatcher(IRI identifier, String name) {
      this.identifier = identifier;
      this.name = name;
    }

    @Override
    protected boolean matchesSafely(Parameter parameter) {
      return parameter.getIdentifier().equals(identifier) && parameter.getName().equals(name);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText(String.format("%s[identifier: '%s', name: '%s']",
          Parameter.class.getSimpleName(), identifier, name));
    }

  }

}
