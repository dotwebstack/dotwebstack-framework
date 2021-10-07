package org.dotwebstack.framework.ext.spatial;

import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Getter
@AllArgsConstructor
public enum GeometrySrid {

  RD_2D(28992, 2), RD_NAP_3D(7415, 3), ETRS89_ETRF2000_2D(9067, 2), ETRS89_ETRF2000_3D(7931, 3);

  private final int srid;

  private final int dimension;

  public static GeometrySrid valueOfSrid(String srid) {
    if (StringUtils.isBlank(srid)) {
      throw new IllegalArgumentException("Argument srid can't be null or empty.");
    }
    if (!NumberUtils.isCreatable(srid)) {
      throw new IllegalArgumentException("Argument srid must be a number.");
    }

    return valueOfSrid(Integer.parseInt(srid));
  }

  public static GeometrySrid valueOfSrid(int srid) {
    return Arrays.stream(values())
        .filter(geometrySrid -> geometrySrid.getSrid() == srid)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("No srid found for '%s'. Valid srids are: %s", srid, supportedSrids())));
  }

  private static String supportedSrids() {
    return Arrays.stream(values())
        .map(GeometrySrid::getSrid)
        .map(srid -> Integer.toString(srid))
        .collect(Collectors.joining(","));
  }
}
