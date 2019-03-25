package org.dotwebstack.framework.backend.rdf4j;

import java.util.Date;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public final class Constants {

  private static final ValueFactory VF = SimpleValueFactory.getInstance();

  // Query

  public static final String BUILDING_FIELD = "building";

  public static final String BUILDING_REQ_FIELD = "buildingReq";

  // Building

  public static final String BUILDING_TYPE = "Building";

  public static final IRI SHAPE_GRAPH = VF.createIRI("example:shapes");

  public static final IRI BUILDING_SHAPE = VF.createIRI("example:shapes#Building");

  public static final IRI BUILDING_CLASS = VF.createIRI("example:def#Building");

  public static final String BUILDING_SUBJECT = "example:building/${identifier}";

  // Building.identifier

  public static final Literal BUILDING_IDENTIFIER_EXAMPLE = VF.createLiteral("123");

  public static final String BUILDING_IDENTIFIER_FIELD = "identifier";

  public static final IRI BUILDING_IDENTIFIER_SHAPE = VF
      .createIRI("example:shapes#Building_identifier");

  public static final String BUILDING_IDENTIFIER_NAME = "identifier";

  public static final IRI BUILDING_IDENTIFIER_PATH = VF.createIRI("example:def#identifier");

  // Building.height

  public static final String BUILDING_HEIGHT_FIELD = "height";

  // Building.builtAt

  public static final String BUILDING_BUILT_AT_NAME = "builtAt";

  public static final Literal BUILDING_BUILT_AT_EXAMPLE = VF.createLiteral(new Date());

}
