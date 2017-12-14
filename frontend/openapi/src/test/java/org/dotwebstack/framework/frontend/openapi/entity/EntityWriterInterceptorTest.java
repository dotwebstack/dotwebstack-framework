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
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EntityWriterInterceptorTest {

  @Mock
  private WriterInterceptorContext contextMock;

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
    when(contextMock.getEntity()).thenReturn(entity);
    when(contextMock.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
    when(tupleEntityMapperMock.map(entity, MediaType.APPLICATION_JSON_TYPE)).thenReturn(
        mappedEntity);

    // Act
    entityWriterInterceptor.aroundWriteTo(contextMock);

    // Assert
    verify(contextMock).setEntity(entityCaptor.capture());
    verify(contextMock).proceed();
    assertThat(entityCaptor.getValue(), equalTo(mappedEntity));
  }

  @Test
  public void aroundWriteTo_DoesNothing_ForUnknownEntity() throws IOException {
    // Arrange
    when(contextMock.getEntity()).thenReturn(new Object());

    // Act
    entityWriterInterceptor.aroundWriteTo(contextMock);

    // Assert
    verify(contextMock, never()).setEntity(any());
    verify(contextMock).proceed();
  }

}
