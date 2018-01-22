package org.dotwebstack.framework.frontend.openapi.handlers;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Assert;
import org.junit.Test;

public class RequestParametersTest {

  @Test
  public void putAll_MapContainsValues_WithMultivaluedMap() {
    MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
    multivaluedMap.put("X", ImmutableList.of("A", "B"));
    RequestParameters parameters = new RequestParameters();

    parameters.putAll(multivaluedMap);

    Assert.assertEquals("A", parameters.get("X"));
  }

  @Test
  public void put_MapContainsValues_WithKeyValue() {
    RequestParameters parameters = new RequestParameters();

    parameters.put("X", "A");
    parameters.put("Y", "B");
    parameters.put("Z", "C");

    Assert.assertEquals("A", parameters.get("X"));
  }

  @Test
  public void get_ReturnsValue_ForExistentKey() {
    RequestParameters parameters = new RequestParameters();
    parameters.put("X", "A");

    Object result = parameters.get("X");

    Assert.assertEquals("A", result);
  }

  @Test
  public void get_ReturnsNull_ForNonExistentKey() {
    RequestParameters parameters = new RequestParameters();
    parameters.put("X", "A");

    Object result = parameters.get("Y");

    Assert.assertNull(result);
  }

}

