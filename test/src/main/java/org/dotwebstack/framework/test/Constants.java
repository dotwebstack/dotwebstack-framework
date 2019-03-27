package org.dotwebstack.framework.test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class Constants {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  private static DatatypeFactory datatypeFactory;

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

  // Query

  public static final String BUILDING_FIELD = "building";

  public static final String BUILDING_REQ_FIELD = "buildingReq";

  public static final String BUILDINGS_FIELD = "buildings";

  // Building

  public static final String BUILDING_TYPE = "Building";

  public static final IRI SHAPE_GRAPH = VF.createIRI("http://example/shapes");

  public static final IRI BUILDING_SHAPE = VF.createIRI("http://example/shapes#Building");

  public static final IRI BUILDING_CLASS = VF.createIRI("http://example/def#Building");

  public static final String BUILDING_SUBJECT = "http://example/building/${identifier}";

  // Building.identifier

  public static final Literal BUILDING_IDENTIFIER_EXAMPLE_1 = VF.createLiteral("123");

  public static final Literal BUILDING_IDENTIFIER_EXAMPLE_2 = VF.createLiteral("456");

  public static final String BUILDING_IDENTIFIER_FIELD = "identifier";

  public static final IRI BUILDING_IDENTIFIER_SHAPE = VF
      .createIRI("http://example/shapes#Building_identifier");

  public static final String BUILDING_IDENTIFIER_NAME = "identifier";

  public static final IRI BUILDING_IDENTIFIER_PATH = VF.createIRI("http://example/def#identifier");

  // Building.height

  public static final Literal BUILDING_HEIGHT_EXAMPLE = VF.createLiteral(24);

  public static final String BUILDING_HEIGHT_FIELD = "height";

  // Building.builtAt

  public static final Literal BUILDING_BUILT_AT_EXAMPLE = VF
      .createLiteral(datatypeFactory.newXMLGregorianCalendar("2018-05-30T09:30:10+02:00"));

  public static final String BUILDING_BUILT_AT_FIELD = "builtAt";

}
