package org.dotwebstack.framework.frontend.openapi.ldpath;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

public final class ExistsFunction<N> extends SelectorFunction<N> {

  private static final int NO_ARGS = 1;
  private static final String XSD_BOOLEAN = "http://www.w3.org/2001/XMLSchema#boolean";

  /**
   * @throws IllegalArgumentException If the number of argument is not 1, this LD path function
   *         requires 1 arguments always.
   */
  @Override
  @SafeVarargs
  public final Collection<N> apply(RDFBackend<N> backend, N context, Collection<N>... args) {
    if (args.length != NO_ARGS) {
      throw new IllegalArgumentException(
          String.format("LdPath function '%s' requires %d arguments.", getLocalName(), NO_ARGS));
    }

    String existsVal = String.valueOf(!args[0].isEmpty());

    try {
      return Collections.singleton(backend.createLiteral(existsVal, null, new URI(XSD_BOOLEAN)));
    } catch (URISyntaxException e) {
      throw new IllegalStateException("xsd:boolean URI configured incorrectly.");
    }
  }

  @Override
  public String getSignature() {
    return "fn:exists(node::Node) :: BooleanLiteral";
  }

  @Override
  public String getDescription() {
    return "Returns true if the node exists";
  }

  @Override
  public String getLocalName() {
    return "exists";
  }

}
