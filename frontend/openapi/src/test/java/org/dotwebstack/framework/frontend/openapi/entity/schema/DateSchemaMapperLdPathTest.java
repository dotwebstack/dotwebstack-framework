package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
  private static final String EXPECTED_LOCAL_DATE = "1982-11-25";
  private static final Literal VALUE =
      VALUE_FACTORY.createLiteral(EXPECTED_LOCAL_DATE, XMLSchema.DATE);

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Value valueMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private DateSchemaMapper dateSchemaMapper;
  private DateProperty dateProperty;
  private SchemaMapperAdapter schemaMapperAdapter;

  @Before
  public void setUp() {
    dateSchemaMapper = new DateSchemaMapper();
    dateProperty = new DateProperty();

    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(dateSchemaMapper));

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_ReturnsLocalDate_ForLdPath() {
    // Arrange
    dateProperty.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "ld-path"));
    Literal literal = VALUE_FACTORY.createLiteral(EXPECTED_LOCAL_DATE, XMLSchema.DATE);

    when(ldPathExecutorMock.ldPathQuery(valueMock, "ld-path")).thenReturn(
        ImmutableList.of(literal));

    // Act
    LocalDate result = dateSchemaMapper.mapGraphValue(dateProperty, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE));
  }

  @Test
  public void mapGraphValue_ReturnsLocalDate_WhenSupportedLiteralLdPathIsDefined() {
    // Arrange
    dateProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(eq(valueMock), anyString())).thenReturn(
        ImmutableList.of(VALUE));

    LocalDate result = dateSchemaMapper.mapGraphValue(dateProperty, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(VALUE.calendarValue().toString()));
  }
}
