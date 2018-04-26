package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import java.util.Collections;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.entity.TupleEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IntegerSchemaMapperTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral(123);
  private static final IRI VALUE_3 = SimpleValueFactory.getInstance().createIRI("http://foo");

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private Value valueMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;
  @Mock
  private TupleEntity tupleEntityMock;

  private SchemaMapperAdapter schemaMapperAdapter;
  private IntegerSchemaMapper integerSchemaMapper;
  private IntegerProperty integerProperty;

  @Before
  public void setUp() {
    integerSchemaMapper = new IntegerSchemaMapper();
    integerProperty = new IntegerProperty();

    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
    schemaMapperAdapter = new SchemaMapperAdapter(Collections.singletonList(integerSchemaMapper));
  }

  @Test
  public void mapTupleValue_ThrowsException_ForNonLiterals() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage("Value is not a literal value.");

    // Arrange & Act
    integerSchemaMapper.mapTupleValue(integerProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN).build());
  }

  @Test
  public void mapTupleValue_ReturnValue_ForLiterals() {
    // Arrange & Act
    Integer result = (Integer) integerSchemaMapper.mapTupleValue(integerProperty, tupleEntityMock,
        ValueContext.builder().value(DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION).build());

    // Assert
    assertThat(result, equalTo(DBEERPEDIA.BROUWTOREN_YEAR_OF_FOUNDATION.intValue()));
  }

  @Test
  public void supports_ReturnsTrue_ForIntegerProperty() {
    // Arrange & Act
    Boolean supported = integerSchemaMapper.supports(integerProperty);

    // Assert
    assertThat(supported, equalTo(true));
  }

  @Test
  public void supports_ReturnsTrue_ForNonIntegerProperty() {
    // Arrange & Act
    Boolean supported = integerSchemaMapper.supports(new StringProperty());

    // Assert
    assertThat(supported, equalTo(false));
  }

  @Test
  public void mapGraphValue_ReturnsValue_ForLdPath() {
    // Arrange
    integerProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(eq(valueMock), anyString())).thenReturn(
        ImmutableList.of(VALUE_1));

    // Act
    Integer result = (Integer) schemaMapperAdapter.mapGraphValue(integerProperty, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);

    // Assert
    assertThat(result, is(VALUE_1.integerValue().intValue()));
  }

  @Test
  public void mapGraphValue_ThrowsException_ForUnsupportedType() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format(
        "LDPathQuery '%s' yielded a value which is not a literal of supported type",
        DUMMY_EXPR));

    // Arrange
    integerProperty.setVendorExtension(OpenApiSpecificationExtensions.LDPATH, DUMMY_EXPR);
    when(ldPathExecutorMock.ldPathQuery(eq(valueMock), anyString())).thenReturn(
        ImmutableList.of(VALUE_3));

    // Act
    schemaMapperAdapter.mapGraphValue(integerProperty, graphEntityMock,
        ValueContext.builder().value(valueMock).build(), schemaMapperAdapter);
  }
}
