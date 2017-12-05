package org.dotwebstack.framework.validation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
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
  private Resource invalidDataResource;

  @Mock
  private Resource shapesResource;

  @Mock
  private Resource dataResource;

  @Mock
  private Resource invalidDataWithoutPrefResource;

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
    invalidDataResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/invalidData.trig").getInputStream());
    invalidDataWithoutPrefResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/invalidDataWithoutPref.trig").getInputStream());
    validDataWithoutPrefResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/validDataWithoutPref.trig").getInputStream());
    prefixesResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/_prefixes.trig").getInputStream());
  }

  @Test
  public void validate_NoError_validConfiguration() throws Exception {
    // Act / Assert
    shaclValidator.validate(RdfModelTransformer.getModel(validDataResoource),
        RdfModelTransformer.getModel(shapesResource));
  }

  @Test
  public void validate_getShaclValidationReport_invalidConfiguration() throws Exception {
    // Act
    final ValidationReport report =
        shaclValidator.validate(RdfModelTransformer.getModel(invalidDataResource),
            RdfModelTransformer.getModel(shapesResource));

    // Assert
    assertThat(report.isValid(), equalTo(false));
    assertThat(report.getValidationReport(),
        equalTo("Invalid configuration at path "
            + "[http://example.org#lid] on node [http://example.org#HuwelijkMarcoNanda] "
            + "with error message [More than 2 values]"));
  }

  @Test
  public void validate_throwShaclValidationException_WithIoException() throws Exception {
    // Arrange
    when(dataResource.getInputStream()).thenThrow(IOException.class);

    // Assert
    thrown.expect(IOException.class);

    // Act
    shaclValidator.validate(RdfModelTransformer.getModel(dataResource),
        RdfModelTransformer.getModel(shapesResource));
  }

  @Test
  public void validate_throwShaclValidationException_WithIoExceptionPrefixes() throws Exception {
    // Arrange
    final Resource ioExceptionResource = mock(Resource.class);
    when(ioExceptionResource.getInputStream()).thenThrow(IOException.class);

    // Assert
    thrown.expect(IOException.class);

    // Act
    shaclValidator.validate(
        RdfModelTransformer.mergeResourceWithPrefixes(dataResource.getInputStream(),
            ioExceptionResource.getInputStream()),
        RdfModelTransformer.getModel(shapesResource));
  }

  @Test
  public void validate_throwShaclValidationException_WithIoExceptionDataShape() throws Exception {
    // Arrange
    Resource prefixesResource = mock(Resource.class);
    when(prefixesResource.getInputStream()).thenReturn(
        new ByteArrayInputStream("@prefix dbeerpedia: <http://dbeerpedia.org#> .".getBytes()));
    Resource ioExceptionShapesResource = mock(Resource.class);
    when(ioExceptionShapesResource.getInputStream()).thenThrow(IOException.class);
    InputStream resource = mock(InputStream.class);

    // Assert
    thrown.expect(IOException.class);

    // Act
    shaclValidator.validate(
        RdfModelTransformer.mergeResourceWithPrefixes(prefixesResource.getInputStream(), resource),
        RdfModelTransformer.getModel(ioExceptionShapesResource));
  }

  @Test
  public void validate_isNotValid_invalidConfigurationWithPrefixes() throws Exception {
    // Act
    final ValidationReport report = shaclValidator.validate(
        RdfModelTransformer.mergeResourceWithPrefixes(prefixesResource.getInputStream(),
            invalidDataWithoutPrefResource.getInputStream()),
        RdfModelTransformer.getModel(shapesResource));

    // Assert
    assertThat(report.isValid(), equalTo(false));
    assertThat(report.getValidationReport(),
        equalTo("Invalid configuration at path "
            + "[http://example.org#lid] on node [http://example.org#HuwelijkMarcoNanda] with error "
            + "message [More than 2 values]"));
  }

  @Test
  public void validate_NoError_validConfigurationWithPrefixes() throws Exception {
    // Act
    final ValidationReport report = shaclValidator.validate(
        RdfModelTransformer.mergeResourceWithPrefixes(prefixesResource.getInputStream(),
            validDataWithoutPrefResource.getInputStream()),
        RdfModelTransformer.getModel(shapesResource));

    // Assert
    assertThat(report.isValid(), equalTo(true));
  }
}
