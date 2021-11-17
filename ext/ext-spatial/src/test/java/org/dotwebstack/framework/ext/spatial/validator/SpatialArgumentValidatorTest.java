package org.dotwebstack.framework.ext.spatial.validator;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLObjectType;
import graphql.schema.SelectedField;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpatialArgumentValidatorTest {

  @Mock
  private DataFetchingEnvironment environment;

  @Test
  void validate_doesNotThrowException_whenNoArguments() {
    mockSelectedField(Map.of());

    new SpatialArgumentValidator().validate(environment);
  }

  @Test
  void validate_doesNotThrowException_whenTypeAndBboxArguments() {
    mockSelectedField(Map.of("type", "POLYGON", "bbox", false));

    new SpatialArgumentValidator().validate(environment);
  }

  @Test
  void validate_doesNotThrowException_whenOnlyTypeArgument() {
    mockSelectedField(Map.of("type", "POLYGON"));

    new SpatialArgumentValidator().validate(environment);
  }

  @Test
  void validate_doesNotThrowException_whenOnlyBoxArgument() {
    mockSelectedField(Map.of("bbox", false));

    new SpatialArgumentValidator().validate(environment);
  }

  @Test
  void validate_doesThrowException_whenTypeAndBboxArguments() {
    var field = mockSelectedField(Map.of("type", "POLYGON", "bbox", true));
    when(field.getQualifiedName()).thenReturn("geometry");

    SpatialArgumentValidator spatialArgumentValidator = new SpatialArgumentValidator();
    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> spatialArgumentValidator.validate(environment));

    assertThat(exception.getMessage(), is("Type argument is not allowed when argument bbox is true (geometry)."));
  }

  private SelectedField mockSelectedField(Map<String, Object> arguments) {
    DataFetchingFieldSelectionSet selectionSet = mock(DataFetchingFieldSelectionSet.class);

    var type = new GraphQLObjectType.Builder().name("Geometry")
        .build();
    SelectedField field = mock(SelectedField.class);
    when(field.getType()).thenReturn(type);
    when(field.getArguments()).thenReturn(arguments);

    when(selectionSet.getFields("**")).thenReturn(List.of(field));
    when(environment.getSelectionSet()).thenReturn(selectionSet);

    return field;
  }
}
