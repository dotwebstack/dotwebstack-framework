package org.dotwebstack.framework.param;

import org.dotwebstack.framework.param.term.TermParameter;
import org.eclipse.rdf4j.model.Resource;

/**
 * A {@code ParameterDefinition} contains the data a {@link Parameter} will be created with. Each
 * parameter has an identifier and name. The definition is also responsible for creating the
 * parameters with {@link #createRequiredParameter()} or {@link #createOptionalParameter()}.
 * <p/>
 * Implementations can add extra fields if required. For example a {@link TermParameter} has a SHACL
 * shape type. Therefore the {@link TermParameterDefinition} has a shape type field to create a term
 * parameter with.
 */
public interface ParameterDefinition<T extends Parameter<?>> {

  Resource getIdentifier();

  String getName();

  T createRequiredParameter();

  T createOptionalParameter();

}
