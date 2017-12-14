package org.dotwebstack.framework.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import org.eclipse.rdf4j.model.Model;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RdfModelTransformerTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Mock
  private Resource validDataResource;

  @Mock
  private Resource prefixesResource;

  @Mock
  private Resource validDataWithoutPrefResource;

  @Before
  public void setUp() throws Exception {
    validDataResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/validData.trig").getInputStream());
    prefixesResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/_prefixes.trig").getInputStream());
    validDataWithoutPrefResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/validDataWithoutPref.trig").getInputStream());
  }

  @Test
  public void getRdf4jModel_IoExceptionDataResource_ThrowsIoException() throws Exception {
    // Arrange
    Resource ioExceptionResource = mock(Resource.class);
    when(ioExceptionResource.getInputStream()).thenThrow(IOException.class);

    // Assert
    thrown.expect(IOException.class);

    // Act
    RdfModelTransformer.getModel(ioExceptionResource.getInputStream());
  }

  @Test
  public void mergePrefixesWithResource_IoExceptionDataResource_ThrowsIoException()
      throws Exception {
    // Arrange
    Resource ioExceptionResource = mock(Resource.class);
    when(ioExceptionResource.getInputStream()).thenThrow(IOException.class);

    // Assert
    thrown.expect(IOException.class);

    // Act
    RdfModelTransformer.mergeResourceWithPrefixes(prefixesResource.getInputStream(),
        ioExceptionResource.getInputStream());
  }

  @Test
  public void mergePrefixesWithResource_IoExceptionPrefixesResource_ThrowsIoException()
      throws Exception {
    // Arrange
    Resource ioExceptionResource = mock(Resource.class);
    when(ioExceptionResource.getInputStream()).thenThrow(IOException.class);

    // Assert
    thrown.expect(IOException.class);

    // Act
    RdfModelTransformer.mergeResourceWithPrefixes(ioExceptionResource.getInputStream(),
        validDataResource.getInputStream());
  }

  @Test
  public void getRdf4jModel_NoErrors_ReturnRdf4jModel() throws Exception {
    // Act
    final Model rdf4jModel = RdfModelTransformer.getModel(validDataResource.getInputStream());

    // Assert
    assertNotNull(rdf4jModel);
    assertThat(rdf4jModel.toString(),
        equalTo("[(http://example.org#Marco, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, "
            + "http://example.org#Persoon) [null], (http://example.org#Marco, http://example.org#naam,"
            + " \"Marco\"^^<http://www.w3.org/2001/XMLSchema#string>) [null], "
            + "(http://example.org#Nanda, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, "
            + "http://example.org#Persoon) [null], (http://example.org#Nanda, http://example.org#naam,"
            + " \"Nanda\"^^<http://www.w3.org/2001/XMLSchema#string>) [null], "
            + "(http://example.org#HuwelijkMarcoNanda, "
            + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://example.org#Huwelijk) [null], "
            + "(http://example.org#HuwelijkMarcoNanda, http://example.org#lid, "
            + "http://example.org#Marco) [null], (http://example.org#HuwelijkMarcoNanda,"
            + " http://example.org#lid, http://example.org#Nanda) [null]]"));
  }

  @Test
  public void mergePrefixesWithResource_NoErrors_ReturnRdf4jModel() throws Exception {
    // Act
    final Model rdf4jModel = RdfModelTransformer.mergeResourceWithPrefixes(
        prefixesResource.getInputStream(), validDataWithoutPrefResource.getInputStream());

    // Assert
    assertNotNull(rdf4jModel);
    assertThat(rdf4jModel.toString(),
        equalTo("[(http://example.org#Marco, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, "
            + "http://example.org#Persoon) [null], (http://example.org#Marco, http://example.org#naam,"
            + " \"Marco\"^^<http://www.w3.org/2001/XMLSchema#string>) [null], "
            + "(http://example.org#Nanda, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, "
            + "http://example.org#Persoon) [null], (http://example.org#Nanda, http://example.org#naam,"
            + " \"Nanda\"^^<http://www.w3.org/2001/XMLSchema#string>) [null], "
            + "(http://example.org#HuwelijkMarcoNanda, "
            + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://example.org#Huwelijk) [null], "
            + "(http://example.org#HuwelijkMarcoNanda, http://example.org#lid, "
            + "http://example.org#Marco) [null], (http://example.org#HuwelijkMarcoNanda,"
            + " http://example.org#lid, http://example.org#Nanda) [null]]"));
  }

}
