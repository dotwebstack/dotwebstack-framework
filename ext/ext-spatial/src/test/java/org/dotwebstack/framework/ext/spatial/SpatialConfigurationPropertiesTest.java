package org.dotwebstack.framework.ext.spatial;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

class SpatialConfigurationPropertiesTest {

  private final SpatialConfigurationProperties configurationProperties = new SpatialConfigurationProperties();

  @Test
  void setSourceCrs_doNothing_forValidString() {
    configurationProperties.setSourceCrs("EPSG:4258");
  }

  @Test
  void setSourceCrs_doNothing_forNull() {
    configurationProperties.setSourceCrs(null);
  }

  @Test
  void setSourceCrs_doNothing_forEmptyString() {
    configurationProperties.setSourceCrs("");
  }

  @Test
  void setSourceCrs_throwsException_forInvalidFormat() {
    assertThrows(InvalidConfigurationException.class, () -> configurationProperties.setSourceCrs("4258"));
  }
}
