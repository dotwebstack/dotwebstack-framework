package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.Response;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import java.io.IOException;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.entity.schema.ResponseProperty;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.term.IntegerTermParameter;
import org.dotwebstack.framework.param.term.StringTermParameter;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQueryResult;
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
public class EntityWriterInterceptorTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private WriterInterceptorContext interceptorContextMock;

  @Mock
  private TupleEntityMapper tupleEntityMapperMock;

  @Mock
  private GraphEntityMapper graphEntityMapperMock;

  @Captor
  private ArgumentCaptor<Object> entityCaptor;

  private EntityWriterInterceptor entityWriterInterceptor;

  @Before
  public void setUp() {
    entityWriterInterceptor =
        new EntityWriterInterceptor(graphEntityMapperMock, tupleEntityMapperMock);
  }

  @Test
  public void aroundWriteTo_MapsEntity_ForTupleEntity() throws IOException {
    // Arrange
    TupleEntity entity = new TupleEntity(ImmutableMap.of(), mock(TupleQueryResult.class));
    Object mappedEntity = ImmutableList.of();
    when(interceptorContextMock.getEntity()).thenReturn(entity);
    when(interceptorContextMock.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
    when(tupleEntityMapperMock.map(entity, MediaType.APPLICATION_JSON_TYPE)).thenReturn(
        mappedEntity);

    // Act
    entityWriterInterceptor.aroundWriteTo(interceptorContextMock);

    // Assert
    verify(interceptorContextMock).setEntity(entityCaptor.capture());
    verify(interceptorContextMock).proceed();
    assertThat(entityCaptor.getValue(), sameInstance(mappedEntity));
  }

  @Test
  public void aroundWriteTo_DoesNothing_ForUnknownEntity() throws IOException {
    // Arrange
    when(interceptorContextMock.getEntity()).thenReturn(new Object());

    // Act
    entityWriterInterceptor.aroundWriteTo(interceptorContextMock);

    // Assert
    verify(interceptorContextMock, never()).setEntity(any());
    verify(interceptorContextMock).proceed();
  }

  @Test
  public void aroundWriteTo_MapsEntity_ForGraphEntity() throws IOException {
    // Arrange
    when(interceptorContextMock.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    Map<MediaType, Property> schemaMap =
        ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE, new ResponseProperty(new Response()));
    GraphEntity entity = new GraphEntity(schemaMap, mock(GraphEntityContext.class));

    when(interceptorContextMock.getEntity()).thenReturn(entity);

    Object mappedEntity = ImmutableList.of();
    when(graphEntityMapperMock.map(entity, MediaType.APPLICATION_JSON_TYPE)).thenReturn(
        mappedEntity);

    // Act
    entityWriterInterceptor.aroundWriteTo(interceptorContextMock);

    // Assert
    verify(interceptorContextMock, never()).getHeaders();
  }

  @Test
  public void aroundWriteTo_DoesNotWriteResponseHeaders_ForZeroHeaders() throws IOException {
    // Arrange
    when(interceptorContextMock.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    Map<MediaType, Property> schemaMap = ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ResponseProperty(new Response().headers(ImmutableMap.of())));
    GraphEntity entity = new GraphEntity(schemaMap, mock(GraphEntityContext.class));

    when(interceptorContextMock.getEntity()).thenReturn(entity);

    Object mappedEntity = ImmutableList.of();
    when(graphEntityMapperMock.map(entity, MediaType.APPLICATION_JSON_TYPE)).thenReturn(
        mappedEntity);

    // Act
    entityWriterInterceptor.aroundWriteTo(interceptorContextMock);

    // Assert
    verify(interceptorContextMock, never()).getHeaders();
  }

  @Test
  public void aroundWriteTo_DoesNotWriteResponseHeaders_ForUnknownVendorExtensions()
      throws IOException {
    // Arrange
    when(interceptorContextMock.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    Map<MediaType, Property> schemaMap = ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ResponseProperty(new Response().headers(ImmutableMap.of("Content-Crs",
            new StringProperty().vendorExtension("foo", "bar").vendorExtension("baz", "qux")))));
    GraphEntity entity = new GraphEntity(schemaMap, mock(GraphEntityContext.class));

    when(interceptorContextMock.getEntity()).thenReturn(entity);

    Object mappedEntity = ImmutableList.of();
    when(graphEntityMapperMock.map(entity, MediaType.APPLICATION_JSON_TYPE)).thenReturn(
        mappedEntity);

    // Act
    entityWriterInterceptor.aroundWriteTo(interceptorContextMock);

    // Assert
    verify(interceptorContextMock, never()).getHeaders();
  }

  @Test
  public void aroundWriteTo_ThrowsConfigurationException_ForUnknownParameter() throws IOException {
    // Assert
    String parameterId = "http://data.informatiehuisruimte.nl/def/ro#ContentCrsParameter";

    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(
        String.format("No parameter found for vendor extension value: '%s'", parameterId));

    // Arrange
    when(interceptorContextMock.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    GraphEntityContext entityContextMock = mock(GraphEntityContext.class);

    InformationProduct productMock = mock(InformationProduct.class);
    when(entityContextMock.getInformationProduct()).thenReturn(productMock);

    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
    when(productMock.getParameters()).thenReturn(ImmutableList.of(
        new StringTermParameter(valueFactory.createIRI("http://foo#", "bar"), "bar", false),
        new StringTermParameter(valueFactory.createIRI("http://baz#", "qux"), "qux", false)));

    Map<MediaType, Property> schemaMap =
        ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
            new ResponseProperty(
                new Response().headers(
                    ImmutableMap.of("Content-Crs",
                        new StringProperty().vendorExtension(
                            OpenApiSpecificationExtensions.PARAMETER, parameterId).vendorExtension(
                                "foo", "bar")))));
    GraphEntity entity = new GraphEntity(schemaMap, entityContextMock);

    when(interceptorContextMock.getEntity()).thenReturn(entity);

    Object mappedEntity = ImmutableList.of();
    when(graphEntityMapperMock.map(entity, MediaType.APPLICATION_JSON_TYPE)).thenReturn(
        mappedEntity);

    // Act
    entityWriterInterceptor.aroundWriteTo(interceptorContextMock);
  }

  @Test
  public void aroundWriteTo_WritesResponseHeaders_ForKnownParameters() throws IOException {
    // Arrange
    when(interceptorContextMock.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    GraphEntityContext entityContextMock = mock(GraphEntityContext.class);

    InformationProduct productMock = mock(InformationProduct.class);
    when(entityContextMock.getInformationProduct()).thenReturn(productMock);
    when(entityContextMock.getResponseParameters()).thenReturn(
        ImmutableMap.of("Content-Crs", "epsg:28992", "x-Pagination-Page", "3"));

    SimpleValueFactory valueFactory = SimpleValueFactory.getInstance();
    when(productMock.getParameters()).thenReturn(ImmutableList.of(
        new StringTermParameter(
            valueFactory.createIRI("http://data.informatiehuisruimte.nl/def/ro#",
                "ContentCrsParameter"),
            "Content-Crs", false),
        new IntegerTermParameter(
            valueFactory.createIRI("http://data.informatiehuisruimte.nl/def/ro#",
                "xPaginationPageParameter"),
            "x-Pagination-Page", false)));

    Map<MediaType, Property> schemaMap = ImmutableMap.of(MediaType.APPLICATION_JSON_TYPE,
        new ResponseProperty(new Response().headers(ImmutableMap.of("Content-Crs",
            new StringProperty().vendorExtension(OpenApiSpecificationExtensions.PARAMETER,
                "http://data.informatiehuisruimte.nl/def/ro#ContentCrsParameter"),
            "x-Pagination-Page",
            new IntegerProperty().vendorExtension(OpenApiSpecificationExtensions.PARAMETER,
                "http://data.informatiehuisruimte.nl/def/ro#xPaginationPageParameter")))));
    GraphEntity entity = new GraphEntity(schemaMap, entityContextMock);

    when(interceptorContextMock.getEntity()).thenReturn(entity);

    MultivaluedMap<String, Object> responseHeaders = new MultivaluedHashMap<>();
    when(interceptorContextMock.getHeaders()).thenReturn(responseHeaders);

    Object mappedEntity = ImmutableList.of();
    when(graphEntityMapperMock.map(entity, MediaType.APPLICATION_JSON_TYPE)).thenReturn(
        mappedEntity);

    // Act
    entityWriterInterceptor.aroundWriteTo(interceptorContextMock);

    // Assert
    assertThat(responseHeaders.size(), is(2));
    assertThat(responseHeaders, hasEntry("Content-Crs", ImmutableList.of("epsg:28992")));
    assertThat(responseHeaders, hasEntry("x-Pagination-Page", ImmutableList.of(3)));
  }

}
