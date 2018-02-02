package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GraphEntityContextTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private InformationProduct productMock;

  private GraphEntityContext context;

  @Before
  public void setUp() {
    context = new GraphEntityContext(ImmutableMap.of(), ImmutableMap.of(), new LinkedHashModel(),
        ImmutableMap.of(), productMock);
  }

  @Test
  public void constructor_InitializesResponseParameters_AsEmpty() {
    // Assert
    assertThat(context.getResponseParameters().isEmpty(), is(true));
  }

  @Test
  public void addResponseParameter_StoresValue_ForNewValue() {
    // Act
    context.addResponseParameter("X", "A");
    context.addResponseParameter("Y", "B");
    context.addResponseParameter("Z", "C");

    // Assert
    assertThat(context.getResponseParameters(), is(ImmutableMap.of("X", "A", "Y", "B", "Z", "C")));
  }

  @Test
  public void addResponseParameter_OverwritesExistingValue_ForDuplicateValue() {
    // Act
    context.addResponseParameter("X", "A");
    context.addResponseParameter("X", "B");

    // Assert
    assertThat(context.getResponseParameters(), is(ImmutableMap.of("X", "B")));
  }

  @Test
  public void getResponseParameters_ReturnsResult_AsImmutable() {
    // Assert
    thrown.expect(UnsupportedOperationException.class);

    // Arrange
    Map<String, String> result = context.getResponseParameters();

    // Act
    result.put("key", "value");
  }

}
