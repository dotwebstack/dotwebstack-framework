package org.dotwebstack.framework.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class ShaclValidatorTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  private ShaclValidator shaclValidator;

  @Mock
  private Resource validDataResoource;

  @Mock
  private Resource invalidDataResoource;

  @Mock
  private Resource shapesResource;

  @Mock
  private Resource dataResource;

  @Mock
  private Resource invalidDataWithoutPrefResoource;

  @Mock
  private Resource prefixesResource;

  @Mock
  private Resource validDataWithoutPrefResource;

  @Before
  public void setUp() throws Exception {
    shaclValidator = new ShaclValidator();
    dataResource = mock(Resource.class);
    prefixesResource = mock(Resource.class);
    shapesResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/shapes.trig").getInputStream());
    validDataResoource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/validData.trig").getInputStream());
    invalidDataResoource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/invalidData.trig").getInputStream());
    invalidDataWithoutPrefResoource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/invalidDataWithoutPref.trig").getInputStream());
    validDataWithoutPrefResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/validDataWithoutPref.trig").getInputStream());
    prefixesResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/_prefixes.trig").getInputStream());
  }

  @Test
  public void validate_NoError_validConfiguration() throws Exception {
    // Act / Assert
    shaclValidator.validate(validDataResoource.getInputStream(), shapesResource);
  }

  @Test
  public void validate_throwShaclValidationException_invalidConfiguration() throws Exception {
    // Assert
    thrown.expect(ShaclValidationException.class);

    // Act
    shaclValidator.validate(invalidDataResoource.getInputStream(), shapesResource);
  }

  @Test
  public void validate_throwShaclValidationException_WithIoException() throws Exception {
    // Arrange
    when(dataResource.getInputStream()).thenThrow(IOException.class);
    InputStream resource = mock(InputStream.class);

    // Assert
    thrown.expect(ShaclValidationException.class);
    thrown.expectMessage("File could not read during the validation process");

    // Act
    shaclValidator.validate(resource, shapesResource);
  }

  @Test
  public void validate_throwShaclValidationException_WithIoExceptionPrefixes()
      throws Exception {
    // Arrange
    final Resource ioExceptionResource = mock(Resource.class);
    when(ioExceptionResource.getInputStream()).thenThrow(IOException.class);

    // Assert
    thrown.expect(ShaclValidationException.class);
    thrown.expectMessage("File could not read during the validation process");

    // Act
    shaclValidator.validate(dataResource.getInputStream(), shapesResource, ioExceptionResource);
  }

  @Test
  public void validate_throwShaclValidationException_WithIoExceptionDataShape()
      throws Exception {
    // Arrange
    Resource prefixesResource = mock(Resource.class);
    when(prefixesResource.getInputStream()).thenReturn(
        new ByteArrayInputStream("@prefix dbeerpedia: <http://dbeerpedia.org#> .".getBytes()));
    Resource ioExceptionShapesResource = mock(Resource.class);
    when(ioExceptionShapesResource.getInputStream()).thenThrow(IOException.class);
    InputStream resource = mock(InputStream.class);

    // Assert
    thrown.expect(ShaclValidationException.class);
    thrown.expectMessage("File could not read during the validation process");

    // Act
    shaclValidator.validate(resource, ioExceptionShapesResource, prefixesResource);
  }

  @Test
  public void validate_throwShaclValidationException_invalidConfigurationWithPrefixes()
      throws Exception {
    // Assert
    thrown.expect(ShaclValidationException.class);
    thrown.expectMessage(
        "Invalid configuration at path [http://example.org#lid] on node [http://example.org#HuwelijkMarcoNanda] with error message [More than 2 values]");

    // Act
    shaclValidator.validate(invalidDataWithoutPrefResoource.getInputStream(), shapesResource,
        prefixesResource);
  }

  @Test
  public void validate_NoError_validConfigurationWithPrefixes() throws Exception {
    // Act / Assert
    shaclValidator
        .validate(validDataWithoutPrefResource.getInputStream(), shapesResource, prefixesResource);
  }
}
