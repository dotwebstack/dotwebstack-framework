package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.models.properties.Property;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.dotwebstack.framework.frontend.openapi.entity.builder.EntityBuilder;
import org.dotwebstack.framework.frontend.openapi.entity.builder.QueryResult;
import org.dotwebstack.framework.frontend.openapi.entity.builder.RequestParameters;
import org.dotwebstack.framework.frontend.openapi.entity.properties.PropertyHandlerRegistry;
import org.eclipse.rdf4j.model.Model;
import org.junit.Before;
import org.junit.Ignore;
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
  private WriterInterceptorContext context;


  @Mock
  private EntityBuilder entityBuilder;

  @Mock
  private PropertyHandlerRegistry propertyHandlerRegistry;

  @Captor
  private ArgumentCaptor<Object> entityCaptor;

  private EntityWriterInterceptor entityWriterInterceptor;

  @Before
  public void setUp() {
    entityWriterInterceptor = new EntityWriterInterceptor(entityBuilder, propertyHandlerRegistry);
  }

  @Test
  public void constructor_ThrowsException_WithMissingEntityWriterInterceptor() {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    new EntityWriterInterceptor(null, null);
  }

  @Test
  public void aroundWriteTo_ThrowsException_WithMissingWriterInterceptorContext()
      throws IOException {
    // Assert
    thrown.expect(NullPointerException.class);

    // Act
    entityWriterInterceptor.aroundWriteTo(null);
  }


  @Ignore
  @Test
  public void aroundWriteTo_MapsEntity_ForTupleEntity() throws IOException {
    // Arrange
    RequestParameters requestParameters =
        RequestParameters.builder().requestStringParameters(ImmutableMap.of()).build();

    Property schemaProperty = mock(Property.class);
    QueryResult queryResult = mock(QueryResult.class);
    Model model = mock(Model.class);
    when(queryResult.getModel()).thenReturn(model);
    TupleEntity entity = (TupleEntity) TupleEntity.builder().withRequestParameters(
        requestParameters).withSchemaProperty(schemaProperty).withBaseUri("").withPath(
            "").withQueryResult(queryResult).build();

    final Object mappedEntity = ImmutableList.of();
    when(context.getEntity()).thenReturn(entity);
    when(context.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);

    // Act
    entityWriterInterceptor.aroundWriteTo(context);

    // Assert
    verify(context).setEntity(entityCaptor.capture());
    verify(context).proceed();
    assertThat(entityCaptor.getValue(), equalTo(mappedEntity));
  }

  @Test
  public void aroundWriteTo_DoesNothing_ForUnknownEntity() throws IOException {
    // Arrange
    when(context.getEntity()).thenReturn(new Object());

    // Act
    entityWriterInterceptor.aroundWriteTo(context);

    // Assert
    verify(context, never()).setEntity(any());
    verify(context).proceed();
  }

}
