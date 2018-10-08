package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.Map;
import java.util.Set;
import org.dotwebstack.framework.frontend.openapi.OpenApiSpecificationExtensions;
import org.dotwebstack.framework.frontend.openapi.handlers.RequestContext;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.repository.Repository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GraphEntityTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private ApiResponse responseMock;

  @Mock
  private Repository repositoryMock;

  @Mock
  private Set<Resource> subjectsMock;

  @Mock
  private OpenAPI openApiMock;

  @Mock
  private Components openApiComponentsMock;

  @Mock
  private RequestContext requestContextMock;

  @Test
  public void newGraphEntity_CreatesGraphEntity_WhenDefinitionsArePresent() {
    // Arrange
    when(openApiMock.getComponents()).thenReturn(openApiComponentsMock);

    // Act
    GraphEntity graphEntity = GraphEntity.newGraphEntity(responseMock, repositoryMock, subjectsMock,
        openApiMock, requestContextMock);

    // Assert
    assertThat(graphEntity.getResponse(), equalTo(responseMock));
    assertThat(graphEntity.getRepository(), equalTo(repositoryMock));
    assertThat(graphEntity.getSubjects(), equalTo(subjectsMock));
    assertThat(graphEntity.getOpenApiComponents(), equalTo(openApiComponentsMock));
    assertThat(graphEntity.getRequestContext(), equalTo(requestContextMock));
  }

  @Test
  public void newGraphEntity_CreatesEmptyMaps_ForAbsentValues() {
    // Act
    GraphEntity graphEntity = GraphEntity.newGraphEntity(responseMock, repositoryMock, subjectsMock,
        openApiMock, requestContextMock);

    // Assert
    assertThat(graphEntity.getOpenApiComponents(), is(not(nullValue())));
    assertThat(graphEntity.getOpenApiComponents().getCallbacks(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getExamples(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getExtensions(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getHeaders(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getLinks(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getParameters(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getRequestBodies(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getResponses(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getSchemas(), is(nullValue()));
    assertThat(graphEntity.getOpenApiComponents().getSecuritySchemes(), is(nullValue()));


    assertThat(graphEntity.getLdPathNamespaces(), is(not(nullValue())));
    assertThat(graphEntity.getLdPathNamespaces().keySet(), is(empty()));
  }

  @Test
  public void newGraphEntity_ExtractsNamespaces_WhenExtEnabled() {
    // Arrange
    Map<String, String> namespaces = ImmutableMap.of("rdf", RDF.NAMESPACE, "rdfs", RDFS.NAMESPACE);
    Map<String, Object> vendorExtensions =
        ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH_NAMESPACES, namespaces);
    when(openApiMock.getExtensions()).thenReturn(vendorExtensions);

    // Act
    GraphEntity graphEntity = GraphEntity.newGraphEntity(responseMock, repositoryMock, subjectsMock,
        openApiMock, requestContextMock);

    // Assert
    assertThat(graphEntity.getLdPathNamespaces(), equalTo(namespaces));
  }

  @Test
  public void newGraphEntity_ThrowsException_WhenNamespacesNotInMap() {
    // Assert
    thrown.expect(LdPathExecutorRuntimeException.class);

    // Arrange
    when(openApiMock.getExtensions()).thenReturn(
        ImmutableMap.of(OpenApiSpecificationExtensions.LDPATH_NAMESPACES, true));

    // Act
    GraphEntity.newGraphEntity(responseMock, repositoryMock, subjectsMock, openApiMock,
        requestContextMock);
  }

}
