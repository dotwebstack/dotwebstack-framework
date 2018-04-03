package org.dotwebstack.framework.frontend.openapi.ldpath;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import lombok.NonNull;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;

public final class KeepAfterLastFunction<N> extends SelectorFunction<N> {

  private static final int NO_ARGS = 2;

  /**
   * @throws IllegalArgumentException If the number of argument is not 2, this LD path function
   *         requires 2 arguments always.
   */
  @Override
  @SafeVarargs
  public final Collection<N> apply(@NonNull RDFBackend<N> backend, N context,
      Collection<N>... args) {
    if (args.length != NO_ARGS) {
      throw new IllegalArgumentException(
          String.format("LdPath function %s requires %d arguments.", getLocalName(), NO_ARGS));
    }

    if (args[0].isEmpty()) {
      return Collections.emptyList();
    }

    final N node = args[0].iterator().next();
    final String separator = backend.stringValue(args[1].iterator().next());

    String str = backend.stringValue(node);
    int separatorIndex = str.lastIndexOf(separator);

    if (separatorIndex != -1) {
      str = str.substring(str.lastIndexOf(separator) + 1);
    }

    return ImmutableList.of(backend.createLiteral(str));
  }

  @Override
  public String getSignature() {
    return "fn:keepAfterLast(node::Node, separator::StringLiteral) :: StringLiteral";
  }

  @Override
  public String getDescription() {
    return "Returns the last part of the string representation of a node after the given separator";
  }

  @Override
  protected String getLocalName() {
    return "keepAfterLast";
  }

}
