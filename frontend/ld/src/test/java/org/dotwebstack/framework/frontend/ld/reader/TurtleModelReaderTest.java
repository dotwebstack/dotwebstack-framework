package org.dotwebstack.framework.frontend.ld.reader;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.dotwebstack.framework.test.DBEERPEDIA;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@RunWith(MockitoJUnitRunner.class)
public class TurtleModelReaderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Type type;

  private Annotation[] annotations;

  @Mock
  private MediaType mediaType;

  @Mock
  private MultivaluedMap<String, String> multiValuedMap;

  private TurtleModelReader turtleModelReader;

  @Before
  public void setup() {
    turtleModelReader = new TurtleModelReader();
  }

  @Test
  public void isReadable_ReturnTrue_WhenInputIsModelClass() {
    // Act/Assert
    assertTrue(turtleModelReader.isReadable(Model.class, type, annotations, mediaType));
  }

  @Test
  public void isReadable_ReturnFalse_WhenInputIsNotaModelClass() {
    // Act/Assert
    assertFalse(turtleModelReader.isReadable(MediaType.class, type, annotations, mediaType));
  }

  @Test
  public void readFrom_GetValidModel_WithValidTurtle() throws IOException {
    // Arrange
    Resource turtle = new ClassPathResource("/modelreader/turtle.ttl");

    // Act
    Model model = turtleModelReader.readFrom(Model.class, type, annotations, mediaType,
        multiValuedMap, turtle.getInputStream());

    // Assert
    assertTrue(model.contains(DBEERPEDIA.BREWERIES, RDF.TYPE, DBEERPEDIA.BACKEND));
    assertTrue(model.contains(DBEERPEDIA.BREWERIES, RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL));
  }

  @Test
  public void readFrom_ThrowException_WithInvalidTurtle() throws IOException {
    // Arrange
    InputStream turtle = new ByteArrayInputStream("invalid data".getBytes());

    // Assert
    thrown.expect(RuntimeException.class);

    // Act
    turtleModelReader.readFrom(Model.class, type, annotations, mediaType, multiValuedMap, turtle);
  }

}
