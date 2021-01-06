package org.dotwebstack.framework.backend.rdf4j.shacl;

import static org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType.HASVALUE;
import static org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType.MAXCOUNT;
import static org.dotwebstack.framework.backend.rdf4j.shacl.ConstraintType.MINCOUNT;

import java.util.EnumMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import org.dotwebstack.framework.backend.rdf4j.shacl.propertypath.BasePath;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Builder
@Getter
public final class PropertyShape {

  private final Resource identifier;

  private final String name;

  private final BasePath path;

  @Builder.Default
  private final Map<ConstraintType, Object> constraints = new EnumMap<>(ConstraintType.class);

  private final IRI nodeKind;

  private final NodeShape node;

  private final IRI datatype;

  public Integer getMinCount() {
    if (constraints.containsKey(MINCOUNT)) {
      return ((Literal) constraints.get(MINCOUNT)).intValue();
    }
    return null;
  }

  public Integer getMaxCount() {
    if (constraints.containsKey(MAXCOUNT)) {
      return ((Literal) constraints.get(MAXCOUNT)).intValue();
    }
    return null;
  }

  public Value getHasValue() {
    return (Value) constraints.getOrDefault(HASVALUE, null);
  }

  public RdfPredicate toConstructPredicate() {
    return getPath().toConstructPredicate();
  }

  public RdfPredicate toPredicate() {
    return getPath().toPredicate();
  }
}
