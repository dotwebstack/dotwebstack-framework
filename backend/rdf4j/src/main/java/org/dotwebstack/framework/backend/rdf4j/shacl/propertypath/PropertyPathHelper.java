package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import org.dotwebstack.framework.core.InvalidConfigurationException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.SHACL;

public class PropertyPathHelper {

  static final String SEQUENCE_PATH = RDF.NAMESPACE + "first";

  static final String INVERSE_PATH = SHACL.NAMESPACE + "inversePath";

  static final String ALTERNATIVE_PATH = SHACL.NAMESPACE + "alternativePath";

  static final String ZERO_OR_MORE_PATH = SHACL.NAMESPACE + "zeroOrMorePath";

  static final String ONE_OR_MORE_PATH = SHACL.NAMESPACE + "oneOrMorePath";

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
    return predicatePath.getIri().toString().equals(RDF.NIL.toString());
  }
}
