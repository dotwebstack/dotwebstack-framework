package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.DateTimeProperty;
import java.time.ZonedDateTime;
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
public class DateTimeSchemaMapperLdPathTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final String EXPECTED_LOCAL_DATE_TIME = "1982-11-25T10:10:10+01:00";
  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
  private static final Literal VALUE =
      SimpleValueFactory.getInstance().createLiteral(EXPECTED_LOCAL_DATE_TIME, XMLSchema.DATETIME);

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Value valueMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private DateTimeSchemaMapper dateTimeSchemaMapper;
  private DateTimeProperty dateTimeProperty;
  private SchemaMapperAdapter schemaMapperAdapter;

  @Before
  public void setUp() {
    dateTimeSchemaMapper = new DateTimeSchemaMapper();
    dateTimeProperty = new DateTimeProperty();

    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(dateTimeSchemaMapper));

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_returnsZonedlDateTime__WhenSupportedLiteralLdPathIsDefined() {
    // Arrange
    dateTimeProperty.setVendorExtensions(
        ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "ld-path"));
    Literal literal = VALUE_FACTORY.createLiteral(EXPECTED_LOCAL_DATE_TIME, XMLSchema.DATETIME);

    when(ldPathExecutorMock.ldPathQuery(valueMock, "ld-path")).thenReturn(
        ImmutableList.of(literal));

    // Act
    ZonedDateTime result = dateTimeSchemaMapper.mapGraphValue(dateTimeProperty, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE_TIME));
  }

  @Test
  public void mapGraphValue_ReturnsZonedDateTime_ForLdPath() {
    // Arrange
    dateTimeProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(eq(valueMock), anyString())).thenReturn(
        ImmutableList.of(VALUE));

    ZonedDateTime result = dateTimeSchemaMapper.mapGraphValue(dateTimeProperty, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE_TIME));
  }
}
