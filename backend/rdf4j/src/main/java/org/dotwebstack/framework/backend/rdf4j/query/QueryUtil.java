package org.dotwebstack.framework.backend.rdf4j.query;

import static org.dotwebstack.framework.core.helpers.ExceptionHelper.illegalArgumentException;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.dotwebstack.framework.backend.rdf4j.shacl.PropertyShape;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class QueryUtil {

  private static final IRI GEOMETRY_IRI = SimpleValueFactory.getInstance()
      .createIRI("http://www.opengis.net/ont/geosparql#wktLiteral");

  private QueryUtil() {}


  public static Geometry parseGeometryOrNull(String wktString) {

    if (Objects.nonNull(wktString)) {
      var reader = new WKTReader();
      try {
        return reader.read(wktString);
      } catch (ParseException ignore) {
        throw illegalArgumentException("invalid wkt string");
      }
    }

    return null;
  }

  public static void addBinding(Map<String, Function<BindingSet, Object>> assembleFns, String alias,
      PropertyShape propertyShape, String fieldName) {

    if (GEOMETRY_IRI.equals(propertyShape.getDatatype())) {
      assembleFns.put(fieldName,
          bindingSet -> bindingSet.getValue(alias) != null ? parseGeometryOrNull(bindingSet.getValue(alias)
              .stringValue()) : null);
    } else {
      assembleFns.put(fieldName, bindingSet -> bindingSet.getValue(alias) != null ? bindingSet.getValue(alias)
          .stringValue() : null);
    }
  }
}
