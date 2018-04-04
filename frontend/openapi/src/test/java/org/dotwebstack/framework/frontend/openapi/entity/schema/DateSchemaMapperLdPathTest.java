package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.DateProperty;
import java.time.LocalDate;
import java.util.Collections;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DateSchemaMapperLdPathTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
  private static final Literal VALUE_1 = VALUE_FACTORY.createLiteral("2016-12-24", XMLSchema.DATE);

  private DateSchemaMapper schemaMapper;

  private DateProperty property;

  @Mock
  private GraphEntity entityMock;

  @Mock
  private Value subjectMock;

  private SchemaMapperAdapter mapperAdapter;

  @Mock
  private LdPathExecutor ldPathExecutorMock;

  @Before
  public void setUp() {
    schemaMapper = new DateSchemaMapper();
    property = new DateProperty();

    mapperAdapter = new SchemaMapperAdapter(Collections.singletonList(schemaMapper));

    when(entityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValueTest() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "ld-path"));
    Literal literal = VALUE_FACTORY.createLiteral("1982-11-25", XMLSchema.DATE);

    when(ldPathExecutorMock.ldPathQuery(subjectMock, "ld-path")).thenReturn(
        ImmutableList.of(literal));

    // Act
    LocalDate result = schemaMapper.mapGraphValue(property, entityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);

    // Assert
    assertThat(result, is(LocalDate.of(1982, 11, 25)));
  }

  @Test
  public void mapGraphValue_ReturnsNull_WhenNoLdPathHasBeenSupplied() {
    // Act
    LocalDate result = schemaMapper.mapGraphValue(property, entityMock,
        ValueContext.builder().value(VALUE_1).build(), mapperAdapter);

    // Assert
    assertThat(result, nullValue());
    verifyZeroInteractions(ldPathExecutorMock);
  }


  @Test
  public void mapGraphValue_ReturnsValue_ForLdPath() {
    // Arrange
    property.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(eq(subjectMock), anyString())).thenReturn(
        ImmutableList.of(VALUE_1));

    LocalDate result = schemaMapper.mapGraphValue(property, entityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);

    // Assert
    assertThat(result.toString(), CoreMatchers.is(VALUE_1.calendarValue().toString()));
  }
}
