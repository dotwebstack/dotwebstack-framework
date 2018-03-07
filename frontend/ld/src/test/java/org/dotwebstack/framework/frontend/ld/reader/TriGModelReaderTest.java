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

@RunWith(MockitoJUnitRunner.class)
public class TriGModelReaderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Type type;

  private Annotation[] annotations;

  @Mock
  private MediaType mediaType;

  @Mock
  private MultivaluedMap<String, String> multiValuedMap;

  private TriGModelReader triGModelReader;

  @Before
  public void setup() {
    triGModelReader = new TriGModelReader();
  }

  @Test
  public void isReadable_ReturnTrue_WhenInputIsModelClass() {
    // Act/Assert
    assertTrue(triGModelReader.isReadable(Model.class, type, annotations, mediaType));
  }

  @Test
  public void isReadable_ReturnFalse_WhenInputIsNotaModelClass() {
    // Act/Assert
    assertFalse(triGModelReader.isReadable(MediaType.class, type, annotations, mediaType));
  }

  @Test
  public void readFrom_GetValidModel_WithValidTriG() throws IOException {
    // Arrange
    InputStream triG = new ByteArrayInputStream(("@prefix rdf: "
        + "<http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"
        + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"
        + "@prefix xml: <http://www.w3.org/XML/1998/namespace> .\n"
        + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
        + "\n"
        + "<http://dbeerpedia.org#Breweries> a <http://dbeerpedia.org#Backend> ;\n"
        + "    rdfs:label \"Beer breweries in The Netherlands\" .").getBytes());

    // Act
    Model model = triGModelReader.readFrom(Model.class, type, annotations, mediaType,
        multiValuedMap, triG);

    // Assert
    assertTrue(model.contains(DBEERPEDIA.BREWERIES, RDF.TYPE, DBEERPEDIA.BACKEND));
    assertTrue(model.contains(DBEERPEDIA.BREWERIES, RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL));
  }

  @Test
  public void readFrom_ThrowException_WithInvalidTriG() throws IOException {
    // Arrange
    InputStream triG = new ByteArrayInputStream("invalid data".getBytes());

    // Assert
    thrown.expect(RuntimeException.class);

    // Act
    triGModelReader.readFrom(Model.class, type, annotations, mediaType, multiValuedMap, triG);
  }

}
