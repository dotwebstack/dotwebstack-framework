package org.dotwebstack.framework.frontend.ld.mappers;


import static javax.ws.rs.HttpMethod.GET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.frontend.http.HttpConfiguration;
import org.dotwebstack.framework.frontend.http.site.Site;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.framework.frontend.ld.SupportedMediaTypesScanner;
import org.dotwebstack.framework.frontend.ld.redirection.Redirection;
import org.dotwebstack.framework.frontend.ld.redirection.RedirectionResourceProvider;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdRedirectionRequestMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Stage stage;

  @Mock
  private Site site;

  @Mock
  private Redirection redirection;

  @Mock
  private RedirectionResourceProvider redirectionResourceProvider;

  @Mock
  private SupportedMediaTypesScanner supportedMediaTypesScanner;

  private LdRedirectionRequestMapper ldRedirectionRequestMapper;

  private HttpConfiguration httpConfiguration;

  @Before
  public void setUp() {
    site = new Site.Builder(DBEERPEDIA.BREWERIES).domain(DBEERPEDIA.DOMAIN.stringValue()).build();

    stage = new Stage.Builder(DBEERPEDIA.BREWERIES, site).basePath(
        DBEERPEDIA.BASE_PATH.stringValue()).build();

    Redirection redirection = new Redirection.Builder(DBEERPEDIA.ID2DOC_REDIRECTION, stage,
        DBEERPEDIA.ID2DOC_URL_PATTERN.stringValue(),
        DBEERPEDIA.ID2DOC_REDIRECTION_TEMPLATE.stringValue()).build();

    Map<IRI, Redirection> redirectionMap = new HashMap<>();
    redirectionMap.put(redirection.getIdentifier(), redirection);

    when(redirectionResourceProvider.getAll()).thenReturn(redirectionMap);

    ldRedirectionRequestMapper = new LdRedirectionRequestMapper(redirectionResourceProvider);

    httpConfiguration = new HttpConfiguration(ImmutableList.of());
  }

  @Test
  public void constructor_DoesNotThrowExceptions_WithValidData() {
    // Arrange / Act
    LdRedirectionRequestMapper ldRedirectionRequestMapper =
        new LdRedirectionRequestMapper(redirectionResourceProvider);

    // Assert
    assertThat(ldRedirectionRequestMapper, not(nullValue()));
  }

  @Test
  public void loadRepresentations_MapRepresentation_WithValidData() {
    // Act
    ldRedirectionRequestMapper.loadRedirections(httpConfiguration);

    // Assert
    Resource resource = (Resource) httpConfiguration.getResources().toArray()[0];
    ResourceMethod method = resource.getResourceMethods().get(0);
    assertThat(httpConfiguration.getResources(), hasSize(1));
    assertThat(resource.getPath(), equalTo("{any: dbeerpedia.org\\/special\\/id\\/(.+)$}"));
    assertThat(resource.getResourceMethods(), hasSize(1));
    assertThat(method.getHttpMethod(), equalTo(GET));
  }

}
