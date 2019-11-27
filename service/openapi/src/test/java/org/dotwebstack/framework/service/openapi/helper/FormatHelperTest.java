package org.dotwebstack.framework.service.openapi.helper;

import static org.dotwebstack.framework.service.openapi.helper.FormatHelper.formatGraphQlQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FormatHelperTest {

  @Test
  public void formatQuery_returnsFormattedQuery_forGraphQlQuery() {
    // Arrange
    String unformatted = "{ brewery(identifier: \"123\") { identifier, name, founded }}";
    String expected = " {\n" + "\t brewery(identifier: \"123\")  {\n" + "\t\t identifier,\n" + "\t\t name,\n"
        + "\t\t founded \n" + "\t}\n" + "}";
    // Act
    String result = formatGraphQlQuery(unformatted);

    // Assert
    assertEquals(expected, result);
  }

  @Test
  public void formatQuery_returnsFormattedQuery_forMultiLayeredGraphQlQuery() {
    // Arrange
    String unformatted = "{ brewery(identifier: \"123\") { identifier, name, address { postalCode }}}";
    String expected = " {\n" + "\t brewery(identifier: \"123\")  {\n" + "\t\t identifier,\n" + "\t\t name,\n"
        + "\t\t address  {\n" + "\t\t\t postalCode \n" + "\t\t}\n" + "\t}\n" + "}";
    // Act
    String result = formatGraphQlQuery(unformatted);

    // Assert
    assertEquals(expected, result);
  }

  @Test
  public void formatQuery_returnsFormattedQuery_forMultiArgumentGraphQlQuery() {
    // Arrange
    String unformatted = "{ breweriesWithInputObject(input: {nestedInput: {nestedNestedInput: {"
        + "name: [\"Heineken Nederland\", \"Brouwerij De Leckere\"]},"
        + "foundedAfter: \"1800-01-01T00:00:00+02:00\"}}) { identifier, name }}";
    String expected = " {\n" + "\t breweriesWithInputObject(input:  {\n" + "\t\tnestedInput:  {\n"
        + "\t\t\tnestedNestedInput:  {\n" + "\t\t\t\tname: [\"Heineken Nederland\",\n"
        + "\t\t\t\t \"Brouwerij De Leckere\"]\n" + "\t\t\t},\n" + "\t\t\tfoundedAfter: \"1800-01-01T00:00:00+02:00\"\n"
        + "\t\t}\n" + "\t})  {\n" + "\t\t identifier,\n" + "\t\t name \n" + "\t}\n" + "}";
    // Act
    String result = formatGraphQlQuery(unformatted);

    // Assert
    assertEquals(expected, result);
  }
}
