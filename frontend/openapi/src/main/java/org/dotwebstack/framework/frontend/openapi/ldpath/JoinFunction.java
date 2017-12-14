package org.dotwebstack.framework.frontend.openapi.ldpath;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.marmotta.ldpath.api.backend.RDFBackend;
import org.apache.marmotta.ldpath.api.functions.SelectorFunction;
import org.apache.marmotta.ldpath.model.transformers.StringTransformer;

public final class JoinFunction<N> extends SelectorFunction<N> {

  private static final int MIN_NO_ARGS = 1;

  private final StringTransformer<N> transformer = new StringTransformer<>();

  /**
   * @throws IllegalArgumentException If the number of argument is zero, this LD path function
   *         requires at least 1 argument.
   */
  @Override
  @SafeVarargs
  public final Collection<N> apply(@NonNull RDFBackend<N> backend, N context,
      Collection<N>... args) {
    if (args.length < MIN_NO_ARGS) {
      throw new IllegalArgumentException(String.format(
          "LdPath function '%s' requires at least %d argument.", getLocalName(), MIN_NO_ARGS));
    }

    Iterator<N> iterator = org.apache.marmotta.ldpath.util.Collections.iterator(1, args);
    ImmutableList.Builder<String> builder = ImmutableList.builder();

    while (iterator.hasNext()) {
      String result = transformer.transform(backend, iterator.next(), null);

      if (!StringUtils.isBlank(result)) {
        builder.add(result);
      }
    }

    String delimiter = backend.stringValue(args[0].iterator().next());
    String joined = String.join(delimiter, builder.build());

    if (StringUtils.isBlank(joined)) {
      return Collections.emptyList();
    }

    return Collections.singleton(backend.createLiteral(joined));
  }

  @Override
  public String getLocalName() {
    return "join";
  }

  @Override
  public String getSignature() {
    return "fn:join(separator : String, nodes : NodeList) : String";
  }

  @Override
  public String getDescription() {
    return "A node function joins a list of nodes interpreted as strings while "
        + "separating them with the supplied separator";
  }

}
