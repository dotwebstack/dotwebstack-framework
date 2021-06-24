package org.dotwebstack.framework.ext.spatial;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

class SpatialConfigurationPropertiesTest {

  private final SpatialConfigurationProperties configurationProperties = new SpatialConfigurationProperties();

  @Test
  void setSourceCrs_doNothing_forValidString() {
    configurationProperties.setSourceCrs("EPSG:4258");

    assertThat(configurationProperties.getSourceCrs(), equalTo("EPSG:4258"));
  }

  @Test
  void setSourceCrs_doNothing_forNull() {
    configurationProperties.setSourceCrs(null);

    assertThat(configurationProperties.getSourceCrs(), nullValue());
  }

  @Test
  void setSourceCrs_doNothing_forEmptyString() {
    configurationProperties.setSourceCrs("");

    assertThat(configurationProperties.getSourceCrs(), nullValue());
  }

  @Test
  void setSourceCrs_throwsException_forInvalidFormat() {
    assertThrows(InvalidConfigurationException.class, () -> configurationProperties.setSourceCrs("4258"));
  }
}
