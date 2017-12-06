package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

public class RequestParametersTest {

  @Test
  public void putAll_MapContainsValues_WithMultivaluedMap() {
    MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
    multivaluedMap.put("X", ImmutableList.of("A"));
    RequestParameters parameters = new RequestParameters();

    parameters.putAll(multivaluedMap);

    Assert.assertEquals("A", parameters.get("X"));
  }

  @Test
  public void putAll_MapContainsValues_WithMap() {
    RequestParameters parameters = new RequestParameters();

    parameters.putAll(ImmutableMap.of("X", "A"));

    Assert.assertEquals("A", parameters.get("X"));
  }

  @Test
  public void asString_ReturnsString_ForExistingEntry() {
    RequestParameters parameters = new RequestParameters();
    parameters.putAll(ImmutableMap.of("X", "A"));

    String result = parameters.asString("X");

    Assert.assertEquals("A", result);
  }

  @Test
  public void asString_ReturnsNull_ForNonexistentEntry() {
    RequestParameters parameters = new RequestParameters();
    parameters.putAll(ImmutableMap.of("X", "A"));

    String result = parameters.asString("Y");

    Assert.assertNull(result);
  }

  @Test
  public void get_ReturnsValue_ForExistentKey() {
    RequestParameters parameters = new RequestParameters();
    parameters.putAll(ImmutableMap.of("X", "A"));

    Object result = parameters.get("X");

    Assert.assertEquals("A", result);
  }

  @Test
  public void get_ReturnsNull_ForNonexistentKey() {
    RequestParameters parameters = new RequestParameters();
    parameters.putAll(ImmutableMap.of("X", "A"));

    Object result = parameters.get("Y");

    Assert.assertNull(result);
  }

}

