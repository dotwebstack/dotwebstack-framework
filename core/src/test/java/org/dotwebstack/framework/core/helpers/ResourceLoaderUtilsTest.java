package org.dotwebstack.framework.core.helpers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import org.junit.jupiter.api.Test;

public class ResourceLoaderUtilsTest {

  @Test
  void assert_notNull_AssetsResource() {
    // Arrange & Act
    URI resource = ResourceLoaderUtils.getResourceLocation("assets/");

    // Assert
    assertNotNull(resource);
  }
}
