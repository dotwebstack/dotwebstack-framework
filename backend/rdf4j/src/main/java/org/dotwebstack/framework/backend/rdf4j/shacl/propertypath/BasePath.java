package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.createIri;
import static org.dotwebstack.framework.backend.rdf4j.helper.IriHelper.stringify;

import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.dotwebstack.framework.backend.rdf4j.constants.Rdf4jConstants;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.sparqlbuilder.rdf.RdfPredicate;

@Setter
@Getter
public abstract class BasePath implements PropertyPath {

  private UUID uuid = UUID.randomUUID();

  private IRI baseIri = createIri(Rdf4jConstants.DOTWEBSTACK_NAMESPACE + this.getClass()
      .getSimpleName()
      .toLowerCase() + "#" + uuid.toString());

  @Override
  public Set<Value> resolvePath(Model model, Resource subject) {
    return Models.getProperties(model, subject, getBaseIri());
  }

  public RdfPredicate toConstructPredicate() {
    return () -> stringify(getBaseIri());
  }

}
