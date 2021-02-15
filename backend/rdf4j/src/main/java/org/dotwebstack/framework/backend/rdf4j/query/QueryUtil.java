package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Objects;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class QueryUtil {

  private QueryUtil() {}


  public static Geometry parseGeometryOrNull(String wktString) {

    if (Objects.nonNull(wktString)) {
      WKTReader reader = new WKTReader();
      try {
        return reader.read(wktString);
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    return null;
  }
}
