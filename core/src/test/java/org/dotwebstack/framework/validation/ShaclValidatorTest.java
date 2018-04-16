package org.dotwebstack.framework.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Collections;
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
  private Resource validDataResource;

  @Mock
  private Resource invalidDataResource;

  @Mock
  private Resource shapesResource;

  @Mock
  private Resource invalidDataWithoutPrefResource;

  @Mock
  private Resource prefixesResource;

  @Mock
  private Resource validDataWithoutPrefResource;

  @Mock
  private Resource invalidDataMultipleErrorsResource;

  @Before
  public void setUp() throws Exception {
    shaclValidator = new ShaclValidator();
    shapesResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/shapes.trig").getInputStream());
    validDataResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/validData.trig").getInputStream());
    invalidDataResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/invalidData.trig").getInputStream());
    invalidDataWithoutPrefResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/invalidDataWithoutPref.trig").getInputStream());
    validDataWithoutPrefResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/validDataWithoutPref.trig").getInputStream());
    prefixesResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/_prefixes.trig").getInputStream());
    invalidDataMultipleErrorsResource = new InputStreamResource(
        new ClassPathResource("/shaclvalidation/invalidDataMultipleErrors.trig").getInputStream());
  }

  @Test
  public void validate_NoErrors_validConfiguration() throws Exception {
    // Act
    final ValidationReport report =
        shaclValidator.validate(RdfModelTransformer.getModel(validDataResource.getInputStream()),
            RdfModelTransformer.getModel(shapesResource.getInputStream()));

    // Assert
    assertThat(report.isValid(), equalTo(true));
    assertThat(report.getErrors(), equalTo(Collections.EMPTY_MAP));
  }

  @Test
  public void validate_getShaclValidationReport_invalidConfiguration() throws Exception {
    // Act
    final ValidationReport report =
        shaclValidator.validate(RdfModelTransformer.getModel(invalidDataResource.getInputStream()),
            RdfModelTransformer.getModel(shapesResource.getInputStream()));

    // Assert
    assertThat(report.isValid(), equalTo(false));
    assertThat(report.getErrors().size(), equalTo(1));
    final String errorKey = report.getErrors().keySet().iterator().next();
    assertThat(report.getErrors().get(errorKey).getReport(),
        equalTo("Invalid configuration at path [http://example.org#lid] on node "
            + "[http://example.org#HuwelijkMarcoNanda] with error message [More than 2 values]"));
  }

  @Test
  public void validate_getShaclValidationReportMultipleErrors_invalidConfiguration()
      throws Exception {
    // Act
    final ValidationReport report = shaclValidator.validate(
        RdfModelTransformer.getModel(invalidDataMultipleErrorsResource.getInputStream()),
        RdfModelTransformer.getModel(shapesResource.getInputStream()));

    // Assert
    assertThat(report.isValid(), equalTo(false));
    assertThat(report.getErrors().size(), equalTo(3));
    assertThat(report.getErrors().values().stream().map(Violation::getReport).filter(
        ("Invalid configuration at path [http://example.org#lid] on node "
            + "[http://example.org#HuwelijkMarcoNanda] with error message "
            + "[More than 2 values]")::equals).findFirst().isPresent(),
        equalTo(true));
    assertThat(report.getErrors().values().stream().map(Violation::getReport).filter(
        ("Invalid configuration at path [http://example.org#lid] on node "
            + "[http://example.org#HuwelijkMarcoNanda] with error message "
            + "[Value does not have shape <http://example.org/shape#Persoon>]")::equals).findFirst().isPresent(),
        equalTo(true));
    assertThat(report.getErrors().values().stream().map(Violation::getReport).filter(
        ("Invalid configuration at path [http://example.org#naam] on node [http://example.org#Bobby]"
            + " with error message [More than 1 values]")::equals).findFirst().isPresent(),
        equalTo(true));
  }

  @Test
  public void validate_isNotValid_invalidConfigurationWithPrefixes() throws Exception {
    // Act
    final ValidationReport report = shaclValidator.validate(
        RdfModelTransformer.mergeResourceWithPrefixes(prefixesResource.getInputStream(),
            invalidDataWithoutPrefResource.getInputStream()),
        RdfModelTransformer.getModel(shapesResource.getInputStream()));

    // Assert
    assertThat(report.isValid(), equalTo(false));
    assertThat(report.getErrors().size(), equalTo(1));
    final String errorKey = report.getErrors().keySet().iterator().next();
    assertThat(report.getErrors().get(errorKey).getReport(),
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
        RdfModelTransformer.getModel(shapesResource.getInputStream()));

    // Assert
    assertThat(report.isValid(), equalTo(true));
  }
}
