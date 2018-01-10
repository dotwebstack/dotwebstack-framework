package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface ParameterDefinitionFactory {

  ParameterDefinition create(@NonNull Model model, @NonNull IRI id);

  /**
   * @return Returns {@code true} if this implementation supports creating the supplied parameter
   *         type; {@code false} otherwise.
   */
  boolean supports(@NonNull IRI type);

}
