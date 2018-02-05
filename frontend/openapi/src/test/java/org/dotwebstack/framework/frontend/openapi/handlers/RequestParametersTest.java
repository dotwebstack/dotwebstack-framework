package org.dotwebstack.framework.frontend.openapi.handlers;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Test;

public class RequestParametersTest {

  @Test
  public void putAll_StoresValues_ForMultivaluedMap() {
    // Arrange
    MultivaluedMap<String, String> input = new MultivaluedHashMap<>();

    input.put("X", ImmutableList.of("A"));
    input.put("Y", ImmutableList.of("B"));
    input.put("Z", ImmutableList.of("C"));

    RequestParameters parameters = new RequestParameters();

    // Act
    parameters.putAll(input);

    // Assert
    assertThat(parameters.get("X"), is("A"));
    assertThat(parameters.get("Y"), is("B"));
    assertThat(parameters.get("Z"), is("C"));
  }

  @Test
  public void putAll_UsesFirstValueForKeyOnly_ForMultivaluedMap() {
    // Arrange
    MultivaluedMap<String, String> input = new MultivaluedHashMap<>();

    input.put("X", ImmutableList.of("A", "B", "C"));

    RequestParameters parameters = new RequestParameters();

    // Act
    parameters.putAll(input);

    // Assert
    assertThat(parameters.get("X"), is("A"));
  }

  @Test
  public void put_StoresValue_ForNewValue() {
    // Arrange
    RequestParameters parameters = new RequestParameters();

    // Act
    parameters.put("X", "A");
    parameters.put("Y", "B");
    parameters.put("Z", "C");

    // Assert
    assertThat(parameters.get("X"), is("A"));
    assertThat(parameters.get("Y"), is("B"));
    assertThat(parameters.get("Z"), is("C"));
  }

  @Test
  public void put_OverwritesExistingValue_ForDuplicateValue() {
    // Arrange
    RequestParameters parameters = new RequestParameters();

    // Act
    parameters.put("X", "A");
    parameters.put("X", "B");

    // Assert
    assertThat(parameters.get("X"), is("B"));
  }

  @Test
  public void get_ReturnsValue_ForExistentKey() {
    // Arrange
    RequestParameters parameters = new RequestParameters();

    // Act
    parameters.put("X", "A");

    // Assert
    assertThat(parameters.get("X"), is("A"));
  }

  @Test
  public void get_ReturnsNull_ForNonExistentKey() {
    // Arrange
    RequestParameters parameters = new RequestParameters();

    // Act
    parameters.put("X", "A");

    // Assert
    assertThat(parameters.get("Y"), nullValue());
  }

  @Test
  public void put_StoresValue_IgnoringCase() {
    // Arrange
    RequestParameters parameters = new RequestParameters();

    // Act
    parameters.put("X", "A");
    parameters.put("y", "B");
    parameters.put("Zz", "C");

    // Assert
    assertThat(parameters.get("x"), is("A"));
    assertThat(parameters.get("Y"), is("B"));
    assertThat(parameters.get("zZ"), is("C"));
  }

  @Test
  public void get_ReturnsValue_IgnoringCase() {
    // Arrange
    RequestParameters parameters = new RequestParameters();

    // Act
    parameters.put("x", "A");
    parameters.put("Y", "B");
    parameters.put("zZ", "C");

    // Assert
    assertThat(parameters.get("X"), is("A"));
    assertThat(parameters.get("y"), is("B"));
    assertThat(parameters.get("Zz"), is("C"));
  }

}

