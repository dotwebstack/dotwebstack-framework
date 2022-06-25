package org.dotwebstack.framework.integrationtest.rmlpostgres;

import io.carml.engine.function.FnoFunction;
import io.carml.engine.function.FnoParam;

public class IndicatieFunction {

  private static final String BEER_NS = "http://dotwebstack.org/id/mapping/beer#";

  @FnoFunction(BEER_NS + "booleanToIndicatie")
  public static String booleanToIndicatie(@FnoParam(BEER_NS + "valueParam") boolean value) {
    return value ? "J" : "N";
  }
}
