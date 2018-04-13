package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions.LDPATH;
import static org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions.RELATIVE_LINK;
import static org.dotwebstack.framework.frontend.openapi.entity.schema.StringSchemaMapper.PATTERN;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.StringProperty;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringSchemaMapperRelativeLinkTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final String BASE_URL = "https://some.base.url/";
  private static final String SOME_PATTERN = "/somePatternWithSinglePlaceHolder/$1";
  private static final String DUMMY_EXPR = "dummyExpr()";
  private static final Literal VALUE_1 = SimpleValueFactory.getInstance().createLiteral("a");
  private static final Literal VALUE_2 = SimpleValueFactory.getInstance().createLiteral("b");

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;
  @Mock
  private RequestContext requestContextMock;

  private ValueContext valueContext;
  private SchemaMapperAdapter schemaMapperAdapter;
  private SchemaMapper<StringProperty, ?> schemaMapper;
  private StringProperty stringProperty;

  @Before
  public void setUp() {
    schemaMapper = new StringSchemaMapper();
    stringProperty = new StringProperty();
    schemaMapperAdapter = new SchemaMapperAdapter(ImmutableList.of(schemaMapper));
    valueContext = ValueContext.builder().value(VALUE_1).build();


    when(requestContextMock.getBaseUri()).thenReturn(BASE_URL);
    when(graphEntityMock.getRequestContext()).thenReturn(requestContextMock);
    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_ReturnsAbsoluteLink_ForRelativeLinkWithoutPlaceHolder() {
    // Arrange
    String pattern = "/somePatternWithoutPlaceholder";
    stringProperty.setVendorExtension(RELATIVE_LINK, ImmutableMap.of(PATTERN, pattern));

    // Act
    Object result = schemaMapper.mapGraphValue(stringProperty, graphEntityMock, valueContext,
        schemaMapperAdapter);

    // Assert
    assertThat(result, is(BASE_URL + pattern));
  }

  @Test
  public void mapGraphValue_ReplacesPlaceHolder_ForRelativeLinkWithsPlaceHolder() {
    // Arrange
    stringProperty.setVendorExtension(RELATIVE_LINK,
        ImmutableMap.of(PATTERN, SOME_PATTERN, LDPATH, DUMMY_EXPR));

    when(ldPathExecutorMock.ldPathQuery(eq(VALUE_1), anyString())).thenReturn(
        ImmutableList.of(VALUE_1));

    // Act
    Object result = schemaMapper.mapGraphValue(stringProperty, graphEntityMock, valueContext,
        schemaMapperAdapter);

    // Assert
    assertThat(result, is(BASE_URL + SOME_PATTERN.replace("$1", VALUE_1.stringValue())));
  }

  @Test
  public void mapGraphValue_ReturnsNull_ForLdPathExpressionWithoutResult() {
    // Arrange
    stringProperty.setVendorExtension(RELATIVE_LINK,
        ImmutableMap.of(PATTERN, SOME_PATTERN, LDPATH, DUMMY_EXPR));

    // Act
    Object result = schemaMapper.mapGraphValue(stringProperty, graphEntityMock, valueContext,
        schemaMapperAdapter);

    // Assert
    assertThat(result, nullValue());
  }

  @Test
  public void mapGraphValue_ThrowsEx_ForRelativeLinkWithLdPathExpressionWithMultipleResults() {
    // Arrange
    stringProperty.setVendorExtension(RELATIVE_LINK,
        ImmutableMap.of(PATTERN, SOME_PATTERN, LDPATH, DUMMY_EXPR));

    when(ldPathExecutorMock.ldPathQuery(eq(VALUE_1), anyString())).thenReturn(
        ImmutableList.of(VALUE_1, VALUE_2));
    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("LDPath query '%s' yielded multiple results (%d) for a property, which "
            + "requires a single result.", DUMMY_EXPR, 2));

    // Act
    schemaMapper.mapGraphValue(stringProperty, graphEntityMock, valueContext,
        schemaMapperAdapter);

  }

  @Test
  public void mapGraphValue_ThrowsException_ForRelativeLinkWithoutPatternProperty() {
    // Arrange
    stringProperty.setVendorExtension(RELATIVE_LINK, ImmutableMap.<String, String>of());

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        String.format("'%s' should have a '%s' property.", RELATIVE_LINK, PATTERN));

    // Act
    schemaMapper.mapGraphValue(stringProperty, graphEntityMock, valueContext,
        schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsException_ForRelativeLinkThatIsNull() {
    // Arrange
    stringProperty.setVendorExtension(RELATIVE_LINK, null);

    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(String.format("'%s' can not be null.", RELATIVE_LINK));

    // Act
    schemaMapper.mapGraphValue(stringProperty, graphEntityMock, valueContext,
        schemaMapperAdapter);
  }

}
