package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.media.DateSchema;
import java.time.LocalDate;
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
public class DateSchemaMapperConstantValueTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String CONSTANT_VALUE = OpenApiSpecificationExtensions.CONSTANT_VALUE;
  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();
  private static final String EXPECTED_LOCAL_DATE = "1982-11-25";

  @Mock
  private GraphEntity graphEntityMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private DateSchema dateSchema;
  private ValueContext valueContext;

  @Before
  public void setUp() {
    dateSchema = new DateSchema();
    schemaMapperAdapter =
        new SchemaMapperAdapter(Collections.singletonList(new DateSchemaMapper()));
    valueContext = ValueContext.builder().build();
  }

  @Test
  public void mapGraphValue_ReturnsLocalDate_WhenStringConstantValueIsDefined() {
    // Arrange
    dateSchema.setExtensions(ImmutableMap.of(CONSTANT_VALUE, EXPECTED_LOCAL_DATE));

    // Act
    LocalDate result = (LocalDate) schemaMapperAdapter.mapGraphValue(dateSchema, false,
        graphEntityMock, valueContext, schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE));
  }

  @Test
  public void mapGraphValue_ReturnsLocalDate_WhenSupportedLiteralConstantValueIsDefined() {
    // Arrange
    Literal literal = VALUE_FACTORY.createLiteral(EXPECTED_LOCAL_DATE, XMLSchema.DATE);
    dateSchema.setExtensions(ImmutableMap.of(CONSTANT_VALUE, literal));

    // Act
    LocalDate result = (LocalDate) schemaMapperAdapter.mapGraphValue(dateSchema, false,
        graphEntityMock, valueContext, schemaMapperAdapter);

    // Assert
    assertThat(result.toString(), is(EXPECTED_LOCAL_DATE));
  }

}
