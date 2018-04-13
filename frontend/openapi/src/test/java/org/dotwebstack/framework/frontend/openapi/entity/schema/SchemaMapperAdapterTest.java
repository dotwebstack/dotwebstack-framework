package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchemaMapperAdapterTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private SchemaMapper<StringProperty, String> stringSchemaMapperMock;
  @Mock
  private TupleEntity tupleEntityMock;

  private SchemaMapperAdapter schemaMapperAdapter;

  @Before
  public void setUp() {
    schemaMapperAdapter = new SchemaMapperAdapter(ImmutableList.of(stringSchemaMapperMock));
  }

  @Test
  public void mapTupleValue_ThrowsException_WhenNoSupportingMapperFound() {
    // Arrange
    IntegerProperty integerProperty = new IntegerProperty();
    when(stringSchemaMapperMock.supports(integerProperty)).thenReturn(false);

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format("No schema mapper available for '%s'.",
        integerProperty.getClass().getName()));

    // Act
    schemaMapperAdapter.mapTupleValue(integerProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build());
  }

  @Test
  public void mapTupleValue_ReturnsHandledValue_WhenSupportingMapperFound() {
    // Arrange
    StringProperty stringProperty = new StringProperty();
    String expectedValue = DBEERPEDIA.BROUWTOREN_NAME.stringValue();
    when(stringSchemaMapperMock.supports(stringProperty)).thenReturn(true);
    when(stringSchemaMapperMock.mapTupleValue(any(StringProperty.class), any(TupleEntity.class),
        any(ValueContext.class))).thenReturn(expectedValue);

    // Act
    Object value = schemaMapperAdapter.mapTupleValue(stringProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_NAME).build());

    // Assert
    assertThat(value, equalTo(expectedValue));
  }
}
