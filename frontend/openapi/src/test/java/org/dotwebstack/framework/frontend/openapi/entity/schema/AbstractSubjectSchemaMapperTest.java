package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.v3.oas.models.media.Schema;
import java.util.HashSet;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("static-access")
public class AbstractSubjectSchemaMapperTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private GraphEntity graphEntityMock;

  @Mock
  private Schema schemaMock;

  private final AbstractSubjectSchemaMapper abstractSubjectSchemaMapper =
      new TestSubjectSchemaMapper();

  @Test
  public void hasSubjectQueryVendorExt_ReturnsFalse_WhenPropDoesNotHaveSubjectQueryVendorExt() {
    // Arrange
    when(schemaMock.getExtensions()).thenReturn(ImmutableMap.of());

    // Act
    boolean result = abstractSubjectSchemaMapper.hasSubjectVendorExtension(schemaMock);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasSubjectQueryVendorExt_ReturnsTrue_WhenPropDoesHaveSubjectQueryVendorExt() {
    // Arrange
    when(schemaMock.getExtensions()).thenReturn(
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT, true));

    // Act
    boolean result = abstractSubjectSchemaMapper.hasSubjectVendorExtension(schemaMock);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void getSubject_ReturnsNull_ForOptionalPropertyWithZeroSubjects() {
    // Arrange
    when(graphEntityMock.getSubjects()).thenReturn(ImmutableSet.of());

    // Act
    Value subject = abstractSubjectSchemaMapper.getSubject(graphEntityMock, false);

    // Assert
    assertThat(subject, is(nullValue()));
  }

  @Test
  public void getSubject_ReturnsSubject_ForPropertyWithOneSubject() {
    // Arrange
    when(graphEntityMock.getSubjects()).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN));

    // Act
    Value subject = abstractSubjectSchemaMapper.getSubject(graphEntityMock, false);

    // Assert
    assertThat(subject, equalTo(DBEERPEDIA.BROUWTOREN));
  }

  @Test
  public void getSubject_ThrowsException_ForRequiredPropertyWithZeroSubjects() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Expected a single subject, but subject query yielded 0 results.");

    // Arrange
    when(graphEntityMock.getSubjects()).thenReturn(ImmutableSet.of());

    // Act
    abstractSubjectSchemaMapper.getSubject(graphEntityMock, true);
  }

  @Test
  public void getSubject_ThrowsException_ForPropertyWithMultipleSubjects() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Expected a single subject, but subject query yielded 2 results.");

    // Arrange
    when(graphEntityMock.getSubjects()).thenReturn(
        ImmutableSet.of(DBEERPEDIA.BROUWTOREN, DBEERPEDIA.MAXIMUS));

    // Act
    abstractSubjectSchemaMapper.getSubject(graphEntityMock, false);
  }

  private static class TestSubjectSchemaMapper extends AbstractSubjectSchemaMapper {

    @Override
    protected Set<String> getSupportedVendorExtensions() {
      return new HashSet<>();
    }

    @Override
    public Object mapTupleValue(Schema schema, TupleEntity entity, ValueContext valueContext) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object mapGraphValue(Schema schema, boolean required, GraphEntity entity,
        ValueContext valueContext, SchemaMapperAdapter schemaMapperAdapter) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Object convertLiteralToType(Literal literal) {
      return literal;
    }

    @Override
    public boolean supports(Schema schema) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Set getSupportedDataTypes() {
      throw new UnsupportedOperationException();
    }

  }

}
