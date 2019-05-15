package org.dotwebstack.framework.backend.rdf4j;

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

  private Constants() {
    throw new IllegalStateException(
        String.format("%s is not meant to be instantiated.", Constants.class));
  }

  // Repository

  public static final String CUSTOM_REPOSITORY_ID = "repo";

  // Properties

  public static final IRI SHAPE_GRAPH = VF.createIRI("https://github.com/dotwebstack/beer/shapes");

  public static final String SHAPE_PREFIX = "https://github.com/dotwebstack/beer/shapes#";

  // Query

  public static final String BREWERY_FIELD = "brewery";

  // Building

  public static final String BREWERY_TYPE = "Brewery";

  public static final IRI BREWERY_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(BREWERY_TYPE));

  public static final IRI BREWERY_CLASS = VF.createIRI("https://github.com/dotwebstack/beer/def#Building");

  public static final IRI BREWERY_EXAMPLE_1 = VF.createIRI("https://github.com/dotwebstack/beer/identifier/brewery/123");

  // Brewery.id

  public static final String BREWERY_IDENTIFIER_FIELD = "identifier";

  public static final IRI BREWERY_IDENTIFIER_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#identifier");

  public static final Literal BREWERY_IDENTIFIER_EXAMPLE_1 = VF.createLiteral("123");

  // Brewery.name

  public static final String BREWERY_NAME_FIELD = "name";

  public static final Literal BREWERY_NAME_EXAMPLE_1 = VF.createLiteral("Brouwerij 1923");

  // Brewery.owners

  public static final String BREWERY_OWNERS_FIELD = "owners";

  public static final Literal BREWERY_OWNERS_EXAMPLE_1 = VF.createLiteral("T. Bier");

  public static final Literal BREWERY_OWNERS_EXAMPLE_2 = VF.createLiteral("P. Pils");

  public static final IRI BREWERY_OWNERS_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#owners");

  // Brewery.founded

  public static final String BREWERY_FOUNDED_FIELD = "founded";

  public static final IRI BREWERY_FOUNDED_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def##founded");

  public static final Literal BREWERY_FOUNDED_EXAMPLE_1 = VF
      .createLiteral(datatypeFactory.newXMLGregorianCalendar("2018-05-30T09:30:10+02:00"));

}
