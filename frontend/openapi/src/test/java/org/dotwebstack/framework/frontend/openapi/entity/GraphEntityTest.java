package org.dotwebstack.framework.frontend.openapi.entity;

import static org.dotwebstack.framework.frontend.openapi.entity.GraphEntity.newGraphEntity;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.Swagger;
import java.util.Map;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.QueryResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GraphEntityTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private InformationProduct productMock;

  @Mock
  private QueryResult<Statement> queryResultMock;

  @Mock
  private Swagger definitionsMock;

  private GraphEntity entity;

  @Before
  public void setUp() {
    entity = newGraphEntity(ImmutableMap.of(), queryResultMock, definitionsMock,
        ImmutableMap.of(), productMock);
  }

  @Test
  public void constructor_InitializesResponseParameters_AsEmpty() {
    // Assert
    assertThat(entity.getResponseParameters().isEmpty(), is(true));
  }

  @Test
  public void addResponseParameter_StoresValue_ForNewValue() {
    // Act
    entity.addResponseParameter("X", "A");
    entity.addResponseParameter("Y", "B");
    entity.addResponseParameter("Z", "C");

    // Assert
    assertThat(entity.getResponseParameters(), is(ImmutableMap.of("X", "A", "Y", "B", "Z", "C")));
  }

  @Test
  public void addResponseParameter_OverwritesExistingValue_ForDuplicateValue() {
    // Act
    entity.addResponseParameter("X", "A");
    entity.addResponseParameter("X", "B");

    // Assert
    assertThat(entity.getResponseParameters(), is(ImmutableMap.of("X", "B")));
  }

  @Test
  public void getResponseParameters_ReturnsResult_AsImmutable() {
    // Assert
    thrown.expect(UnsupportedOperationException.class);

    // Arrange
    Map<String, String> result = entity.getResponseParameters();

    // Act
    result.put("key", "value");
  }

}
