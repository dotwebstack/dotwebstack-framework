package org.dotwebstack.framework.frontend.openapi.handlers;

import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class RequestParametersTest {

  @Test
  public void testClearParameters() {
    RequestParameters parameters = new RequestParameters();
    parameters.put("X", 123);
    parameters.put("Y", 123);
    parameters.put("Z", 123);

    parameters.cleanParameters("X", "Z");

    Assert.assertNull(parameters.get("X"));
    Assert.assertNull(parameters.get("Z"));
    Assert.assertEquals(123, parameters.get("Y"));
  }

  @Test
  public void testAsBoolean() {
    RequestParameters parameters = new RequestParameters();

    parameters.put("X", true);

    Assert.assertFalse(parameters.asBoolean("Y"));
    Assert.assertTrue(parameters.asBoolean("X"));
  }

  @Test
  public void testAsInt() {
    RequestParameters parameters = new RequestParameters();

    parameters.put("X", 1);

    Assert.assertNull(parameters.asInt("Y"));
    Assert.assertEquals(Integer.valueOf(1), parameters.asInt("X"));
  }

  @Test
  public void testAsIntWithDefault() {
    RequestParameters parameters = new RequestParameters();

    parameters.put("X", 1);

    Assert.assertEquals(Integer.valueOf(2), parameters.asInt("Y", 2));
    Assert.assertEquals(Integer.valueOf(1), parameters.asInt("X", 2));
  }

  @Test
  public void testAsString() {
    RequestParameters parameters = new RequestParameters();

    parameters.put("X", "A");

    Assert.assertNull(parameters.asString("Y"));
    Assert.assertEquals("A", parameters.asString("X"));
  }

  @Test
  public void testPutIfAbsent() {
    RequestParameters parameters = new RequestParameters();

    parameters.put("X", "A");
    parameters.putIfAbsent("X", "B");
    parameters.putIfAbsent("Y", "C");

    Assert.assertEquals("C", parameters.asString("Y"));
    Assert.assertEquals("A", parameters.asString("X"));
  }

  @Test
  public void testAsMap() {
    RequestParameters parameters = new RequestParameters();

    parameters.put("X", "A");
    parameters.put("Y", "B");

    Assert.assertEquals(parameters.asMap(), ImmutableMap.of("X", "A", "Y", "B"));
  }

  @Test
  public void testPutAll() {
    RequestParameters parameters = new RequestParameters();
    parameters.put("X", "A");

    RequestParameters parameters2 = new RequestParameters();
    parameters2.putAll(parameters);

    Assert.assertThat(parameters2.asMap(), Matchers.hasEntry("X", "A"));
  }



}

