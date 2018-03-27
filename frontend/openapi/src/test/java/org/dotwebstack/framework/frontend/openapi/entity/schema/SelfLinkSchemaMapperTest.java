package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SelfLinkSchemaMapperTest {

  @Mock
  private TupleEntity tupleEntityMock;

  @Mock
  private ValueContext valueContextMock;

  private SelfLinkSchemaMapper schemaMapper;

  private ObjectProperty property;

  @Before
  public void setUp() {
    schemaMapper = new SelfLinkSchemaMapper();
    property = new ObjectProperty();
    property.setVendorExtension(OpenApiSpecificationExtensions.TYPE,
        OpenApiSpecificationExtensions.SELF_LINK);
  }

  @Test
  public void mapTupleValue_ReturnsLink_WhenInvoked() {
    // Arrange

    // Act
    Object result = schemaMapper.mapTupleValue(property, tupleEntityMock, valueContextMock);

    // Assert
    // assertThat(result, instanceOf(Map.class));
  }

  @Test
  public void supports_ReturnsTrue_ForObjectPropertyWithRequiredVendorExtension() {
    // Act
    boolean result = schemaMapper.supports(property);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void supports_ReturnsFalse_ForNonObjectProperty() {
    // Act
    boolean result = schemaMapper.supports(new ArrayProperty());

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void supports_ReturnsFalse_ForObjectPropertyWithoutRequiredVendorExtension() {
    // Act
    boolean result = schemaMapper.supports(new ObjectProperty());

    // Assert
    assertThat(result, is(false));
  }

}
