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
public class RdfXmlModelReaderTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Type type;

  private Annotation[] annotations;

  @Mock
  private MediaType mediaType;

  @Mock
  private MultivaluedMap<String, String> multiValuedMap;

  private RdfXmlModelReader rdfXmlModelReader;

  @Before
  public void setup() {
    rdfXmlModelReader = new RdfXmlModelReader();
  }

  @Test
  public void isReadable_ReturnTrue_WhenInputIsModelClass() {
    // Act/Assert
    assertTrue(rdfXmlModelReader.isReadable(Model.class, type, annotations, mediaType));
  }

  @Test
  public void isReadable_ReturnFalse_WhenInputIsNotaModelClass() {
    // Act/Assert
    assertFalse(rdfXmlModelReader.isReadable(MediaType.class, type, annotations, mediaType));
  }

  @Test
  public void readFrom_GetValidModel_WithValidRdfXml() throws IOException {
    // Arrange
    Resource rdfXml = new ClassPathResource("/modelreader/rdfXml.xml");

    // Act
    Model model = rdfXmlModelReader.readFrom(Model.class, type, annotations, mediaType,
        multiValuedMap, rdfXml.getInputStream());

    // Assert
    assertTrue(model.contains(DBEERPEDIA.BREWERIES, RDF.TYPE, DBEERPEDIA.BACKEND));
    assertTrue(model.contains(DBEERPEDIA.BREWERIES, RDFS.LABEL, DBEERPEDIA.BREWERIES_LABEL));
  }

  @Test
  public void readFrom_ThrowException_WithInvalidRdfXml() throws IOException {
    // Arrange
    InputStream rdfXml = new ByteArrayInputStream("invalid data".getBytes());

    // Assert
    thrown.expect(RuntimeException.class);

    // Act
    rdfXmlModelReader.readFrom(Model.class, type, annotations, mediaType, multiValuedMap, rdfXml);
  }

}
