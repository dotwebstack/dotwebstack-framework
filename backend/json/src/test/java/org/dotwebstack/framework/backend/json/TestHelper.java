package org.dotwebstack.framework.backend.json;

public class TestHelper {

  public static String getSingleBrewery() {
    return "{\n" + "      \"identifier\": 1,\n" + "      \"brewmasters\": \"Jeroen van Hees\",\n"
        + "      \"founded\": \"2014-05-03\",\n" + "      \"name\": \"De Brouwerij\",\n" + "      \"beerCount\": 3,\n"
        + "      \"group\": \"Onafhankelijk\",\n" + "      \"owners\": [\n" + "        \"J.v.Hees\",\n"
        + "        \"I.Verhoef\",\n" + "        \"L.du Clou\",\n" + "        \"M.Kuijpers\"\n" + "      ]\n" + "    }";
  }

  public static String getJsonTestData() {
    return "{\n" + "  \"breweries\": [\n" + "    {\n" + "      \"identifier\": 1,\n"
        + "      \"brewmasters\": \"Jeroen van Hees\",\n" + "      \"founded\": \"2014-05-03\",\n"
        + "      \"name\": \"De Brouwerij\",\n" + "      \"beerCount\": 3,\n" + "      \"group\": \"Onafhankelijk\",\n"
        + "      \"owners\": [\n" + "        \"J.v.Hees\",\n" + "        \"I.Verhoef\",\n" + "        \"L.du Clou\",\n"
        + "        \"M.Kuijpers\"\n" + "      ]\n" + "    }\n" + "  ],\n" + "  \"beers\": [\n" + "    {\n"
        + "      \"identifier\": 1,\n" + "      \"brewery\": 1,\n" + "      \"name\": \"Alfa Edel Pils\"\n" + "    },\n"
        + "    {\n" + "      \"identifier\": 2,\n" + "      \"brewery\": 1,\n" + "      \"name\": \"Alfa Radler\"\n"
        + "    }\n" + "  ]\n" + "}";
  }
}
