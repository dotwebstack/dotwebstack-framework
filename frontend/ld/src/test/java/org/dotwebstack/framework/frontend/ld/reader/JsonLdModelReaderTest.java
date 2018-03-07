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
public class JsonLdModelReaderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Type type;

  private Annotation[] annotations;

  @Mock
  private MediaType mediaType;

  @Mock
  private MultivaluedMap<String, String> multiValuedMap;

  private JsonLdModelReader jsonLdModelReader;

  @Before
  public void setup() {
    jsonLdModelReader = new JsonLdModelReader();
  }

  @Test
  public void isReadable_ReturnTrue_WhenInputIsModelClass() {
    // Act/Assert
    assertTrue(jsonLdModelReader.isReadable(Model.class, type, annotations, mediaType));
  }

  @Test
  public void isReadable_ReturnFalse_WhenInputIsNotaModelClass() {
    // Act/Assert
    assertFalse(jsonLdModelReader.isReadable(MediaType.class, type, annotations, mediaType));
  }

  @Test
  public void readFrom_GetValidModel_WithValidJsonLd() throws IOException {
    // Arrange
    InputStream jsonLd = new ByteArrayInputStream(("{\n"
        + "  \"@context\": {\n"
        + "    \"rdf\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n"
        + "    \"rdfs\": \"http://www.w3.org/2000/01/rdf-schema#\",\n"
        + "    \"xsd\": \"http://www.w3.org/2001/XMLSchema#\"\n"
        + "  },\n"
        + "  \"@id\": \"http://dbeerpedia.org#Breweries\",\n"
        + "  \"@type\": \"http://dbeerpedia.org#Backend\",\n"
        + "  \"rdfs:label\": \"Beer breweries in The Netherlands\"\n"
        + "}").getBytes());

    // Act
    Model model = jsonLdModelReader.readFrom(Model.class, type, annotations, mediaType,
        multiValuedMap, jsonLd);

    // Assert
    assertTrue(model.contains(DBEERPEDIA.BREWERIES, RDF.TYPE, DBEERPEDIA.BACKEND));
    assertTrue(model.contains(DBEERPEDIA.BREWERIES, RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL));
  }

  @Test
  public void readFrom_ThrowException_WithInvalidJsonLd() throws IOException {
    // Arrange
    InputStream jsonLd = new ByteArrayInputStream("invalid data".getBytes());

    // Assert
    thrown.expect(RuntimeException.class);

    // Act
    jsonLdModelReader.readFrom(Model.class, type, annotations, mediaType, multiValuedMap, jsonLd);
  }

}
