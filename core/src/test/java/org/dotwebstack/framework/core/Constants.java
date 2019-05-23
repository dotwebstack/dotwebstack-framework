package org.dotwebstack.framework.core;

import java.time.ZonedDateTime;

public final class Constants {

  private Constants() {
    throw new IllegalStateException(String.format("%s is not meant to be instantiated.", Constants.class));
  }

  // Query

  public static final String BREWERY_FIELD = "brewery";

  // Brewery.id

  public static final String BREWERY_IDENTIFIER_FIELD = "identifier";

  public static final String BREWERY_IDENTIFIER_EXAMPLE_1 = "123";

  // Brewery.name

  public static final String BREWERY_NAME_FIELD = "name";

  public static final String BREWERY_NAME_EXAMPLE_1 = "Brouwerij 1923";

  // Brewery.founded

  public static final String BREWERY_FOUNDED_FIELD = "founded";

  public static final ZonedDateTime BREWERY_FOUNDED_EXAMPLE_1 = ZonedDateTime.now();

  // Building.foundedAtYear

  public static final String BREWERY_FOUNDED_AT_YEAR_FIELD = "foundedAtYear";

  public static final int BREWERY_FOUNDED_AT_YEAR_EXAMPLE_1 = BREWERY_FOUNDED_EXAMPLE_1.getYear();

}
