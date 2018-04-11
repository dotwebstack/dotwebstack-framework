package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.BooleanProperty;
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
public class BooleanSchemaMapperLdPathTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private BooleanSchemaMapper schemaMapper;

  private BooleanProperty property;

  @Mock
  private GraphEntity entityMock;

  @Mock
  private Value subjectMock;

  private SchemaMapperAdapter mapperAdapter;

  @Mock
  private LdPathExecutor ldPathExecutorMock;


  @Before
  public void setUp() {
    schemaMapper = new BooleanSchemaMapper();
    property = new BooleanProperty();

    mapperAdapter = new SchemaMapperAdapter(Collections.singletonList(schemaMapper));

    when(entityMock.getLdPathExecutor()).thenReturn(ldPathExecutorMock);
  }

  @Test
  public void mapGraphValue_ReturnsBooleanValue_ForLdPath() {
    // Arrange
    property.setVendorExtensions(ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH, "ld-path"));
    Literal literal = VALUE_FACTORY.createLiteral("true", XMLSchema.BOOLEAN);

    when(ldPathExecutorMock.ldPathQuery(subjectMock, "ld-path")).thenReturn(
        ImmutableList.of(literal));

    // Act
    Boolean result = schemaMapper.mapGraphValue(property, entityMock,
        ValueContext.builder().value(subjectMock).build(), mapperAdapter);

    // Assert
    assertThat(result, is(true));
  }

}
