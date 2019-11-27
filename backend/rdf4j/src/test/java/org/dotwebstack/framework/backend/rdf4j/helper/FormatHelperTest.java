package org.dotwebstack.framework.backend.rdf4j.helper;

import static org.dotwebstack.framework.backend.rdf4j.helper.FormatHelper.formatQuery;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FormatHelperTest {

  @Test
  public void formatQuery_returnsFormattedQuery_forSelectQuery() {
    String unformattedQuery =
        "SELECT ?s\n" + "WHERE { { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github"
            + ".com/dotwebstack/beer/def#Brewery> . }\n"
            + "{ ?s (<http://schema.org/name>|<https://github.com/dotwebstack/beer/def#label>|<http://www.w3"
            + ".org/1999/02/22-rdf-syntax-ns#nil>) ?x0 .\n"
            + "FILTER ( ?x0 = \"Heineken Nederland\"^^<http://www.w3.org/2001/XMLSchema#string> ) } }\n"
            + "ORDER BY ( !( BOUND( ?x0 ) ) ) ASC( ?x0 )\n";
    String expected = "SELECT ?s WHERE {\n" + "  {\n"
        + "    ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Brewery> "
        + ".\n" + "  }\n" + "  {\n"
        + "    ?s (<http://schema.org/name>|<https://github.com/dotwebstack/beer/def#label>|<http://www.w3"
        + ".org/1999/02/22-rdf-syntax-ns#nil>) ?x0 .\n"
        + "     FILTER ( ?x0 = \"Heineken Nederland\"^^<http://www.w3.org/2001/XMLSchema#string> )\n" + "  }\n" + "}\n"
        + "ORDER BY ( !( BOUND( ?x0 ) ) ) ASC( ?x0 ) ";

    // Act
    String result = formatQuery(unformattedQuery);

    // Assert
    assertEquals(expected, result);
  }

  @Test
  public void formatQuery_returnsFormattedQuery_forConstructQuery() {
    // Arrange
    String unformattedQuery = "CONSTRUCT { ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github"
        + ".com/dotwebstack/beer/def#Beer> .\n" + "?x0 <https://github.com/dotwebstack/beer/def#identifier> ?x1 .\n"
        + "?x0 <https://github.com/dotwebstack/beer/def#beertype> ?x2 .\n"
        + "?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#BeerType> .\n"
        + "?x2 <http://www.dotwebstack.org/alternativepath#d2ebe8cd-42aa-401b-a756-8f982c55d9f7> ?x3 . }\n"
        + "WHERE {VALUES ?x0 {<https://github.com/dotwebstack/beer/id/beer/6>}  { ?x0 <http://www.w3"
        + ".org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Beer> . }\n"
        + "OPTIONAL { ?x0 <https://github.com/dotwebstack/beer/def#identifier> ?x1 . }\n" + "{ SELECT ?x0 ?x2 ?x3\n"
        + "WHERE {VALUES ?x0 {<https://github.com/dotwebstack/beer/id/beer/6>}  OPTIONAL { ?x0 <https://github"
        + ".com/dotwebstack/beer/def#beertype> ?x2 .\n"
        + "{ ?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#BeerType> ."
        + " }\n"
        + "OPTIONAL { ?x2 (<http://schema.org/name>|<https://github.com/dotwebstack/beer/def#label>|<http://www.w3"
        + ".org/1999/02/22-rdf-syntax-ns#nil>) ?x3 . } } }\n" + " } }\n";
    String expected = "CONSTRUCT {\n"
        + "  ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Beer> .\n"
        + "   ?x0 <https://github.com/dotwebstack/beer/def#identifier> ?x1 .\n"
        + "   ?x0 <https://github.com/dotwebstack/beer/def#beertype> ?x2 .\n"
        + "   ?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#BeerType> "
        + ".\n" + "   ?x2 <http://www.dotwebstack.org/alternativepath#d2ebe8cd-42aa-401b-a756-8f982c55d9f7> ?x3 .\n"
        + "}\n" + "WHERE {\n" + "  VALUES ?x0 {\n" + "    <https://github.com/dotwebstack/beer/id/beer/6>\n" + "  }\n"
        + "  {\n"
        + "    ?x0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github.com/dotwebstack/beer/def#Beer> .\n"
        + "  }\n" + "  OPTIONAL {\n" + "    ?x0 <https://github.com/dotwebstack/beer/def#identifier> ?x1 .\n" + "  }\n"
        + "  {\n" + "    SELECT ?x0 ?x2 ?x3 WHERE {\n" + "      VALUES ?x0 {\n"
        + "        <https://github.com/dotwebstack/beer/id/beer/6>\n" + "      }\n" + "      OPTIONAL {\n"
        + "        ?x0 <https://github.com/dotwebstack/beer/def#beertype> ?x2 .\n" + "        {\n"
        + "          ?x2 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <https://github"
        + ".com/dotwebstack/beer/def#BeerType> .\n" + "        }\n" + "        OPTIONAL {\n"
        + "          ?x2 (<http://schema.org/name>|<https://github.com/dotwebstack/beer/def#label>|<http://www.w3"
        + ".org/1999/02/22-rdf-syntax-ns#nil>) ?x3 .\n" + "        }\n" + "      }\n" + "    }\n" + "  }\n" + "}\n";

    // Act
    String result = formatQuery(unformattedQuery);

    // Assert
    assertEquals(expected, result);
  }
}
