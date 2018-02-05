package org.dotwebstack.framework.frontend.ld.parameter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.frontend.ld.parameter.source.RequestUriParameterSource;
import org.dotwebstack.framework.frontend.ld.parameter.target.ParameterTarget;
import org.dotwebstack.framework.frontend.ld.parameter.target.Target;
import org.dotwebstack.framework.param.ParameterDefinition;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UriParameterMapperTest {

  @Mock
  private ParameterDefinition parameterDefinition;

  private RequestUriParameterSource requestUriParameterSource;

  private Target parameterTarget;

  @Mock
  ContainerRequestContext containerRequestContext;

  @Mock
  UriInfo uriInfo;

  URI uri;

  @Before
  public void setUp() {
    // Arange
    uri = URI.create("http://" + DBEERPEDIA.ORG_HOST + DBEERPEDIA.BREWERY_DOC_PATH);

    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getAbsolutePath()).thenReturn(uri);

    requestUriParameterSource = new RequestUriParameterSource();
    parameterTarget = new ParameterTarget(parameterDefinition);
    when(parameterDefinition.getName()).thenReturn(DBEERPEDIA.SUBJECT_PARAMETER_NAME);
  }

  @Test
  public void build_CreateUriParameterMapper_WithSourceAndTarget() {
    // Act
    final UriParameterMapper uriParameterMapper =
        new UriParameterMapper.UriParameterMapperBuilder(DBEERPEDIA.SUBJECT_FROM_URL,
            requestUriParameterSource, parameterTarget).build();

    Map<String, String> map = uriParameterMapper.map(containerRequestContext);

    // Assert
    assertThat(map.keySet().toArray()[0], equalTo(DBEERPEDIA.SUBJECT_PARAMETER_NAME));
    assertThat(map.values().toArray()[0],
        equalTo(uri.getScheme() + "://" + uri.getHost() + uri.getPath()));
  }

  @Test
  public void build_CreateUriParameterMapper_WithPatternAndTemplate() {
    // Act
    final UriParameterMapper uriParameterMapper =
        new UriParameterMapper.UriParameterMapperBuilder(DBEERPEDIA.SUBJECT_FROM_URL,
            requestUriParameterSource, parameterTarget).pattern(
                DBEERPEDIA.SUBJECT_FROM_PATH_PATTERN.stringValue()).template(
                    DBEERPEDIA.SUBJECT_FROM_URL_TEMPLATE.stringValue()).build();

    Map<String, String> map = uriParameterMapper.map(containerRequestContext);

    // Assert
    assertThat(map.keySet().toArray()[0], equalTo(DBEERPEDIA.SUBJECT_PARAMETER_NAME));
    assertThat(map.values().toArray()[0],
        equalTo(uri.getScheme() + "://" + uri.getHost() + DBEERPEDIA.BREWERY_ID_PATH));
  }

}
