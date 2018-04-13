package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.swagger.models.properties.Property;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
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

  @Mock
  private GraphEntity entityMock;

  @Mock
  private Property schemaMock;

  private final AbstractSubjectSchemaMapper mapper = new TestSubjectSchemaMapper();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void hasSubjectQueryVendorExt_ReturnsFalse_WhenPropDoesNotHaveSubjectQueryVendorExt() {
    // Arrange
    when(schemaMock.getVendorExtensions()).thenReturn(ImmutableMap.of());

    // Act
    boolean result = mapper.hasSubjectVendorExtension(schemaMock);

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void hasSubjectQueryVendorExt_ReturnsTrue_WhenPropDoesHaveSubjectQueryVendorExt() {
    // Arrange
    when(schemaMock.getVendorExtensions()).thenReturn(
        ImmutableMap.of(OpenApiSpecificationExtensions.SUBJECT, true));

    // Act
    boolean result = mapper.hasSubjectVendorExtension(schemaMock);

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void getSubject_ReturnsNull_ForOptionalPropertyWithZeroSubjects() {
    // Arrange
    when(schemaMock.getRequired()).thenReturn(false);
    when(entityMock.getSubjects()).thenReturn(ImmutableSet.of());

    // Act
    Value subject = mapper.getSubject(schemaMock, entityMock);

    // Assert
    assertThat(subject, is(nullValue()));
  }

  @Test
  public void getSubject_ReturnsSubject_ForPropertyWithOneSubject() {
    // Arrange
    when(entityMock.getSubjects()).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN));

    // Act
    Value subject = mapper.getSubject(schemaMock, entityMock);

    // Assert
    assertThat(subject, equalTo(DBEERPEDIA.BROUWTOREN));
  }

  @Test
  public void getSubject_ThrowsException_ForRequiredPropertyWithZeroSubjects() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Expected a single subject, but subject query yielded no results.");

    // Arrange
    when(schemaMock.getRequired()).thenReturn(true);
    when(entityMock.getSubjects()).thenReturn(ImmutableSet.of());

    // Act
    mapper.getSubject(schemaMock, entityMock);
  }

  @Test
  public void getSubject_ThrowsException_ForPropertyWithMultipleSubjects() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Expected a single subject, but subject query yielded multiple results.");

    // Arrange
    when(entityMock.getSubjects()).thenReturn(
        ImmutableSet.of(DBEERPEDIA.BROUWTOREN, DBEERPEDIA.MAXIMUS));

    // Act
    mapper.getSubject(schemaMock, entityMock);
  }

  private static class TestSubjectSchemaMapper extends AbstractSubjectSchemaMapper {

    @Override
    public Object mapTupleValue(Property schema, TupleEntity entity, ValueContext valueContext) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object mapGraphValue(Property schema, GraphEntity entity, ValueContext valueContext,
        SchemaMapperAdapter schemaMapperAdapter) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean supports(Property schema) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected Set getSupportedDataTypes() {
      throw new UnsupportedOperationException();
    }

  }

}
