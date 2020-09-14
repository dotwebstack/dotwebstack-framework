package org.dotwebstack.framework.service.openapi.param;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.query.GraphQlField;
import org.dotwebstack.framework.service.openapi.TestResources;
import org.dotwebstack.framework.service.openapi.exception.ParameterValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class ExpandParamHandlerTest {

  private static final String TYPE_STRING = "string";

  @Mock
  private GraphQlField graphQlField;

  @Mock
  private StringSchema schema;

  @Mock
  private Parameter parameter;

  private ExpandParamHandler paramHandler;

  @BeforeEach
  public void setup() {
    this.paramHandler = new ExpandParamHandler(TestResources.openApi());
  }

  @Test
  public void validate_doesNotThrowError_withValidExpand() throws ParameterValidationException {
    // Arrange
    when(graphQlField.getFields()).thenReturn(ImmutableList.of(GraphQlField.builder()
        .name("beers")
        .build()));

    when(graphQlField.getType()).thenReturn("Object1");

    when(parameter.getSchema()).thenReturn(schema);

    when(schema.getDefault()).thenReturn("beers");
    when(schema.getType()).thenReturn(TYPE_STRING);

    // Act & Assert
    assertDoesNotThrow(() -> paramHandler.validate(graphQlField, parameter, "/breweries"));
  }

  @Test
  public void validate_doesNotThrowError_withValidExpand_DefaultValue() throws ParameterValidationException {
    // Arrange
    when(graphQlField.getFields()).thenReturn(ImmutableList.of(GraphQlField.builder()
        .name("beers")
        .build()));

    when(graphQlField.getType()).thenReturn("Object1");

    when(parameter.getSchema()).thenReturn(schema);

    when(schema.getDefault()).thenReturn("beers");
    when(schema.getType()).thenReturn(TYPE_STRING);

    // Act & Assert
    assertDoesNotThrow(() -> paramHandler.validate(graphQlField, parameter, "/breweries"));
  }

  @Test
  public void validate_doesNotThrowError_withDefaultComposedSchemaAllOf() throws ParameterValidationException {
    // Arrange
    when(graphQlField.getFields()).thenReturn(ImmutableList.of(GraphQlField.builder()
        .name("beers")
        .type("Object4")
        .fields(ImmutableList.of(GraphQlField.builder()
            .name("ingredients")
            .build()))
        .build()));

    when(graphQlField.getType()).thenReturn("Object1");
    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getDefault()).thenReturn("beers.ingredients");
    when(schema.getType()).thenReturn(TYPE_STRING);

    // Act & Assert
    assertDoesNotThrow(() -> paramHandler.validate(graphQlField, parameter, "/breweries"));
  }

  @Test
  public void validate_doesNotThrowError_withDefaultComposedSchemaOneOf() throws ParameterValidationException {
    // Arrange
    when(graphQlField.getFields()).thenReturn(ImmutableList.of(GraphQlField.builder()
        .name("beers")
        .type("Object5")
        .fields(ImmutableList.of(GraphQlField.builder()
            .name("ingredients")
            .build()))
        .build()));

    when(graphQlField.getType()).thenReturn("Object1");
    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getDefault()).thenReturn("beers.ingredients");
    when(schema.getType()).thenReturn(TYPE_STRING);

    // Act & Assert
    assertDoesNotThrow(() -> paramHandler.validate(graphQlField, parameter, "/breweries"));
  }

  @Test
  public void validate_throwError_withInvalidExpandParam() throws ParameterValidationException {
    // Arrange
    when(graphQlField.getFields()).thenReturn(ImmutableList.of(GraphQlField.builder()
        .name("beers")
        .build()));

    when(graphQlField.getType()).thenReturn("Object1");

    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getDefault()).thenReturn("beers");
    when(schema.getType()).thenReturn(TYPE_STRING);
    when(schema.getEnum()).thenReturn(ImmutableList.of("address"));

    // Act & Assert
    assertThrows(ParameterValidationException.class,
        () -> paramHandler.validate(graphQlField, parameter, "/breweries"));
  }

  @Test
  public void validate_doesNotThrowError_withInValidNestedExpandParameter() throws ParameterValidationException {
    // Arrange
    when(graphQlField.getFields()).thenReturn(ImmutableList.of(GraphQlField.builder()
        .name("address")
        .fields(ImmutableList.of(GraphQlField.builder()
            .name("postalCode")
            .build()))
        .build()));

    when(graphQlField.getType()).thenReturn("Object1");

    when(parameter.getSchema()).thenReturn(schema);
    when(schema.getDefault()).thenReturn("beers.ingredients");
    when(schema.getType()).thenReturn(TYPE_STRING);

    // Act & Assert
    assertThrows(InvalidConfigurationException.class,
        () -> paramHandler.validate(graphQlField, parameter, "/breweries"));
  }
}
