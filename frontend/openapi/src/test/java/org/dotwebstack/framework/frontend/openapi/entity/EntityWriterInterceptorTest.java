package org.dotwebstack.framework.frontend.openapi.entity;

import static org.dotwebstack.framework.frontend.openapi.entity.GraphEntity.newGraphEntity;
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
import com.google.common.collect.ImmutableSet;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.StringProperty;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.term.IntegerTermParameter;
import org.dotwebstack.framework.param.term.StringTermParameter;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
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

  private static final ValueFactory VALUE_FACTORY = SimpleValueFactory.getInstance();

  private static final String NAMESPACE_RO = "http://data.informatiehuisruimte.nl/def/ro#";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();
  @Mock
  private WriterInterceptorContext interceptorContextMock;

  @Mock
  private TupleEntityMapper tupleEntityMapperMock;

  @Mock
  private GraphEntityMapper graphEntityMapperMock;

  @Mock
  private RequestContext requestContextMock;

  @Mock
  private InformationProduct productMock;

  @Mock
  private Repository repositoryMock;

  @Mock
  private Swagger definitionsMock;

  @Captor
  private ArgumentCaptor<Object> entityCaptor;

  private EntityWriterInterceptor entityWriterInterceptor;

  @Before
  public void setUp() {
    entityWriterInterceptor =
        new EntityWriterInterceptor(graphEntityMapperMock, tupleEntityMapperMock);

    when(interceptorContextMock.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
    when(requestContextMock.getInformationProduct()).thenReturn(productMock);
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
  public void aroundWriteTo_MapsEntity_ForTupleEntity() throws IOException {
    // Arrange
    TupleEntity entity =
        new TupleEntity(new Response(), mock(TupleQueryResult.class), requestContextMock);
    Object mappedEntity = ImmutableList.of();
    when(interceptorContextMock.getEntity()).thenReturn(entity);
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
  public void aroundWriteTo_MapsEntity_ForGraphEntity() throws IOException {
    // Arrange
    GraphEntity entity = newGraphEntity(new Response(), repositoryMock, ImmutableSet.of(),
        definitionsMock, requestContextMock);
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
    Response response = new Response().headers(ImmutableMap.of());

    GraphEntity entity = newGraphEntity(response, repositoryMock, ImmutableSet.of(),
        definitionsMock, requestContextMock);
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
    Response response = new Response().headers(ImmutableMap.of("Content-Crs",
        new StringProperty().vendorExtension("foo", "bar").vendorExtension("baz", "qux")));

    GraphEntity entity = newGraphEntity(response, repositoryMock, ImmutableSet.of(),
        definitionsMock, requestContextMock);
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
    thrown.expect(ConfigurationException.class);
    thrown.expectMessage(String.format("No parameter found for vendor extension value: '%s'",
        NAMESPACE_RO + "ContentCrsParameter"));

    // Arrange
    when(productMock.getParameters()).thenReturn(ImmutableList.of(
        new StringTermParameter(VALUE_FACTORY.createIRI("http://foo#", "bar"), "bar", false),
        new StringTermParameter(VALUE_FACTORY.createIRI("http://baz#", "qux"), "qux", false)));

    Response response = new Response().header("Content-Crs",
        new StringProperty().vendorExtension(OpenApiSpecificationExtensions.PARAMETER,
            NAMESPACE_RO + "ContentCrsParameter").vendorExtension("foo", "bar"));

    GraphEntity entity = newGraphEntity(response, repositoryMock, ImmutableSet.of(),
        definitionsMock, requestContextMock);
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
    when(productMock.getParameters()).thenReturn(ImmutableList.of(
        new StringTermParameter(VALUE_FACTORY.createIRI(NAMESPACE_RO, "ContentCrsParameter"),
            "contentCrs", false),
        new IntegerTermParameter(VALUE_FACTORY.createIRI(NAMESPACE_RO, "xPaginationPageParameter"),
            "page", false)));

    Response response = new Response().header("Content-Crs",
        new StringProperty().vendorExtension(OpenApiSpecificationExtensions.PARAMETER,
            NAMESPACE_RO + "ContentCrsParameter")).header("x-Pagination-Page",
                new IntegerProperty().vendorExtension(OpenApiSpecificationExtensions.PARAMETER,
                    NAMESPACE_RO + "xPaginationPageParameter"));

    when(requestContextMock.getParameters()).thenReturn(
        ImmutableMap.of("contentCrs", "epsg:28992", "page", "3"));

    GraphEntity entity = newGraphEntity(response, repositoryMock, ImmutableSet.of(),
        definitionsMock, requestContextMock);

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
