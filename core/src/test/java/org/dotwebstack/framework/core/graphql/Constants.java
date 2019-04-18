package org.dotwebstack.framework.core.graphql;

import java.time.ZonedDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Constants {

  // Query

  public static final String BUILDING_FIELD = "building";

  // Building.identifier

  public static final String BUILDING_IDENTIFIER_FIELD = "identifier";

  public static final String BUILDING_IDENTIFIER_EXAMPLE_1 = "123";

  // Building.height

  public static final String BUILDING_HEIGHT_FIELD = "height";

  public static final int BUILDING_HEIGHT_EXAMPLE_1 = 24;

  // Building.builtAt

  public static final String BUILDING_BUILT_AT_FIELD = "builtAt";

  public static final ZonedDateTime BUILDING_BUILT_AT_EXAMPLE_1 = ZonedDateTime.now();

  // Building.builtAtYear

  public static final String BUILDING_BUILT_AT_YEAR_FIELD = "builtAtYear";

  public static final int BUILDING_BUILT_AT_YEAR_EXAMPLE_1 = BUILDING_BUILT_AT_EXAMPLE_1.getYear();

}
