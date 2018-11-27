package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.models.media.StringSchema;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.frontend.openapi.entity.LdPathExecutor;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StringSchemaMapperContextLinksTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final Literal VALUE = SimpleValueFactory.getInstance().createLiteral("value");

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private RequestContext requestContextMock;
  @Mock
  private LdPathExecutor ldPathExecutorMock;

  private ValueContext valueContext;
  private SchemaMapperAdapter schemaMapperAdapter;
  private SchemaMapper<StringSchema, ?> schemaMapper;
  private StringSchema schema;

  @Before
  public void setUp() {
    schemaMapper = new StringSchemaMapper();
    schemaMapperAdapter = new SchemaMapperAdapter(ImmutableList.of(schemaMapper));
    schema = new StringSchema();

    valueContext = ValueContext.builder().value(VALUE).build();

    when(graphEntityMock.getRequestContext()).thenReturn(requestContextMock);
    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenContextLinksVendorExtIsPresentButValueIsNoMap() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        "Schema '" + OpenApiSpecificationExtensions.CONTEXT_LINKS + "' should be defined as Map");

    // Arrange
    schema.addExtension(OpenApiSpecificationExtensions.CONTEXT_LINKS, null);

    // Act
    schemaMapper.mapGraphValue(schema, false, graphEntityMock, valueContext, schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenVendorExtIsPresentButLinkChoicesIsNoList() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        "Schema '" + StringSchemaMapper.LINK_CHOICES + "' should be defined as List");

    // Arrange
    Map<String, Object> contextLinksExtension = new HashMap<>();
    schema.addExtension(OpenApiSpecificationExtensions.CONTEXT_LINKS, contextLinksExtension);

    // Act
    schemaMapper.mapGraphValue(schema, false, graphEntityMock, valueContext, schemaMapperAdapter);
  }

  @Test
  public void mapGraphValue_ThrowsEx_WhenVendorExtIsPresentButLdPathIsNoString() {
    // Assert
    thrown.expect(SchemaMapperRuntimeException.class);
    thrown.expectMessage(
        "Schema '" + OpenApiSpecificationExtensions.KEY_LDPATH + "' should be defined as String");

    // Arrange
    schema.addExtension(OpenApiSpecificationExtensions.CONTEXT_LINKS,
        ImmutableMap.of(StringSchemaMapper.LINK_CHOICES, Lists.newArrayList()));

    // Act
    schemaMapper.mapGraphValue(schema, false, graphEntityMock, valueContext, schemaMapperAdapter);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void mapGraphValue_picksTheRightKey_ForInheritedLdPath() {
    // Arrange
    when(graphEntityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);

    Map<String, Object> contextLinksExtension = new HashMap<>();
    schema.addExtension(OpenApiSpecificationExtensions.CONTEXT_LINKS, contextLinksExtension);

    List<Object> choices = new ArrayList<>();
    contextLinksExtension.put(StringSchemaMapper.LINK_CHOICES, choices);

    contextLinksExtension.put(OpenApiSpecificationExtensions.KEY_LDPATH, "key / path");
    /* this is pattern path to be shared/inherited */
    contextLinksExtension.put(OpenApiSpecificationExtensions.LDPATH, "common / pattern / path");

    /*
     * note that choices below do not contain specific x-ld-path; so common ld-path should be used
     */
    Map<String, Object> choice1 = choice("object_type_1", "/xyz/$1");
    Map<String, Object> choice2 = choice("object_type_2", "/abc/$1");

    choices.add(choice1);
    choices.add(choice2);

    Collection<Value> commonPatternResult =
        Lists.newArrayList(SimpleValueFactory.getInstance().createLiteral("common_type"));
    when(ldPathExecutorMock.ldPathQuery(VALUE, "common / pattern / path")).thenReturn(
        commonPatternResult);

    Collection<Value> realKeyResult =
        Lists.newArrayList(SimpleValueFactory.getInstance().createLiteral("object_type_2"));
    when(ldPathExecutorMock.ldPathQuery(VALUE, "key / path")).thenReturn(realKeyResult);

    when(requestContextMock.getBaseUri()).thenReturn("/base");

    // Act
    Object result = schemaMapper.mapGraphValue(schema, false, graphEntityMock, valueContext,
        schemaMapperAdapter);

    // Assert
    /*
     * we expect common pattern to be used because none of the choices above define specific
     * x-ld-path
     */
    Assert.assertEquals("/base/abc/common_type", result);

    // Arrange (2)
    /* now test what happens if there is a common ld path */
    /* define specific ld-path for the choice 'object_type_2' */
    ((Map<String, Object>) choice2.get(OpenApiSpecificationExtensions.RELATIVE_LINK)).put(
        OpenApiSpecificationExtensions.LDPATH, "specific / path / for / object_type_2");
    Collection<Value> choice2PatternResult =
        Lists.newArrayList(SimpleValueFactory.getInstance().createLiteral("object_type_2"));
    when(ldPathExecutorMock.ldPathQuery(VALUE, "specific / path / for / object_type_2")).thenReturn(
        choice2PatternResult);

    // Act (2)
    result = schemaMapper.mapGraphValue(schema, false, graphEntityMock, valueContext,
        schemaMapperAdapter);

    // Assert (2)
    Assert.assertEquals("/base/abc/object_type_2", result);

    // Arrange (3)
    /* in case there are no choice matching real key, return null */
    realKeyResult = Lists.newArrayList(
        SimpleValueFactory.getInstance().createLiteral("this_key_is_not_covered_in_choices"));
    when(ldPathExecutorMock.ldPathQuery(VALUE, "key / path")).thenReturn(realKeyResult);

    // Act (3)
    result = schemaMapper.mapGraphValue(schema, false, graphEntityMock, valueContext,
        schemaMapperAdapter);

    // Assert (3)
    Assert.assertNull(result);
  }

  private Map<String, Object> choice(String key, String pattern) {
    Map<String, Object> choiceRelativeLink = new HashMap<>();

    choiceRelativeLink.put(StringSchemaMapper.PATTERN, pattern);
    choiceRelativeLink.put(OpenApiSpecificationExtensions.LDPATH, null);

    Map<String, Object> choiceValue = new HashMap<>();

    choiceValue.put(OpenApiSpecificationExtensions.RELATIVE_LINK, choiceRelativeLink);
    choiceValue.put(StringSchemaMapper.KEY, key);

    return choiceValue;
  }

}
