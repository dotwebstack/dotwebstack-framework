package org.dotwebstack.framework.frontend.openapi.ldpath;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import lombok.NonNull;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

public final class TrimFunction<N> extends SelectorFunction<N> {

  private static final int NO_ARGS = 1;

  /**
   * @throws IllegalArgumentException If the number of argument is not 1, this LD path function
   *         requires 1 arguments always.
   */
  @Override
  @SafeVarargs
  public final Collection<N> apply(@NonNull RDFBackend<N> backend, N context,
      Collection<N>... args) {
    if (args.length != NO_ARGS) {
      throw new IllegalArgumentException(
          String.format("LdPath function '%s' requires %d arguments.", getLocalName(), NO_ARGS));
    }

    N node = args[0].iterator().next();
    String stringValue = backend.stringValue(node);
    String trimmedStringValue = stringValue.trim();
    N literal = backend.createLiteral(trimmedStringValue);

    return ImmutableList.of(literal);
  }

  @Override
  public String getSignature() {
    return "fn:trim(node::Node) :: StringLiteral";
  }

  @Override
  public String getDescription() {
    return "Trims the string representation of a node";
  }

  @Override
  protected String getLocalName() {
    return "trim";
  }

}
