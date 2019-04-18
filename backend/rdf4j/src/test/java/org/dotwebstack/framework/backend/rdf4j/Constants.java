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

  public static final IRI SHAPE_GRAPH = VF.createIRI("http://example/shapes");

  public static final String SHAPE_PREFIX = "http://example/shapes#";

  // Query

  public static final String BUILDING_FIELD = "building";

  // Building

  public static final String BUILDING_TYPE = "Building";

  public static final IRI BUILDING_SHAPE = VF.createIRI(SHAPE_PREFIX.concat(BUILDING_TYPE));

  public static final IRI BUILDING_CLASS = VF.createIRI("http://example/def#Building");

  public static final IRI BUILDING_EXAMPLE_1 = VF.createIRI("http://example/building/123");

  // Building.identifier

  public static final String BUILDING_IDENTIFIER_FIELD = "identifier";

  public static final IRI BUILDING_IDENTIFIER_PATH = VF.createIRI("http://example/def#identifier");

  public static final Literal BUILDING_IDENTIFIER_EXAMPLE_1 = VF.createLiteral("123");

  // Building.height

  public static final String BUILDING_HEIGHT_FIELD = "height";

  public static final Literal BUILDING_HEIGHT_EXAMPLE_1 = VF.createLiteral(24);

  // Building.builtAt

  public static final String BUILDING_BUILT_AT_FIELD = "builtAt";

  public static final IRI BUILDING_BUILT_AT_PATH = VF.createIRI("http://example/def#builtAt");

  public static final Literal BUILDING_BUILT_AT_EXAMPLE_1 = VF
      .createLiteral(datatypeFactory.newXMLGregorianCalendar("2018-05-30T09:30:10+02:00"));

}
