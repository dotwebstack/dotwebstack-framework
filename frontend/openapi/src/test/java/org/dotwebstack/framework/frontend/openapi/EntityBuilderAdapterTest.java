package org.dotwebstack.framework.frontend.openapi;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.swagger.models.properties.Property;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityBuilderAdapterTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  TupleEntityBuilder tupleEntityBuilder;

  @Mock
  Property schema;

  EntityBuilderAdapter entityBuilderAdapter;

  @Before
  public void setUp() {
    entityBuilderAdapter = new EntityBuilderAdapter(tupleEntityBuilder);
  }

  @Test
  public void buildTupleQueryResult() {
    // Arrange
    TupleQueryResult result = mock(TupleQueryResult.class);
    Object expectedEntity = new Object();
    when(tupleEntityBuilder.build(result, schema)).thenReturn(expectedEntity);

    // Act
    Object entity = entityBuilderAdapter.build(result, schema);

    // Assert
    assertThat(entity, equalTo(expectedEntity));
  }

  @Test
  public void buildGraphQueryResult() {
    // Arrange
    GraphQueryResult result = mock(GraphQueryResult.class);

    // Assert
    thrown.expect(EntityBuilderRuntimeException.class);
    thrown.expectMessage(String.format("Result type '%s' is not supported.", result.getClass()));

    // Act
    entityBuilderAdapter.build(result, schema);
  }

}
