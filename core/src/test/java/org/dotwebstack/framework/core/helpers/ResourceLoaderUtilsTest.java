package org.dotwebstack.framework.core.helpers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import org.dotwebstack.framework.core.CoreProperties;
import org.junit.jupiter.api.Test;

public class ResourceLoaderUtilsTest {

  @Test
  void assert_notNull_AssetsResource() {
    ResourceLoaderUtils resourceLoaderUtils = new ResourceLoaderUtils(new CoreProperties());
    URI resource = resourceLoaderUtils.getResourceLocation("assets/");

    assertNotNull(resource);
  }
}
