package org.dotwebstack.framework.backend.rdf4j;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.eclipse.rdf4j.model.BNode;
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

  // Repository

  public static final String CUSTOM_REPOSITORY_ID = "repo";

  // Properties

  public static String RDF4J_PREFIX = "http://rdf4j.org/schema/rdf4j#";

  public static IRI SHACH_SHAPE_GRAPH = VF.createIRI(RDF4J_PREFIX, "SHACLShapeGraph");

  public static final IRI SHAPE_GRAPH = VF.createIRI("https://github.com/dotwebstack/beer/shapes");

  public static final String SHAPE_PREFIX = "https://github.com/dotwebstack/beer/shapes#";

  public static final String SCHEMA_PREFIX = "http://schema.org/";

  public static final String XSD_PREFIX = "http://www.w3.org/2001/XMLSchema#";

  public static final String SHACL_PREFIX = "http://www.w3.org/ns/shacl#";

  public static final String BEER_DEF = "https://github.com/dotwebstack/beer/def#";

  public static final IRI BEER_3 = VF.createIRI(BEER_DEF.concat("3"));
  // Query

  public static final IRI XSD_STRING = VF.createIRI(XSD_PREFIX.concat("string"));

  public static final IRI SHACL_LITERAL = VF.createIRI(SHACL_PREFIX.concat("Literal"));

  public static final String BREWERY_FIELD = "brewery";

  public static final String BREWERIES_FIELD = "breweries";

  // Building

  public static final String BREWERY_TYPE = "Brewery";

  public static final IRI BREWERY_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(BREWERY_TYPE));

  public static final IRI BREWERY_TARGET_CLASS = VF.createIRI(BEER_DEF.concat("Brewery"));

  public static final IRI BREWERY_CLASS = VF.createIRI("https://github.com/dotwebstack/beer/def#Building");

  public static final IRI BREWERY_EXAMPLE_1 =
      VF.createIRI("https://github.com/dotwebstack/beer/identifier/brewery/123");

  // Brewery.id

  public static final String BREWERY_IDENTIFIER_FIELD = "identifier";

  public static final IRI BREWERY_IDENTIFIER_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#identifier");

  public static final Literal BREWERY_IDENTIFIER_EXAMPLE_1 = VF.createLiteral("123");

  // Brewery.subject
  public static final String BREWERY_SUBJECT_FIELD = "subject";

  public static final String BREWERY_SUBJECT_EXAMPLE_1 = "https://github.com/dotwebstack/beer/id/brewery/123";

  // Brewery.name

  public static final String BREWERY_NAME_FIELD = "name";

  public static final String BREWERY_NAME_TYPE = "Brewery_name";

  public static final IRI BREWERY_LABEL = VF.createIRI("https://github.com/dotwebstack/beer/def#label");

  public static final IRI BREWERY_LABEL_PATH = VF.createIRI(SHAPE_PREFIX.concat(BREWERY_NAME_TYPE));

  public static final Literal BREWERY_NAME_EXAMPLE_1 = VF.createLiteral("Brouwerij 1923");

  public static final IRI BREWERY_BEERS = VF.createIRI("https://github.com/dotwebstack/beer/def#beers");

  public static final String BREWERY_BEERS_FIELD = "beers";

  // Brewery.owners

  public static final String BREWERY_OWNERS_FIELD = "owners";

  public static final Literal BREWERY_OWNERS_EXAMPLE_1 = VF.createLiteral("T. Bier");

  public static final Literal BREWERY_OWNERS_EXAMPLE_2 = VF.createLiteral("P. Pils");

  public static final IRI BREWERY_OWNERS_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#owners");

  // Brewery.founded

  public static final String BREWERY_FOUNDED_FIELD = "founded";

  public static final String BREWERY_FOUNDED_TYPE = "Brewery_founded";

  public static final IRI BREWERY_FOUNDED_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#founded");

  public static final IRI BREWERY_FOUNDED_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(BREWERY_FOUNDED_TYPE));

  public static final Literal BREWERY_FOUNDED_EXAMPLE_1 =
      VF.createLiteral(datatypeFactory.newXMLGregorianCalendar("2018-05-30T09:30:10.000+02:00"));

  // Brewery.postalCode

  public static final String POSTAL_CODE_FIELD = "postalCode";

  public static final String BREWERY_POSTAL_CODE_TYPE = "Brewery_postalCode";

  public static final IRI BREWERY_POSTAL_CODE_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(BREWERY_POSTAL_CODE_TYPE));

  public static final String POSTAL_CODE_1 = "1234 AB";


  // Brewery.beers

  public static final IRI BEERS_TARGET_CLASS = VF.createIRI(BEER_DEF.concat("Beers"));

  public static final String BEERS_FIELD = "beers";

  public static final String BEER_NAMES_FIELD = "beerNames";

  public static final String BEERS_NAME_FIELD = "name";

  public static final String BREWERY_BEERS_TYPE = "Brewery_beers";

  public static final String BREWERY_BEERNAMES_TYPE = "Brewery_beerNames";

  public static final IRI BREWERY_BEERS_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(BREWERY_BEERS_TYPE));

  public static final IRI BREWERY_BEERNAMES_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(BREWERY_BEERNAMES_TYPE));

  public static final IRI BREWERY_BEERS_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#brewery");

  public static final BNode BEER_EXAMPLE_1 = VF.createBNode();

  public static final IRI BEER_EXAMPLE_2 = VF.createIRI("https://github.com/dotwebstack/beer/identifier/beer/1");

  public static final Literal BEER_IDENTIFIER_EXAMPLE_1 = VF.createLiteral("1");

  public static final String BEER_NAME_EXAMPLE_1 = "Beer 1";

  public static final String BEER_FIELD = "beer";

  public static final String BEER_NAME_FIELD = "name";


  // Beer.beertypes

  public static final String BEERTYPES_FIELD = "beerTypes";

  public static final String BEERTYPES_RAW_FIELD = "beerTypesRaw";

  public static final String BREWERY_BEERTYPE_TYPE = "Beer_beerTypes";

  public static final String BREWERY_BEERTYPE_ZERO_OR_MORE_TYPE = "Beer_beerTypesZeroOrMore";

  public static final String BREWERY_BEERTYPE_ZERO_OR_ONE_TYPE = "Beer_beerTypesZeroOrOne";

  public static final IRI BEER_BEERTYPE_PATH = VF.createIRI("https://github.com/dotwebstack/beer/def#beertype");

  public static final IRI BEER_BEERTYPE_SHAPE = VF.createIRI(SHAPE_PREFIX + BREWERY_BEERTYPE_TYPE);

  public static final IRI BEER_BEERTYPE_ZERO_OR_MORE_SHAPE =
      VF.createIRI(SHAPE_PREFIX + BREWERY_BEERTYPE_ZERO_OR_MORE_TYPE);

  public static final IRI BEER_BEERTYPE_ZERO_OR_ONE_SHAPE =
      VF.createIRI(SHAPE_PREFIX + BREWERY_BEERTYPE_ZERO_OR_ONE_TYPE);

  public static final BNode BEERTYPE_EXAMPLE_1 = VF.createBNode();

  public static final String BEERTYPE_EXAMPLE_1_NAME = "Beertype1";

  public static final BNode BEERTYPE_EXAMPLE_2 = VF.createBNode();

  public static final String BEERTYPE_EXAMPLE_2_NAME = "Beertype2";

  public static final IRI BEER_INGREDIENT = VF.createIRI("https://github.com/dotwebstack/beer/def#ingredient");

  // Schema

  public static final IRI SCHEMA_ADDRESS = VF.createIRI(SCHEMA_PREFIX + "address");

  public static final IRI SCHEMA_NAME = VF.createIRI(SCHEMA_PREFIX + "name");

  public static final IRI SCHEMA_POSTAL_CODE = VF.createIRI(SCHEMA_PREFIX + "postalCode");

  // Brewery.address

  public static final String ADDRESS_TYPE = "Brewery_Address";

  public static final IRI ADDRESS_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(ADDRESS_TYPE));

  public static final String BREWERY_ADDRESS_FIELD = "address";

  public static final IRI BREWERY_ADDRESS_PATH = VF.createIRI("http://schema.org/address");

  public static final BNode BREWERY_ADDRESS_EXAMPLE_1 = VF.createBNode();

  public static final BNode BREWERY_ADDRESS_EXAMPLE_2 = VF.createBNode();

  // Address.postalCode

  public static final String POSTALCODE_TYPE = "Address_PostalCode";

  public static final IRI POSTALCODE_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(POSTALCODE_TYPE));

  public static final IRI ADDRESS_POSTALCODE_PATH = VF.createIRI("http://schema.org/postalCode");

  public static final String ADDRESS_POSTALCODE_EXAMPLE_1 = "1234 AC";

  public static final String ADDRESS_POSTALCODE_EXAMPLE_2 = "1234 BD";

  // Beer.ingredient

  public static final String INGREDIENTS_FIELD = "ingredients";

  public static final String INGREDIENTS_NAME_FIELD = "name";

  // Beer.supplement

  public static final String SUPPLEMENTS_FIELD = "supplements";

  public static final String SUPPLEMENTS_NAME_FIELD = "name";

  // Ingredients

  public static final IRI INGREDIENTS_TARGET_CLASS = VF.createIRI(BEER_DEF.concat("Ingredients"));

}
