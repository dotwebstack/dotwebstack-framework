package org.dotwebstack.framework.frontend.openapi.entity.schema;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import io.swagger.models.Response;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.GraphEntity;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.IRI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResponseSchemaMapperTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Captor
  private ArgumentCaptor<ValueContext> valueContextCaptor;

  @Mock
  private GraphEntity graphEntityMock;
  @Mock
  private SchemaMapperAdapter schemaMapperAdapterMock;

  private ResponseSchemaMapper schemaMapper;

  @Before
  public void setUp() {
    schemaMapper = new ResponseSchemaMapper();
  }

  @Test
  public void mapGraphValue_DoesNotSwitchContext_WhenSubjectExtIsNotEnabled() {
    // Arrange
    ValueContext valueContext = ValueContext.builder().build();
    Property schemaProperty = mock(Property.class);
    ResponseProperty responseProperty = new ResponseProperty(new Response().schema(schemaProperty));

    // Act
    schemaMapper.mapGraphValue(responseProperty, graphEntityMock, valueContext,
            schemaMapperAdapterMock);

    // Assert
    verify(schemaMapperAdapterMock).mapGraphValue(eq(schemaProperty), eq(graphEntityMock),
            valueContextCaptor.capture(), eq(schemaMapperAdapterMock));
    assertThat(valueContextCaptor.getValue().getValue(), is(nullValue()));
  }

  @Test
  public void mapGraphValue_SwitchesContext_WhenSubjectExtIsEnabled() {
    // Arrange
    ValueContext valueContext = ValueContext.builder().build();
    Property schemaProperty = mock(Property.class);
    ResponseProperty responseProperty = new ResponseProperty(
            new Response().vendorExtension(OpenApiSpecificationExtensions.SUBJECT, true).schema(
                    schemaProperty));
    when(graphEntityMock.getSubjects()).thenReturn(ImmutableSet.of(DBEERPEDIA.BROUWTOREN));

    // Act
    schemaMapper.mapGraphValue(responseProperty, graphEntityMock, valueContext,
            schemaMapperAdapterMock);

    // Assert
    verify(schemaMapperAdapterMock).mapGraphValue(eq(schemaProperty), eq(graphEntityMock),
            valueContextCaptor.capture(), eq(schemaMapperAdapterMock));
    assertThat(valueContextCaptor.getValue().getValue(), equalTo(DBEERPEDIA.BROUWTOREN));
  }

  @Test
  public void mapGraphValue_DoesNotSwitchContext_WhenSubjectExtAndNoSubjects() {
    // Arrange
    ValueContext valueContext = ValueContext.builder().build();
    Property schemaProperty = mock(Property.class);
    ResponseProperty responseProperty = new ResponseProperty(
            new Response().vendorExtension(OpenApiSpecificationExtensions.SUBJECT, true).schema(
                    schemaProperty));
    when(graphEntityMock.getSubjects()).thenReturn(ImmutableSet.of());

    // Act
    Object result = schemaMapper.mapGraphValue(responseProperty, graphEntityMock, valueContext,
            schemaMapperAdapterMock);

    // Assert
    verifyZeroInteractions(schemaMapperAdapterMock);
    assertThat(result, is(nullValue()));
  }

  @Test
  public void support_ReturnsTrue_ForResponseProperty() {
    // Act
    boolean result = schemaMapper.supports(new ResponseProperty(new Response()));

    // Assert
    assertThat(result, is(true));
  }

  @Test
  public void support_ReturnsFalse_ForNonResponseProperty() {
    // Act
    boolean result = schemaMapper.supports(new ArrayProperty());

    // Assert
    assertThat(result, is(false));
  }

  @Test
  public void getSupportedDataTypes_ReturnsEmptySet() {
    // Act
    Set<IRI> result = schemaMapper.getSupportedDataTypes();

    // Assert
    assertThat(result, empty());
  }

}