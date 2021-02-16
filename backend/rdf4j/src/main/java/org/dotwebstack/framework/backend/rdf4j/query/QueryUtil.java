package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PredicatePath;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.PropertyPath;
import org.eclipse.rdf4j.query.BindingSet;
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

  public static void addBinding(Map<String, Function<BindingSet, Object>> assembleFns, String alias,
      PropertyShape propertyShape, String fieldName) {

    if ("asWKT".equals(getPropertyPathLocalName(propertyShape))) {
      assembleFns.put(fieldName,
          bindingSet -> bindingSet.getValue(alias) != null ? parseGeometryOrNull(bindingSet.getValue(alias)
              .stringValue()) : null);
    } else {
      assembleFns.put(fieldName, bindingSet -> bindingSet.getValue(alias) != null ? bindingSet.getValue(alias)
          .stringValue() : null);
    }
  }

  public static String getPropertyPathLocalName(PropertyShape propertyShape) {

    if (Objects.nonNull(propertyShape) && Objects.nonNull(propertyShape.getPath())) {
      return getLocalName(propertyShape.getPath());
    }
    return null;
  }

  public static String getLocalName(PropertyPath propertyPath) {

    if (propertyPath instanceof PredicatePath) {
      PredicatePath predicatePath = (PredicatePath) propertyPath;

      if (Objects.nonNull(predicatePath.getIri())) {
        return predicatePath.getIri()
            .getLocalName();
      }
    }
    return null;
  }
}
