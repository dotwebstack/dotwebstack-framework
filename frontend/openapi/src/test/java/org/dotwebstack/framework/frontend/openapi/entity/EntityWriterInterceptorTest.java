package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptorContext;
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
  private WriterInterceptorContext context;

  @Mock
  private TupleEntityMapper tupleEntityMapper;

  @Mock
  private GraphEntityMapper graphEntityMapper;

  @Captor
  private ArgumentCaptor<Object> entityCaptor;

  private EntityWriterInterceptor entityWriterInterceptor;

  @Before
  public void setUp() {
    entityWriterInterceptor = new EntityWriterInterceptor(graphEntityMapper, tupleEntityMapper);
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


  @Test
  public void aroundWriteTo_MapsEntity_ForTupleEntity() throws IOException {
    // Arrange
    TupleEntity entity = mock(TupleEntity.class);
    Object mappedEntity = ImmutableList.of();
    when(context.getEntity()).thenReturn(entity);
    when(context.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
    when(tupleEntityMapper.mapTuple(entity, MediaType.APPLICATION_JSON_TYPE)).thenReturn(
        mappedEntity);

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
