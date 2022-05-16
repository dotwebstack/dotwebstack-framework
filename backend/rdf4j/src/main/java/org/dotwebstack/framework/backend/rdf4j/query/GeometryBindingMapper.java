package org.dotwebstack.framework.backend.rdf4j.query;

import java.util.Objects;
import lombok.Getter;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

@Getter
class GeometryBindingMapper extends BindingMapper {

  private final WKTReader wktReader = new WKTReader();

  public GeometryBindingMapper(String alias) {
    super(alias);
  }

  public GeometryBindingMapper(Variable variable) {
    super(variable);
  }

  @Override
  public Object apply(BindingSet bindings) {
    try {
      return wktReader.read(Objects.toString(super.apply(bindings)));
    } catch (ParseException e) {
      throw new IllegalStateException("Unable to read wkt geometry", e);
    }
  }
}
