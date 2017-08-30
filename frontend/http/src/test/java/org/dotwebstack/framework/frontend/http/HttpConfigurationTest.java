package org.dotwebstack.framework.frontend.http;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class HttpConfigurationTest {

  @Test
  public void construct() {
    new HttpConfiguration(ImmutableList.of());
  }

}
