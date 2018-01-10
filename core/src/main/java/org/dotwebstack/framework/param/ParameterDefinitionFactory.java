package org.dotwebstack.framework.param;

import lombok.NonNull;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;

public interface ParameterDefinitionFactory {

  ParameterDefinition create(@NonNull Model model, @NonNull IRI id);

  boolean supports(@NonNull IRI type);

}
