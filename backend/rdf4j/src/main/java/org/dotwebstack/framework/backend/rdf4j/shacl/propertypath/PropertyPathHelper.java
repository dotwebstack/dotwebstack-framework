package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;

public class PropertyPathHelper {

  private static final String SHACL_NS = "http://www.w3.org/ns/shacl#";

  private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

  private static final String NIL = RDF_NS + "nil";

  static final String SEQUENCE_PATH = RDF_NS + "first";

  static final String INVERSE_PATH = SHACL_NS + "inversePath";

  static final String ALTERNATIVE_PATH = SHACL_NS + "alternativePath";

  static final String ZERO_OR_MORE_PATH = SHACL_NS + "zeroOrMorePath";

  static final String ONE_OR_MORE_PATH = SHACL_NS + "oneOrMorePath";

  private PropertyPathHelper() {
    throw new IllegalStateException(
            String.format("%s is not meant to be instantiated.", PropertyPathHelper.class));
  }

  public static Value findRequiredProperty(Model model, Resource subject, IRI predicate) {
    return Models.getProperty(model, subject, predicate)
            .orElseThrow(() -> new InvalidConfigurationException(
                            String.format("Resource '%s' requires a '%s' IRI property.",
                                    subject, predicate)));
  }

  public static boolean isNil(PredicatePath predicatePath) {
    return predicatePath.getIri().toString().equals(NIL);
  }
}
