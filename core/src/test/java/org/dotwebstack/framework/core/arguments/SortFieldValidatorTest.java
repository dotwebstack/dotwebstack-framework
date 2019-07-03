package org.dotwebstack.framework.core.arguments;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeName;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.Arrays;
import java.util.Optional;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.validators.SortFieldValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
class SortFieldValidatorTest {
  private static final String TYPE_DEF_1 = "typeDef1";

  private static final String TYPE_DEF_2 = "typeDef2";

  private static final String FIELD_NAME_1 = "field1";

  private static final String FIELD_NAME_2 = "field2";

  @Mock
  private TypeDefinitionRegistry registry;

  @Mock
  private ObjectTypeDefinition typeDefinition1;

  @Mock
  private ObjectTypeDefinition typeDefinition2;

  @Mock
  private FieldDefinition fieldDefinition1;

  @Mock
  private FieldDefinition fieldDefinition2;

  private TypeName type1 = new TypeName(TYPE_DEF_2);

  private TypeName type2 = new TypeName("somevalue");

  private ListType listType = new ListType(type1);

  private SortFieldValidator sortFieldValidator;

  @BeforeEach
  public void setup() {
    when(fieldDefinition1.getName()).thenReturn(FIELD_NAME_1);
    when(fieldDefinition1.getType()).thenReturn(type1);
    when(typeDefinition1.getFieldDefinitions()).thenReturn(Arrays.asList(fieldDefinition1));

    when(registry.getType(anyString())).thenAnswer(invocationOnMock -> {
      if (invocationOnMock.getArguments()[0].equals(TYPE_DEF_1)) {
        return Optional.of(typeDefinition1);
      } else if (invocationOnMock.getArguments()[0].equals(TYPE_DEF_2)) {
        return Optional.of(typeDefinition2);
      } else {
        return Optional.empty();
      }
    });
    sortFieldValidator = new SortFieldValidator(registry);
  }

  @Test
  public void validate_success_withOneField() {
    // Act / assert
    sortFieldValidator.validateSortFieldValue(TYPE_DEF_1, null, null, FIELD_NAME_1);
  }

  @Test
  public void validate_success_withMultipleFields() {
    // Arrange
    when(fieldDefinition2.getName()).thenReturn(FIELD_NAME_2);
    when(fieldDefinition2.getType()).thenReturn(type2);
    when(typeDefinition2.getFieldDefinitions()).thenReturn(Arrays.asList(fieldDefinition2));

    // Act / Assert
    sortFieldValidator.validateSortFieldValue(TYPE_DEF_1, null, null, FIELD_NAME_1 + "." + FIELD_NAME_2);
  }

  @Test
  public void validate_error_withUnknownField() {
    // Act

    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> sortFieldValidator.validateSortFieldValue(TYPE_DEF_1, null, null, FIELD_NAME_1 + ".UNKNOWN"));

    // Assert
    assertTrue(thrown.getMessage()
        .contains("has no Field"));
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void validate_error_withUnknownType() {
    // Act
    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> sortFieldValidator.validateSortFieldValue("UNKNOWN", null, null, FIELD_NAME_1));

    // Assert
    assertTrue(thrown.getMessage()
        .contains("not found"));
  }

  @Test
  @MockitoSettings(strictness = Strictness.LENIENT)
  public void validate_error_withFieldListType() {
    // Arrange
    when(fieldDefinition1.getType()).thenReturn(listType);

    // Act
    InvalidConfigurationException thrown = assertThrows(InvalidConfigurationException.class,
        () -> sortFieldValidator.validateSortFieldValue(TYPE_DEF_1, null, null, FIELD_NAME_1));

    // Assert
    assertTrue(thrown.getMessage()
        .contains("is a List"));
  }
}
