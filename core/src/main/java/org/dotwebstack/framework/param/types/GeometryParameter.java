package org.dotwebstack.framework.param.types;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Map;
import org.dotwebstack.framework.backend.BackendException;
import org.dotwebstack.framework.param.AbstractParameter;
import org.dotwebstack.framework.param.BindableParameter;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;

public class GeometryParameter extends AbstractParameter<Geometry>
    implements BindableParameter<Geometry> {

  public GeometryParameter(IRI identifier, String name) {
    super(identifier, name, false);
  }

  @Override
  protected Geometry handleInner(Map<String, String> parameterValues) {
    return parseValue(parameterValues);
  }

  @Override
  protected Geometry parseValue(Map<String, String> parameterValues) {
    return null;
  }

  @Override
  public Literal getValue(Geometry value) {
    return null;
  }

  @Override
  protected void validateRequired(Map<String, String> parameterValues) {
    if (parseValue(parameterValues) == null) {
      throw new BackendException(
          String.format("No value found for required parameter '%s'. Supplied parameterValues: %s",
              getIdentifier(), parameterValues));
    }
  }

}
