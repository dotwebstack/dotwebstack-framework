package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.DateTimeProperty;
import java.time.ZonedDateTime;
import java.util.Collections;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.eclipse.rdf4j.model.Literal;
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
public class DateTimeSchemaMapperConstantValueTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String CONSTANT_VALUE = OpenApiSpecificationExtensions.CONSTANT_VALUE;
  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
  private static final String EXPECTED_LOCAL_DATE_TIME = "1982-11-25T10:10:10+01:00";

  @Mock
  private GraphEntity graphEntityMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private DateTimeProperty dateTimeProperty;
  private ValueContext valueContext;

  @Before
  public void setUp() {
    dateTimeProperty = new DateTimeProperty();
    schemaMapperAdapter =
        new SchemaMapperAdapter(Collections.singletonList(new DateTimeSchemaMapper()));
    valueContext = ValueContext.builder().build();
  }

  @Test
  public void mapGraphValue_ReturnsLocalDateTime_WhenStringConstantValueIsDefined() {
    // Arrange
    dateTimeProperty.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, EXPECTED_LOCAL_DATE_TIME));

    // Act
    ZonedDateTime result = (ZonedDateTime) schemaMapperAdapter.mapGraphValue(dateTimeProperty,
        graphEntityMock, valueContext, schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE_TIME));
  }

  @Test
  public void mapGraphValue_ReturnsLocalDateTime_WhenSupportedLiteralConstantValueIsDefined() {
    // Arrange
    Literal literal = VALUE_FACTORY.createLiteral(EXPECTED_LOCAL_DATE_TIME, XMLSchema.DATETIME);

    dateTimeProperty.setVendorExtensions(ImmutableMap.of(CONSTANT_VALUE, literal));

    // Act
    ZonedDateTime result = (ZonedDateTime) schemaMapperAdapter.mapGraphValue(dateTimeProperty,
        graphEntityMock, valueContext, schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE_TIME));
  }

}
