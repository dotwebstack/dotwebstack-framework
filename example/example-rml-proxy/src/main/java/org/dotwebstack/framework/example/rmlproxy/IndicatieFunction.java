package org.dotwebstack.framework.example.rmlproxy;

import com.taxonic.carml.engine.function.FnoFunction;
import com.taxonic.carml.engine.function.FnoParam;
import java.util.Objects;

public class IndicatieFunction {

  private static final String BEER_NS = "http://dotwebstack.org/id/mapping/beer#";

  @FnoFunction(BEER_NS + "booleanToIndicatie")
  public static String booleanToIndicatie(@FnoParam(BEER_NS + "valueParam") String value) {
    return Objects.equals(value, "true") ? "J" : "N";
  }
}
