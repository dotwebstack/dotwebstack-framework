package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.atlassian.oai.validator.model.ApiOperation;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestContextTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ApiOperation operationMock;

  @Mock
  private InformationProduct informationProductMock;

  private Map<String, String> parameters;

  private RequestContext requestContext;

  @Before
  public void setUp() {
    parameters = new HashMap<>();
    requestContext = new RequestContext(operationMock, informationProductMock, parameters, "");
  }

  @Test
  public void constructor_InitializesResponseParameters_AsEmpty() {
    // Assert
    assertThat(requestContext.getParameters().isEmpty(), is(true));
  }

  @Test
  public void addParameter_StoresValue_ForNewValue() {
    // Act
    requestContext.addParameter("X", "A");
    requestContext.addParameter("Y", "B");
    requestContext.addParameter("Z", "C");

    // Assert
    assertThat(requestContext.getParameters(), is(ImmutableMap.of("X", "A", "Y", "B", "Z", "C")));
  }

  @Test
  public void addParameter_OverwritesExistingValue_ForDuplicateValue() {
    // Act
    requestContext.addParameter("X", "A");
    requestContext.addParameter("X", "B");

    // Assert
    assertThat(requestContext.getParameters(), is(ImmutableMap.of("X", "B")));
  }

  @Test
  public void getParameters_ReturnsResult_AsImmutable() {
    // Assert
    thrown.expect(UnsupportedOperationException.class);

    // Arrange
    Map<String, String> result = requestContext.getParameters();

    // Act
    result.put("key", "value");
  }

}
