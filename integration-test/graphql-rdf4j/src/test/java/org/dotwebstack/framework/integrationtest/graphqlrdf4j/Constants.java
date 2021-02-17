package org.dotwebstack.framework.integrationtest.graphqlrdf4j;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class Constants {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static final DatatypeFactory datatypeFactory;

  static {
    try {
      datatypeFactory = DatatypeFactory.newInstance();
    } catch (DatatypeConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  private Constants() {}

  private static final String SCHEMA_PREFIX = "http://schema.org/";

  static final String BREWERY_FIELD = "brewery";

  static final String BREWERIES_FIELD = "breweries";

  static final String BREWERY_IDENTIFIER_FIELD = "identifier";

  static final Literal BREWERY_IDENTIFIER_EXAMPLE_1 = VF.createLiteral("123");

  static final String BREWERY_SUBJECT_FIELD = "subject";

  static final String BREWERY_SUBJECT_EXAMPLE_1 = "https://github.com/dotwebstack/beer/id/brewery/123";

  static final String BREWERY_NAME_FIELD = "name";

  static final Literal BREWERY_NAME_EXAMPLE_1 = VF.createLiteral("Brouwerij 1923");

  static final String BREWERY_FOUNDED_FIELD = "founded";

  static final Literal BREWERY_FOUNDED_EXAMPLE_1 =
      VF.createLiteral(datatypeFactory.newXMLGregorianCalendar("2018-05-30"));

  static final String BEERS_FIELD = "beers";

  static final String BEERS_NAME_FIELD = "name";

  static final Literal BEER_IDENTIFIER_EXAMPLE_1 = VF.createLiteral("1");

  static final String BEER_FIELD = "beer";

  static final String BEER_NAME_FIELD = "name";

  static final String BEERTYPES_RAW_FIELD = "beerTypesRaw";

  static final IRI SCHEMA_NAME = VF.createIRI(SCHEMA_PREFIX + "name");

  static final String BREWERY_ADDRESS_FIELD = "address";

  static final String BREWERY_GEOMETRY_FIELD = "geometry";

  static final String INGREDIENTS_FIELD = "ingredients";

  static final String INGREDIENTS_NAME_FIELD = "name";

  static final String SUPPLEMENTS_FIELD = "supplements";

  static final String SUPPLEMENTS_NAME_FIELD = "name";

}
