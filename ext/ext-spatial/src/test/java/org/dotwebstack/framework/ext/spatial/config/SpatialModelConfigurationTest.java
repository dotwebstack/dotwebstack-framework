package org.dotwebstack.framework.ext.spatial.config;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.validation.ConstraintViolationException;
import java.io.File;
import java.net.MalformedURLException;
import org.dotwebstack.framework.ext.spatial.backend.SpatialBackendModule;
import org.dotwebstack.framework.ext.spatial.testhelper.TestSpatialBackendModule;
import org.dotwebstack.framework.ext.spatial.testhelper.TestSpatialReferenceSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpatialModelConfigurationTest {

  private SpatialModelConfiguration modelConfiguration;

  @BeforeEach
  void setUp() {
    SpatialBackendModule<TestSpatialReferenceSystem> spatialBackendModule = new TestSpatialBackendModule();
    modelConfiguration = new SpatialModelConfiguration(spatialBackendModule);
  }

  @Test
  void spatial_returnsSpatial() {
    String localUrl = getDotWebStackConfiguration("src/test/resources/config/dotwebstack/dotwebstack-spatial.yaml");

    var result = modelConfiguration.spatial(localUrl);

    assertThat(result, is(notNullValue()));
    assertThat(result.getReferenceSystems(),
        allOf(hasEntry(is(28992), is(createTestSpatialReferenceSystem(2, 4, null, null))),
            hasEntry(is(7415), is(createTestSpatialReferenceSystem(3, 4, 28992, null))),
            hasEntry(is(9067), is(createTestSpatialReferenceSystem(2, 9, null, null))),
            hasEntry(is(7931), is(createTestSpatialReferenceSystem(3, 9, 9067, "test")))));
    assertThat(result.isUseWorkaroundForIntersects(), is(true));
  }

  @Test
  void spatial_returnsEmptySpatial_ifNoSpatialConfig() {
    String localUrl = getDotWebStackConfiguration("src/test/resources/config/dotwebstack/dotwebstack-no-spatial.yaml");

    var result = modelConfiguration.spatial(localUrl);

    assertThat(result, is(notNullValue()));
    assertThat(result.getReferenceSystems(), is(nullValue()));
  }

  @Test
  void spatial_throwsException_forMissingProperty() {
    String localUrl =
        getDotWebStackConfiguration("src/test/resources/config/dotwebstack/dotwebstack-spatial-invalid.yaml");

    ConstraintViolationException exception =
        assertThrows(ConstraintViolationException.class, () -> modelConfiguration.spatial(localUrl));

    assertThat(exception.getMessage(), containsString("dotwebstack-spatial-invalid.yaml is not valid. Reasons (1):"));
  }

  private TestSpatialReferenceSystem createTestSpatialReferenceSystem(Integer dimensions, Integer scale,
      Integer equivalent, String extraInfo) {
    TestSpatialReferenceSystem srs = new TestSpatialReferenceSystem();
    srs.setDimensions(dimensions);
    srs.setScale(scale);
    srs.setEquivalent(equivalent);
    srs.setExtraInfo(extraInfo);
    return srs;
  }

  private String getDotWebStackConfiguration(String path) {
    File file = new File(path);

    try {
      return file.toURI()
          .toURL()
          .toExternalForm();
    } catch (MalformedURLException e) {
      throw new AssertionError(String.format("Invalid file path '%s'.", path));
    }
  }
}
