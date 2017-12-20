package org.dotwebstack.framework.frontend.openapi.entity;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LdPathExecutorTest {

  @Mock
  private GraphEntityContext entityContextMock;

  @Before
  public void setUp() {}

  @Test
  public void ldPathQuery_ReturnsResult_ForQuery() {

    // Arrange
    when(entityContextMock.getLdPathNamespaces()).thenReturn(
        ImmutableMap.of("beer", "http://dbeerpedia.org#"));

    Model model = new ModelBuilder().subject(DBEERPEDIA.BROUWTOREN).add(RDF.TYPE,
        DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME, DBEERPEDIA.BROUWTOREN_NAME).subject(
            DBEERPEDIA.MAXIMUS).add(RDF.TYPE, DBEERPEDIA.BREWERY_TYPE).add(DBEERPEDIA.NAME,
                DBEERPEDIA.MAXIMUS_NAME).build();
    when(entityContextMock.getModel()).thenReturn(model);

    LdPathExecutor executor = new LdPathExecutor(entityContextMock);

    // Act
    Collection<Value> result = executor.ldPathQuery(DBEERPEDIA.BROUWTOREN, "beer:Name");

    // Assert
    assertThat(result, contains(DBEERPEDIA.BROUWTOREN_NAME));
  }

}
