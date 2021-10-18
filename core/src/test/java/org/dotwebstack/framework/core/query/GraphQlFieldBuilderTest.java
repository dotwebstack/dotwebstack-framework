package org.dotwebstack.framework.core.query;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.language.FieldDefinition;
import graphql.language.InputValueDefinition;
import graphql.language.NonNullType;
import graphql.language.Type;
import graphql.language.TypeDefinition;
import graphql.language.TypeName;
import graphql.language.Value;
import graphql.schema.idl.TypeDefinitionRegistry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.dotwebstack.framework.core.config.TypeUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphQlFieldBuilderTest {

  @Mock
  private TypeDefinitionRegistry registry;

  private GraphQlFieldBuilder builder;

  @BeforeEach
  void doBefore() {
    builder = new GraphQlFieldBuilder(registry);
  }

  @Test
  void toGraphQlField_returnsGraphQlField() {
    TypeName baseType = mock(TypeName.class);
    when(baseType.getName()).thenReturn("anyName");

    NonNullType nonNullTypeMock = mock(NonNullType.class);
    when(nonNullTypeMock.getType()).thenReturn(baseType);

    InputValueDefinition inputValueDefinitionMock = mock(InputValueDefinition.class);
    when(inputValueDefinitionMock.getName()).thenReturn("someName");
    when(inputValueDefinitionMock.getType()).thenReturn(nonNullTypeMock);
    when(inputValueDefinitionMock.getDefaultValue()).thenReturn(mock(Value.class));

    FieldDefinition.Builder childFieldDefinition = createFieldDefinition();
    TypeName childMock = mock(TypeName.class);
    when(childMock.getName()).thenReturn("aa");
    childFieldDefinition.type(childMock);
    List<FieldDefinition> children = List.of(childFieldDefinition.build());

    TypeDefinition typeDefinitionMock = mock(TypeDefinition.class);
    when(typeDefinitionMock.getChildren()).thenReturn(children);
    when(registry.getType(any(Type.class))).thenReturn(Optional.of(typeDefinitionMock));

    FieldDefinition.Builder fieldDefinition2 = createFieldDefinition();
    fieldDefinition2.inputValueDefinitions(List.of(inputValueDefinitionMock));
    fieldDefinition2.type(nonNullTypeMock);

    GraphQlField graphQlFieldMock = mock(GraphQlField.class);
    Map<String, GraphQlField> map = Map.of("aa", graphQlFieldMock);

    var result = builder.toGraphQlField(fieldDefinition2.build(), map);
    assertThat(result, CoreMatchers.is(notNullValue()));
    assertTrue(result instanceof GraphQlField);
  }

  @Test
  void toGraphQlField_throwsException_MissingType() {
    GraphQlFieldBuilder builder = new GraphQlFieldBuilder(this.registry);
    FieldDefinition.Builder fieldDefinition = createFieldDefinition();

    Map<String, GraphQlField> typeNameFieldMap = new HashMap<>();
    assertThrows(InvalidConfigurationException.class,
        () -> builder.toGraphQlField(fieldDefinition.build(), typeNameFieldMap));
  }

  private FieldDefinition.Builder createFieldDefinition() {
    return FieldDefinition.newFieldDefinition()
        .name("founded")
        .type(TypeUtils.newNonNullableType("DateTime"));
  }
}
