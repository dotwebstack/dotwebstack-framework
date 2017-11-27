package org.dotwebstack.framework.frontend.ld.handlers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.config.ConfigurationException;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RepresentationRequestHandlerTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Mock
  private Representation representation;

  @Mock
  private ContainerRequestContext containerRequestContext;

  @Mock
  private InformationProduct informationProduct;

  private RepresentationRequestParameterMapper representationRequestParameterMapper;

  private RepresentationRequestHandler getRequestHandler;

  @Before
  public void setUp() {
    representationRequestParameterMapper = new RepresentationRequestParameterMapper();
    getRequestHandler =
        new RepresentationRequestHandler(representation, representationRequestParameterMapper);
  }

  @Test
  public void constructor_ThrowsException_WithMissingRepresentation() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new RepresentationRequestHandler(null, representationRequestParameterMapper);
  }

  @Test
  public void apply_ReturnQueryRepresentation_WhenGraphQueryResult() {
    // Arrange
    when(representation.getInformationProduct()).thenReturn(informationProduct);
    GraphQueryResult queryResult = mock(GraphQueryResult.class);
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);
    when(informationProduct.getResultType()).thenReturn(ResultType.GRAPH);

    UriInfo uriInfo = mock(UriInfo.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/");

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(GraphEntity.class));
    GraphEntity entity = (GraphEntity) response.getEntity();
    assertThat(entity.getQueryResult(), equalTo(queryResult));
    assertThat(entity.getRepresentation(), equalTo(representation));
  }

  @Test
  public void apply_ReturnQueryRepresentation_WhenTupleQueryResult() {
    // Arrange
    when(representation.getInformationProduct()).thenReturn(informationProduct);
    TupleQueryResult queryResult = mock(TupleQueryResult.class);
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);
    when(informationProduct.getResultType()).thenReturn(ResultType.TUPLE);

    UriInfo uriInfo = mock(UriInfo.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/");

    // Act
    Response response = getRequestHandler.apply(containerRequestContext);

    // Assert
    assertThat(response.getStatus(), equalTo(200));
    assertThat(response.getEntity(), instanceOf(TupleEntity.class));
    TupleEntity entity = (TupleEntity) response.getEntity();
    assertThat(entity.getQueryResult(), equalTo(queryResult));
    assertThat(entity.getRepresentation(), equalTo(representation));
  }

  @Test
  public void apply_ThrowsException_WhenQueryResultIsUnexpected() {
    // Arrange
    when(representation.getInformationProduct()).thenReturn(informationProduct);
    Object queryResult = new Object();
    when(informationProduct.getResult(ImmutableMap.of())).thenReturn(queryResult);

    UriInfo uriInfo = mock(UriInfo.class);
    when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
    when(uriInfo.getPath()).thenReturn("/");

    // Assert
    thrown.expect(ConfigurationException.class);

    // Act
    getRequestHandler.apply(containerRequestContext);
  }

}
