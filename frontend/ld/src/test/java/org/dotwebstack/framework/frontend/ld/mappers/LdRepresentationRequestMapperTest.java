package org.dotwebstack.framework.frontend.ld.mappers;

import com.google.common.collect.ImmutableList;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.SupportedReaderMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.SupportedWriterMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.handlers.TransactionRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.TransactionRequestHandlerFactory;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.frontend.ld.representation.RepresentationResourceProvider;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.transaction.Transaction;
import org.dotwebstack.framework.transaction.flow.Flow;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdRepresentationRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Stage stage;

  @Mock
  private Site site;

  @Mock
  private InformationProduct informationProduct;

  @Mock
  private Transaction transaction;

  @Mock
  private Flow flow;

  @Mock
  private Representation representation;

  @Mock
  private RepresentationResourceProvider representationResourceProvider;

  @Mock
  private SupportedWriterMediaTypesScanner supportedWriterMediaTypesScanner;

  @Mock
  private SupportedReaderMediaTypesScanner supportedReaderMediaTypesScanner;
  @Mock
  private LdRepresentationRequestMapper ldRepresentationRequestMapper;

  // @Mock
  // private RepresentationRequestParameterMapper representationRequestParameterMapper;
  //
  // private RepresentationRequestHandler representationRequestHandler;

  private TransactionRequestHandler transactionRequestHandler;

  // @Mock
  // private RepresentationRequestHandlerFactory representationRequestHandlerFactory;

  @Mock
  private TransactionRequestHandlerFactory transactionRequestHandlerFactory;

  private HttpConfiguration httpConfiguration;

  @Before
  public void setUp() {
    // site = new
    // Site.Builder(DBEERPEDIA.BREWERIES).domain(DBEERPEDIA.DOMAIN.stringValue()).build();
    //
    // stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
    // DBEERPEDIA.BASE_PATH.stringValue()).build();
    //
    // representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
    // informationProduct).stage(stage).pathPattern(DBEERPEDIA.PATH_PATTERN_VALUE).build();
    // Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    // representationMap.put(representation.getIdentifier(), representation);
    //
    // when(representationResourceProvider.getAll()).thenReturn(representationMap);
    //
    // transaction = new Transaction.Builder(DBEERPEDIA.TRANSACTION).flow(flow).build();
    //
    // representationRequestHandler =
    // new RepresentationRequestHandler(representation, representationRequestParameterMapper);
    // transactionRequestHandler = new TransactionRequestHandler(transaction);
    // ldRepresentationRequestMapper = new LdRepresentationRequestMapper(
    // representationResourceProvider, supportedWriterMediaTypesScanner,
    // supportedReaderMediaTypesScanner, representationRequestHandlerFactory,
    // transactionRequestHandlerFactory);
    // when(representationRequestHandlerFactory.newRepresentationRequestHandler(
    // isA(Representation.class))).thenReturn(representationRequestHandler);

    httpConfiguration = new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_DoesNotThrowExceptions_WithValidData() {
    // // Arrange / Act
    // LdRepresentationRequestMapper ldRepresentationRequestMapper = new
    // LdRepresentationRequestMapper(
    // representationResourceProvider, supportedWriterMediaTypesScanner,
    // supportedReaderMediaTypesScanner, representationRequestHandlerFactory,
    // transactionRequestHandlerFactory);
    //
    // // Assert
    // assertThat(ldRepresentationRequestMapper, not(nullValue()));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithValidData() {
    // // Arrange
    // when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
    // new MediaType[] {MediaType.valueOf("text/turtle")});
    //
    // // Act
    // ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);
    //
    // // Assert
    // Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    // final ResourceMethod method = resource.getResourceMethods().get(0);
    // assertThat(httpConfiguration.getResources(), hasSize(1));
    // assertThat(resource.getPath(), equalTo("/" + DBEERPEDIA.ORG_HOST
    // + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));
    // assertThat(resource.getResourceMethods(), hasSize(1));
    // assertThat(method.getHttpMethod(), equalTo(HttpMethod.GET));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithoutStage() {
    // // Arrange
    // representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
    // informationProduct).pathPattern(DBEERPEDIA.PATH_PATTERN_VALUE).build();
    // Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    // representationMap.put(representation.getIdentifier(), representation);
    // when(representationResourceProvider.getAll()).thenReturn(representationMap);
    //
    // // Act
    // ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);
    //
    // // Assert
    // assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithNullStage() {
    // // Arrange
    // representation = new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
    // informationProduct).pathPattern(DBEERPEDIA.PATH_PATTERN_VALUE).stage(null).build();
    // Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    // representationMap.put(representation.getIdentifier(), representation);
    // when(representationResourceProvider.getAll()).thenReturn(representationMap);
    //
    // // Act
    // ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);
    //
    // // Assert
    // assertThat(httpConfiguration.getResources(), hasSize(0));
  }

  @Test
  public void loadRepresentations_IgnoreSecondRepresentation_WhenAddedTwice() {
    // // Arrange
    // when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
    // new MediaType[] {MediaType.valueOf("text/turtle")});
    //
    // Representation representation =
    // new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
    // informationProduct).pathPattern(DBEERPEDIA.PATH_PATTERN_VALUE).stage(stage).build();
    // Representation samePathRepresentation =
    // new Representation.Builder(DBEERPEDIA.GRAPH_BREWERY_LIST_REPRESENTATION).informationProduct(
    // informationProduct).pathPattern(DBEERPEDIA.PATH_PATTERN_VALUE).stage(stage).build();
    // Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    // representationMap.put(representation.getIdentifier(), representation);
    // representationMap.put(samePathRepresentation.getIdentifier(), samePathRepresentation);
    // when(representationResourceProvider.getAll()).thenReturn(representationMap);
    //
    // // Act
    // ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);
    //
    // // Assert
    // assertThat(httpConfiguration.getResources(), hasSize(1));
  }

  @Test
  public void loadRepresentations_UsesPathDomainParameter_WithMatchAllDomain() {
    // // Arrange
    // when(supportedWriterMediaTypesScanner.getMediaTypes(any())).thenReturn(
    // new MediaType[] {MediaType.valueOf("text/turtle")});
    //
    // Site site = new Site.Builder(DBEERPEDIA.BREWERIES).build();
    // Stage stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
    // DBEERPEDIA.BASE_PATH.stringValue()).build();
    // Representation representation =
    // new Representation.Builder(DBEERPEDIA.BREWERIES).informationProduct(
    // informationProduct).pathPattern(DBEERPEDIA.PATH_PATTERN_VALUE).stage(stage).build();
    //
    // Map<org.eclipse.rdf4j.model.Resource, Representation> representationMap = new HashMap<>();
    // representationMap.put(representation.getIdentifier(), representation);
    // when(representationResourceProvider.getAll()).thenReturn(representationMap);
    //
    // // Act
    // ldRepresentationRequestMapper.loadRepresentations(httpConfiguration);
    //
    // // Assert
    // assertThat(httpConfiguration.getResources(), hasSize(1));
    // Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    // assertThat(resource.getPath(), equalTo("/" + Stage.PATH_DOMAIN_PARAMETER
    // + DBEERPEDIA.BASE_PATH.getLabel() + DBEERPEDIA.PATH_PATTERN_VALUE));

  }

}
